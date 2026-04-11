package com.example.ai_music_pro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_music_pro.data.repository.SongRepository
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

    private fun observeSocketEvents() {
        viewModelScope.launch {
            socketManager.roomEvents.collect { event ->
                when (event) {
                    is RoomEvent.RoomJoined -> _currentRoomId.value = event.roomId
                    else -> {}
                }
            }
        }
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
