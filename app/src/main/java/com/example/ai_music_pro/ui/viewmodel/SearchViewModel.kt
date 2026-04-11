package com.example.ai_music_pro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_music_pro.data.repository.SongRepository
import com.example.ai_music_pro.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SongRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    val searchHistory: StateFlow<List<String>> = repository.getSearchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        setupSearchDebounce()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .filter { it.length >= 2 }
                .distinctUntilChanged()
                .onEach { _isSearching.value = true }
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    private suspend fun performSearch(query: String) {
        val result = repository.getSongs(limit = 50) // For now use global list and filter locally or use a real search API if available
        result.onSuccess { response ->
            _searchResults.value = response.songs.filter { 
                it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
            }
        }.onFailure {
            _searchResults.value = emptyList()
        }
        _isSearching.value = false
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
        }
    }

    fun onSearchExecute(query: String) {
        viewModelScope.launch {
            repository.addSearchQuery(query)
            performSearch(query)
        }
    }

    fun removeSearchHistory(query: String) {
        viewModelScope.launch {
            repository.deleteSearchQuery(query)
        }
    }
}
