package com.example.ai_music_pro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_music_pro.data.repository.SongRepository
import com.example.ai_music_pro.domain.model.CarouselItem
import com.example.ai_music_pro.domain.model.Song
import com.example.ai_music_pro.socket.RoomEvent
import com.example.ai_music_pro.socket.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    private val repository: SongRepository,
    private val socketManager: SocketManager
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _carousels = MutableStateFlow<List<CarouselItem>>(emptyList())
    val carousels: StateFlow<List<CarouselItem>> = _carousels.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var _filteredSongs = MutableStateFlow<List<Song>>(emptyList())
    val filteredSongs: StateFlow<List<Song>> = _filteredSongs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Room State
    private val _currentRoomId = MutableStateFlow<String?>(null)
    val currentRoomId: StateFlow<String?> = _currentRoomId.asStateFlow()

    init {
        fetchSongs()
        fetchCarousels()
        observeSocketEvents()
        socketManager.connect()
        
        viewModelScope.launch {
            // Combine songs and search query for filtering
            songs.collect { list ->
                updateFilteredSongs(list, _searchQuery.value)
            }
        }
        viewModelScope.launch {
            _searchQuery.collect { query ->
                updateFilteredSongs(_songs.value, query)
            }
        }
    }

    private fun fetchCarousels() {
        viewModelScope.launch {
            repository.getCarousels()
                .onSuccess { list ->
                    _carousels.value = list.shuffled() // Show carousels on random basis
                }
        }
    }

    private fun updateFilteredSongs(allSongs: List<Song>, query: String) {
        _filteredSongs.value = if (query.isEmpty()) {
            allSongs
        } else {
            allSongs.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.artist.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _participants = MutableStateFlow<List<com.example.ai_music_pro.domain.model.UserProfile>>(emptyList())
    val participants: StateFlow<List<com.example.ai_music_pro.domain.model.UserProfile>> = _participants.asStateFlow()

    private val _queue = MutableStateFlow<List<String>>(emptyList())
    val queue: StateFlow<List<String>> = _queue.asStateFlow()

    private val _hostId = MutableStateFlow<String?>(null)
    val hostId: StateFlow<String?> = _hostId.asStateFlow()

    private val _currentSongId = MutableStateFlow<String?>(null)
    val currentSongId: StateFlow<String?> = _currentSongId.asStateFlow()

    private val _isHost = MutableStateFlow(false)
    val isHost: StateFlow<Boolean> = _isHost.asStateFlow()

    private fun observeSocketEvents() {
        viewModelScope.launch {
            socketManager.roomEvents.collect { event ->
                when (event) {
                    is RoomEvent.RoomJoined -> {
                        _currentRoomId.value = event.roomId
                        // Request the full room state
                        socketManager.getRoomState(event.roomId)
                    }
                    is RoomEvent.RoomStateReceived -> {
                        val data = event.data
                        _hostId.value = data.optString("hostId", "")
                        _currentSongId.value = data.optString("currentSongId", null)
                        
                        // Parse queue
                        val queueArr = data.optJSONArray("queue")
                        val q = mutableListOf<String>()
                        if (queueArr != null) {
                            for (i in 0 until queueArr.length()) q.add(queueArr.getString(i))
                        }
                        _queue.value = q

                        // Parse participants
                        val pArr = data.optJSONArray("participants")
                        val pList = mutableListOf<com.example.ai_music_pro.domain.model.UserProfile>()
                        if (pArr != null) {
                            for (i in 0 until pArr.length()) {
                                val p = pArr.getJSONObject(i)
                                pList.add(com.example.ai_music_pro.domain.model.UserProfile(
                                    _id = p.optString("_id", ""),
                                    name = p.optString("name", "Guest"),
                                    profilePhoto = p.optString("profilePhoto", null)
                                ))
                            }
                        }
                        _participants.value = pList
                    }
                    is RoomEvent.QueueUpdated -> {
                        _queue.value = event.queue
                    }
                    is RoomEvent.UserJoined -> {
                        // Refresh room state to get updated participants list
                        _currentRoomId.value?.let { socketManager.getRoomState(it) }
                    }
                    is RoomEvent.UserLeft -> {
                        _participants.value = _participants.value.filter { it.id != event.userId }
                    }
                    is RoomEvent.UserKicked -> {
                        _participants.value = _participants.value.filter { it.id != event.userId }
                    }
                    else -> {}
                }
            }
        }
    }

    fun requestSong(songId: String) {
        _currentRoomId.value?.let { socketManager.requestSong(it, songId) }
    }

    fun removeQueueItem(songId: String) {
        _currentRoomId.value?.let { socketManager.removeQueueItem(it, songId) }
    }

    fun changeSong(songId: String) {
        _currentRoomId.value?.let { socketManager.changeSong(it, songId) }
    }

    fun kickUser(targetUserId: String) {
        _currentRoomId.value?.let { socketManager.kickUser(it, targetUserId) }
    }

    fun leaveRoom() {
        _currentRoomId.value = null
        _participants.value = emptyList()
        _queue.value = emptyList()
        _hostId.value = null
    }

    fun fetchSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getSongs()
                .onSuccess { response ->
                    _songs.value = response.songs
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Unknown error occurred"
                }
            _isLoading.value = false
        }
    }

    fun joinRoom(roomId: String) {
        socketManager.joinRoom(roomId)
    }

    fun createRoom() {
        socketManager.createRoom()
    }

    fun syncPlay(currentTimeMs: Long, songId: String) {
        _currentRoomId.value?.let { socketManager.play(it, currentTimeMs, songId) }
    }

    fun syncPause(currentTimeMs: Long) {
        _currentRoomId.value?.let { socketManager.pause(it, currentTimeMs) }
    }

    fun syncSeek(currentTimeMs: Long) {
        _currentRoomId.value?.let { socketManager.seek(it, currentTimeMs) }
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.disconnect()
    }
}
