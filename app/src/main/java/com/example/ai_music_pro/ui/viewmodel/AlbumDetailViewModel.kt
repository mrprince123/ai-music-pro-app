package com.example.ai_music_pro.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_music_pro.data.repository.SongRepository
import com.example.ai_music_pro.domain.model.Song
import com.example.ai_music_pro.data.local.entities.LocalAlbumEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val repository: SongRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val albumId: String = savedStateHandle.get<String>("albumId") ?: ""

    private val _album = MutableStateFlow<LocalAlbumEntity?>(null)
    val album: StateFlow<LocalAlbumEntity?> = _album.asStateFlow()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        if (albumId.isNotBlank()) {
            loadAlbum()
        }
    }

    private fun loadAlbum() {
        viewModelScope.launch {
            _isLoading.value = true
            _album.value = repository.getLocalAlbum(albumId)
            _album.value?.let { loadSongs(it) }
            _isLoading.value = false
        }
    }

    private suspend fun loadSongs(album: LocalAlbumEntity) {
        val songIds = album.songIds.split(",").mapNotNull { it.trim().takeIf(String::isNotEmpty) }
        repository.getSongsByIds(songIds).onSuccess { songs ->
            _songs.value = songs
        }.onFailure {
            _error.value = it.message ?: "Unable to load album songs"
        }
    }

    fun deleteAlbum(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteLocalAlbum(albumId)
            onDeleted()
        }
    }
}
