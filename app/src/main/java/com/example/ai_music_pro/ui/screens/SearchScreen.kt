package com.example.ai_music_pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ai_music_pro.ui.components.AppInputField
import com.example.ai_music_pro.ui.components.SectionHeader
import com.example.ai_music_pro.ui.components.SongListItem
import com.example.ai_music_pro.ui.components.SongListShimmer
import com.example.ai_music_pro.ui.theme.Dimens
import com.example.ai_music_pro.ui.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onSongClick: (com.example.ai_music_pro.domain.model.Song) -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.PaddingDefault)
    ) {
        Spacer(modifier = Modifier.height(Dimens.PaddingDefault))
        Text(
            text = "Search",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = Dimens.PaddingDefault)
        )

        // Search Bar (New Rounded Component)
        AppInputField(
            value = searchQuery,
            onValueChange = { viewModel.onQueryChange(it) },
            placeholder = "What do you want to listen to?",
            leadingIcon = Icons.Default.Search
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (searchQuery.isEmpty()) {
                if (searchHistory.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Recent searches", showSeeAll = false, onSeeAllClick = {})
                    }
                    items(searchHistory) { history ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onQueryChange(history) }
                                .padding(vertical = Dimens.PaddingSmall),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.width(Dimens.PaddingDefault))
                            Text(text = history, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.removeSearchHistory(history) }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            } else {
                if (isSearching) {
                    item {
                        SongListShimmer()
                    }
                } else if (searchResults.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(text = "No results found for \"$searchQuery\"", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                } else {
                    item {
                        SectionHeader(title = "Songs", showSeeAll = false, onSeeAllClick = {})
                    }
                    items(searchResults) { song ->
                        SongListItem(song = song, onClick = { 
                            viewModel.onSearchExecute(searchQuery)
                            onSongClick(song) 
                        })
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(120.dp)) }
        }
    }
}
