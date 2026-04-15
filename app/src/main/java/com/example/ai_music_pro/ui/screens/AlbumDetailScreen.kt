package com.example.ai_music_pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ai_music_pro.ui.components.SongListItem
import com.example.ai_music_pro.ui.components.SongListShimmer
import com.example.ai_music_pro.ui.theme.LunkgemBlue
import com.example.ai_music_pro.ui.theme.SpotifyGreen
import com.example.ai_music_pro.ui.theme.SurfaceGray
import com.example.ai_music_pro.ui.viewmodel.AlbumDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: String,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSongClick: (com.example.ai_music_pro.domain.model.Song) -> Unit = {},
    onDeleteAlbum: () -> Unit = {}
) {
    val album by viewModel.album.collectAsState()
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(album?.name ?: "Album", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteAlbum {
                            onDeleteAlbum()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Album", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading) {
                SongListShimmer()
                return@Box
            }

            val currentAlbum = album

            if (currentAlbum == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Album not found", color = Color.Gray)
                }
                return@Box
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Premium Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    SpotifyGreen.copy(alpha = 0.4f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        ),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Album, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(64.dp))
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Column {
                            Text(text = "ALBUM", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = currentAlbum.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 28.sp, fontWeight = FontWeight.Black, lineHeight = 32.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "${songs.size} songs", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(songs) { song ->
                        SongListItem(song = song, onClick = { onSongClick(song) })
                    }
                    item { Spacer(modifier = Modifier.height(120.dp)) }
                }
            }

            if (error != null) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = {
                        TextButton(onClick = { }) {
                        Text(text = "OK", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                ) {
                    Text(text = error ?: "Unknown error", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
