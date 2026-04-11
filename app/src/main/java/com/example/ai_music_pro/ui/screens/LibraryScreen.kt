package com.example.ai_music_pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ai_music_pro.ui.components.SongListItem
import com.example.ai_music_pro.ui.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    onSongClick: (com.example.ai_music_pro.domain.model.Song) -> Unit = {},
    onAlbumClick: (String) -> Unit = {}
) {
    val likedSongs by viewModel.likedSongs.collectAsState()
    val localAlbums by viewModel.localAlbums.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Library",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color.White
                )
            },
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Liked Songs") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Albums") }
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (selectedTab == 0) {
                if (likedSongs.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Songs you like will appear here", color = Color.Gray)
                        }
                    }
                } else {
                    items(likedSongs) { song ->
                        SongListItem(song = song, onClick = { onSongClick(song) })
                    }
                }
            } else {
                if (localAlbums.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(text = "No local albums created yet", color = Color.Gray)
                        }
                    }
                } else {
                    items(localAlbums) { album ->
                        ListItem(
                            headlineContent = { Text(album.name, color = Color.White) },
                            supportingContent = { Text("${album.songIds.split(",").size} songs", color = Color.Gray) },
                            modifier = Modifier
                                .background(Color.Transparent)
                                .clickable { onAlbumClick(album.id) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
            // Bottom padding for player
            item { Spacer(modifier = Modifier.height(120.dp)) }
        }
    }
}
