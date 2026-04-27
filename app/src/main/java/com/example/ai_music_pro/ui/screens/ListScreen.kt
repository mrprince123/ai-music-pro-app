package com.example.ai_music_pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai_music_pro.domain.model.Song
import com.example.ai_music_pro.ui.components.SongListItem
import com.example.ai_music_pro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    title: String,
    songs: List<Song>,
    onBackClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onLikeClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Custom Header like Your Library
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = com.example.ai_music_pro.ui.theme.SpotifyGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Optional subtle green indicator/line to match "tone"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(com.example.ai_music_pro.ui.theme.SpotifyGreen, Color.Transparent)
                            )
                        )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = Dimens.PaddingDefault, vertical = Dimens.PaddingSmall)
        ) {
            if (songs.isEmpty()) {
                item {
                    Text("No songs found here.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
            items(songs) { song ->
                SongListItem(song = song, onClick = { onSongClick(song) }, onLikeClick = onLikeClick)
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}
