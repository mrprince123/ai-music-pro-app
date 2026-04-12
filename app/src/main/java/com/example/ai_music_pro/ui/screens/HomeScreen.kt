package com.example.ai_music_pro.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai_music_pro.R
import com.example.ai_music_pro.domain.model.CarouselItem
import com.example.ai_music_pro.domain.model.Song
import com.example.ai_music_pro.domain.model.QuickAccessItem
import com.example.ai_music_pro.ui.components.*
import com.example.ai_music_pro.ui.theme.Dimens
import com.example.ai_music_pro.ui.theme.LunkgemBlue
import com.example.ai_music_pro.ui.theme.SurfaceGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    allSongs: List<Song>,
    filteredSongs: List<Song>,
    carousels: List<CarouselItem>,
    onSongClick: (Song) -> Unit,
    onJoinRoom: (String) -> Unit,
    onCreateRoom: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSettingsClick: () -> Unit = {},
    currentRoomId: String? = null,
    isLoading: Boolean = false,
    onAddToQueue: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
    onLikeClick: (String) -> Unit = {}
) {
    var showRoomDialog by remember { mutableStateOf(false) }
    var showAllView by remember { mutableStateOf(false) }
    
    val popularSongs = remember(allSongs) { allSongs.shuffled().take(6) }
    val newReleases = remember(allSongs) { allSongs.reversed().take(6) }
    val recommended = remember(allSongs) { allSongs.take(6) }

    val refreshState = rememberPullToRefreshState()
    
    val isRefreshing = isLoading && allSongs.isNotEmpty()
    val isInitialLoading = isLoading && allSongs.isEmpty()

    if (showRoomDialog) {
        RoomJoinDialog(
            onDismiss = { showRoomDialog = false },
            onJoin = { roomId ->
                onJoinRoom(roomId)
                showRoomDialog = false
            },
            onCreate = {
                onCreateRoom()
                showRoomDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F0F0F), Color(0xFF000000))
                )
            )
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = refreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    HeaderSection(
                        onSyncClick = { showRoomDialog = true },
                        onSettingsClick = onSettingsClick,
                        currentRoomId = currentRoomId,
                        onBackClick = if (showAllView) { { showAllView = false } } else null
                    )
                }

                if (isInitialLoading) {
                    item { HomeShimmer() }
                } else {
                    // Search Bar
                    if (!showAllView) {
                        item {
                            AppInputField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                placeholder = "Search music...",
                                leadingIcon = Icons.Default.Search,
                                modifier = Modifier.padding(horizontal = Dimens.PaddingDefault, vertical = Dimens.PaddingSmall)
                            )
                        }
                    }

                    // Content Logic
                    if (showAllView || searchQuery.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = if (searchQuery.isNotEmpty()) "Search Results" else "All Songs",
                                showSeeAll = false,
                                onSeeAllClick = { }
                            )
                        }
                        items(filteredSongs) { song ->
                            SongListItem(song = song, onClick = { onSongClick(song) }, onLikeClick = onLikeClick)
                        }
                    } else {
                        // Default Home Content
                        item {
                            QuickAccessSection(
                                items = listOf(
                                    QuickAccessItem("Recently Played", Icons.Default.Refresh, Color(0xFF1DB954)),
                                    QuickAccessItem("Liked Songs", Icons.Default.Favorite, Color(0xFF509BF5)),
                                    QuickAccessItem("Your Albums", Icons.Default.MusicNote, Color(0xFFF57340)),
                                    QuickAccessItem("Trending", Icons.Default.Star, Color(0xFFE91E63))
                                ),
                                onItemClick = { onSearchQueryChange(it) }
                            )
                        }

                        item { BannerSection(carousels) }

                        item {
                            Text(
                                text = "Mood & Genres",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(16.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val categories = listOf("Classic", "Romantic", "Rock", "Soul", "Pop")
                                items(categories) { category ->
                                    CategoryItem(name = category, onClick = { onSearchQueryChange(category) })
                                }
                            }
                        }

                        item {
                            SectionHeader(title = "Popular Today", onSeeAllClick = { })
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(popularSongs) { song ->
                                    PlaylistCard(song = song, onClick = { onSongClick(song) }, onLikeClick = onLikeClick)
                                }
                            }
                        }

                        item {
                            SectionHeader(title = "New Release", onSeeAllClick = { })
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(newReleases) { song ->
                                    PlaylistCard(song = song, onClick = { onSongClick(song) }, onLikeClick = onLikeClick)
                                }
                            }
                        }

                        item {
                            SectionHeader(title = "Recommended for you", onSeeAllClick = { })
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(recommended) { song ->
                                    PlaylistCard(song = song, onClick = { onSongClick(song) }, onLikeClick = onLikeClick)
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun CategoryItem(name: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(LunkgemBlue.copy(alpha = 0.1f))
                .border(2.dp, LunkgemBlue.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = LunkgemBlue,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = name, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HeaderSection(
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    currentRoomId: String?,
    onBackClick: (() -> Unit)? = null
) {
    Surface(
        color = Color.Black.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.1f), Color.Transparent))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
            } else {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(Dimens.IconSizeMedium))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(Dimens.RadiusSmall))
                )
                Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                Text(text = "AI Music Pro", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentRoomId != null) {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LunkgemBlue.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "ROOM: $currentRoomId", color = LunkgemBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = onSyncClick) {
                    Icon(Icons.Default.Share, contentDescription = "Join Room", tint = LunkgemBlue, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
fun QuickAccessSection(items: List<QuickAccessItem>, onItemClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingSmall)) {
        val rows = items.chunked(2)
        rows.forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { item ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(Dimens.PaddingSmall / 2)
                            .height(56.dp)
                            .clickable { onItemClick(item.title) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(Dimens.RadiusSmall),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.1f), Color.Transparent))
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.fillMaxHeight().width(56.dp).background(item.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(Dimens.IconSizeMedium))
                            }
                            Text(
                                text = item.title,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
