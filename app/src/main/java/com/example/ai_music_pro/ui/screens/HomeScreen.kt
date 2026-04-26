package com.example.ai_music_pro.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ai_music_pro.ui.theme.SpotifyGreen
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    allSongs: List<Song>,
    filteredSongs: List<Song>,
    carousels: List<CarouselItem>,
    categories: List<String>,
    onSongClick: (Song) -> Unit,
    onJoinRoom: (String) -> Unit,
    onCreateRoom: () -> Unit,
    onCreateMusicClick: () -> Unit,
    onQuickAccessClick: (String) -> Unit,
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
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }
    
    val popularSongs = remember(allSongs) { allSongs.shuffled().take(8) }
    val newReleases = remember(allSongs) { allSongs.reversed().take(8) }
    val recommended = remember(allSongs) { allSongs.take(8) }
    val recentMix = remember(allSongs) { allSongs.shuffled().take(6) }

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

    if (selectedCategory != null) {
        val categorySongs = when (selectedCategory) {
            "Popular Today" -> allSongs.shuffled()
            "New Release" -> allSongs.reversed()
            "Recommended for you" -> allSongs
            else -> allSongs.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
        CategorySongsScreen(
            categoryName = selectedCategory!!,
            songs = categorySongs,
            onSongClick = onSongClick,
            onBackClick = { selectedCategory = null },
            onLikeClick = onLikeClick
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = refreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ─── Top Bar ───
                    item {
                        SpotifyTopBar(
                            onSyncClick = { showRoomDialog = true },
                            onSettingsClick = onSettingsClick,
                            currentRoomId = currentRoomId,
                            onCreateMusicClick = onCreateMusicClick
                        )
                    }

                    // ─── Filter Chips ───
                    item {
                        val filters = listOf("All", "Music", "Podcasts", "Artists")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            items(filters) { filter ->
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter },
                                    label = {
                                        Text(
                                            text = filter,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SpotifyGreen,
                                        selectedLabelColor = Color.Black,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = Color.Transparent,
                                        selectedBorderColor = Color.Transparent,
                                        enabled = true,
                                        selected = selectedFilter == filter
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }
                    }

                    if (isInitialLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxHeight(0.7f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = SpotifyGreen)
                            }
                        }
                    } else if (allSongs.isEmpty() && !isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxHeight(0.7f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No songs available",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        // ─── Quick Access Grid (4 fixed items only) ───
                        item {
                            SpotifyQuickAccessGrid(
                                items = listOf(
                                    QuickAccessItem("Recently Played", Icons.Default.History, Color(0xFF4CAF50)),
                                    QuickAccessItem("Liked Songs", Icons.Default.Favorite, Color(0xFFE91E63)),
                                    QuickAccessItem("Your Albums", Icons.Default.LibraryMusic, Color(0xFF2196F3)),
                                    QuickAccessItem("Trending", Icons.Default.TrendingUp, Color(0xFFFF9800))
                                ),
                                onItemClick = onQuickAccessClick
                            )
                        }

                        // ─── Featured Banner ───
                        item {
                            BannerSection(carousels = carousels)
                        }

                        // ─── Made For You ───
                        item {
                            SectionHeader(title = "Made For You", onSeeAllClick = { selectedCategory = "Recommended for you" })
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(recommended) { song ->
                                    SpotifyCompactCard(song = song, onClick = { onSongClick(song) })
                                }
                            }
                        }

                        // ─── Browse Genres ───
                        item {
                            Text(
                                text = "Browse All",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 12.dp)
                            )
                            SpotifyGenreGrid(
                                categories = categories.ifEmpty { listOf("Pop", "Rock", "Classical", "Jazz", "Hip Hop", "Electronic") },
                                onCategoryClick = { selectedCategory = it }
                            )
                        }

                        // ─── Popular Today ───
                        item {
                            SectionHeader(title = "Popular Today", onSeeAllClick = { selectedCategory = "Popular Today" })
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(popularSongs) { song ->
                                    PlaylistCard(song = song, onClick = { onSongClick(song) }, onLikeClick = onLikeClick)
                                }
                            }
                        }

                        // ─── New Releases ───
                        item {
                            SectionHeader(title = "New Releases", onSeeAllClick = { selectedCategory = "New Release" })
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(newReleases) { song ->
                                    PlaylistCard(song = song, onClick = { onSongClick(song) }, onLikeClick = onLikeClick)
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(120.dp)) }
                }
            }
        }
    }
}

// ─── Spotify-Style Top Bar ───
@Composable
fun SpotifyTopBar(
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    currentRoomId: String?,
    onCreateMusicClick: () -> Unit
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 6 -> "Good Night"
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            hour < 21 -> "Good evening"
            else -> "Good night"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 8.dp)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Greeting
            Text(
                text = greeting,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // Right actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentRoomId != null) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SpotifyGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            color = SpotifyGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                IconButton(onClick = onCreateMusicClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onSyncClick) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Room",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ─── Quick Access Grid (Spotify 2x3 layout with album art) ───
@Composable
fun SpotifyQuickAccessGrid(
    items: List<QuickAccessItem>,
    recentSongs: List<Song>,
    onItemClick: (String) -> Unit,
    onSongClick: (Song) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
        // First: Quick access items as compact cards (2 columns)
        val rows = items.chunked(2)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable { onItemClick(item.title) },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Colored icon strip
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(56.dp)
                                    .background(item.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = item.color,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = item.title,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Then: Recent songs as Spotify-style compact album rows (2 col)
        if (recentSongs.isNotEmpty()) {
            val recentPairs = recentSongs.take(6).chunked(2)
            recentPairs.forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pair.forEach { song ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clickable { onSongClick(song) },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = song.coverUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(56.dp)
                                        .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                                )
                                Text(
                                    text = song.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )
                            }
                        }
                    }
                    // If odd number, fill the remaining space
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ─── Spotify Compact Card (vertical album art + title) ───
@Composable
fun SpotifyCompactCard(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song.title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─── Spotify Genre Grid (colored cards with genre name) ───
@Composable
fun SpotifyGenreGrid(categories: List<String>, onCategoryClick: (String) -> Unit) {
    val genreColors = listOf(
        Color(0xFFE13300), Color(0xFF1E3264), Color(0xFF8D67AB),
        Color(0xFFE8115B), Color(0xFF148A08), Color(0xFFE91429),
        Color(0xFFDC148C), Color(0xFF537AA1), Color(0xFF056952),
        Color(0xFFBA5D07), Color(0xFF477D95), Color(0xFF503750)
    )

    // Non-scrollable grid — embedded inside LazyColumn item
    val rows = categories.chunked(2)
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEachIndexed { index, genre ->
                    val colorIdx = (categories.indexOf(genre)) % genreColors.size
                    val color = genreColors[if (colorIdx < 0) 0 else colorIdx]
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clickable { onCategoryClick(genre) },
                        color = color,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Text(
                                text = genre,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
