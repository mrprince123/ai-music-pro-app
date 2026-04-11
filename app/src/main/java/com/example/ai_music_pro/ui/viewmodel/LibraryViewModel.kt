package com.example.ai_music_pro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_music_pro.data.local.entities.LocalAlbumEntity
import com.example.ai_music_pro.data.repository.SongRepository
import com.example.ai_music_pro.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: SongRepository
) : ViewModel() {

    val likedSongs: StateFlow<List<Song>> = repository.getLikedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val localAlbums: StateFlow<List<LocalAlbumEntity>> = repository.getLocalAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
