package com.example.ai_music_pro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_music_pro.data.repository.SongRepository
import com.example.ai_music_pro.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateAlbumViewModel @Inject constructor(
    private val repository: SongRepository
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _selectedSongIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedSongIds: StateFlow<Set<String>> = _selectedSongIds.asStateFlow()

    init {
        fetchSongs()
    }

    private fun fetchSongs() {
        viewModelScope.launch {
            repository.getSongs(limit = 100).onSuccess {
                _songs.value = it.songs
            }
        }
    }

    fun toggleSongSelection(songId: String) {
        val current = _selectedSongIds.value.toMutableSet()
        if (current.contains(songId)) {
            current.remove(songId)
        } else {
            current.add(songId)
        }
        _selectedSongIds.value = current
    }

    fun createAlbum(name: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            if (name.isNotEmpty() && _selectedSongIds.value.isNotEmpty()) {
                val albumId = repository.createLocalAlbum(name, _selectedSongIds.value.toList())
                onSuccess(albumId)
            }
        }
    }
}
