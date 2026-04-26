package com.example.ai_music_pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ai_music_pro.domain.model.Song
import com.example.ai_music_pro.ui.components.SongListItem
import com.example.ai_music_pro.ui.components.SectionHeader
import com.example.ai_music_pro.ui.components.HeaderSection

@Composable
fun CategorySongsScreen(
    categoryName: String,
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onBackClick: () -> Unit,
    onLikeClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HeaderSection(
            onSyncClick = {},
            onSettingsClick = {},
            currentRoomId = null,
            onCreateMusicClick = {},
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                SectionHeader(
                    title = "$categoryName Hits",
                    showSeeAll = false,
                    onSeeAllClick = {}
                )
            }

            items(songs) { song ->
                SongListItem(
                    song = song,
                    onClick = { onSongClick(song) },
                    onLikeClick = onLikeClick
                )
            }
        }
    }
}
