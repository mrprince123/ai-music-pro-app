package com.example.ai_music_pro.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.ai_music_pro.domain.model.AudioOutputDevice
import com.example.ai_music_pro.domain.model.LyricLine
import com.example.ai_music_pro.domain.model.Song
import com.example.ai_music_pro.ui.components.AudioOutputSheet
import com.example.ai_music_pro.ui.components.LyricsView
import com.example.ai_music_pro.ui.theme.*
import androidx.compose.material.icons.automirrored.filled.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    song: Song?,
    isPlaying: Boolean,
    progress: Float,
    elapsedTime: String,
    totalTime: String,
    onPlayPauseClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onCloseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    participants: List<com.example.ai_music_pro.domain.model.UserProfile> = emptyList(),
    queue: List<String> = emptyList(),
    allSongs: List<com.example.ai_music_pro.domain.model.Song> = emptyList(),
    onRemoveQueueItem: (String) -> Unit = {},
    onLikeClick: (String) -> Unit = {},
    // New: Lyrics support
    syncedLyrics: List<LyricLine> = emptyList(),
    staticLyrics: String? = null,
    lyricsLoading: Boolean = false,
    currentPositionMs: Long = 0L,
    // New: Audio output support
    availableDevices: List<AudioOutputDevice> = emptyList(),
    activeDevice: AudioOutputDevice? = null,
    resumeAfterSwitch: Boolean = false,
    onDeviceSwitch: (AudioOutputDevice) -> Unit = {},
    onResumeToggle: (Boolean) -> Unit = {}
) {
    if (song == null) return

    var showLyrics by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showDeviceSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PlayerBackground)
    ) {
        // Ambient glow from album art (blurred background layer)
        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(120.dp)
                .scale(1.3f)
        )
        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.85f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.PaddingDefault)
        ) {
            // Top Bar
            PlayerTopBar(
                onCloseClick = onCloseClick,
                showLyrics = showLyrics,
                onLyricsToggle = { showLyrics = it },
                onDevicesClick = { showDeviceSheet = true },
                activeDeviceName = activeDevice?.name
            )

            // Main Centered Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showLyrics) {
                    LyricsView(
                        song = song,
                        staticLyrics = staticLyrics,
                        syncedLyrics = syncedLyrics,
                        currentPositionMs = currentPositionMs,
                        isLoading = lyricsLoading,
                        modifier = Modifier.padding(vertical = Dimens.PaddingDefault)
                    )
                } else {
                    // Artwork with scale animation
                    val artworkScale by animateFloatAsState(
                        targetValue = if (isPlaying) 1f else 0.88f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "artworkScale"
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Album art with ambient glow ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Subtle glow ring behind art
                        if (isPlaying) {
                            val pulseAlpha by rememberInfiniteTransition(label = "pulse").animateFloat(
                                initialValue = 0.15f,
                                targetValue = 0.35f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(2000),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "glowPulse"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.88f)
                                    .aspectRatio(1f)
                                    .scale(1.05f)
                                    .clip(RoundedCornerShape(Dimens.RadiusExtraLarge))
                                    .background(SpotifyGreen.copy(alpha = pulseAlpha))
                            )
                        }

                        AsyncImage(
                            model = song.coverUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(1f)
                                .scale(artworkScale)
                                .clip(RoundedCornerShape(Dimens.RadiusExtraLarge))
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Song Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = song.artist,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { onLikeClick(song._id) }) {
                            Icon(
                                imageVector = if (song.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (song.isLiked) Color.Red else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Controls
                    PlayerControls(
                        isPlaying = isPlaying,
                        progress = progress,
                        elapsedTime = elapsedTime,
                        totalTime = totalTime,
                        onPlayPauseClick = onPlayPauseClick,
                        onSeek = onSeek,
                        onPreviousClick = onPreviousClick,
                        onNextClick = onNextClick,
                        onQueueClick = { showQueueSheet = true }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Device pill
                    if (activeDevice != null) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { showDeviceSheet = true },
                            color = SpotifyGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speaker,
                                    contentDescription = null,
                                    tint = SpotifyGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = activeDevice.name,
                                    color = SpotifyGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Queue Sheet
        if (showQueueSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQueueSheet = false },
                dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) },
                containerColor = Color(0xE6181818),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                RoomQueueContent(
                    participants = participants,
                    queue = queue,
                    allSongs = allSongs,
                    onRemoveItem = onRemoveQueueItem
                )
            }
        }

        // Device Sheet
        if (showDeviceSheet) {
            AudioOutputSheet(
                availableDevices = availableDevices,
                activeDevice = activeDevice,
                resumeAfterSwitch = resumeAfterSwitch,
                onDeviceSelect = { device ->
                    onDeviceSwitch(device)
                    showDeviceSheet = false
                },
                onResumeToggle = onResumeToggle,
                onDismiss = { showDeviceSheet = false }
            )
        }
    }
}

@Composable
fun PlayerTopBar(
    onCloseClick: () -> Unit,
    showLyrics: Boolean,
    onLyricsToggle: (Boolean) -> Unit,
    onDevicesClick: () -> Unit = {},
    activeDeviceName: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = Dimens.PaddingDefault),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCloseClick) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(Dimens.IconSizeLarge)
            )
        }

        Text(
            text = if (showLyrics) "Lyrics" else "Now Playing",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row {
            // Devices button
            IconButton(onClick = onDevicesClick) {
                Icon(
                    imageVector = Icons.Default.DevicesOther,
                    contentDescription = "Audio Output",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
            }
            // Lyrics toggle
            IconButton(onClick = { onLyricsToggle(!showLyrics) }) {
                Icon(
                    imageVector = if (showLyrics) Icons.Default.Album else Icons.Default.Lyrics,
                    contentDescription = "Toggle Lyrics",
                    tint = if (showLyrics) SpotifyGreen else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    progress: Float,
    elapsedTime: String,
    totalTime: String,
    onPlayPauseClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onQueueClick: () -> Unit,
    isLiked: Boolean = false,
    onLikeClick: () -> Unit = {}
) {
    Column {
        // Progress slider
        Slider(
            value = progress,
            onValueChange = onSeek,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = SpotifyGreen,
                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = elapsedTime,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
            Text(
                text = totalTime,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Playback controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Shuffle */ }) {
                Icon(
                    Icons.Default.Shuffle,
                    null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(onClick = onPreviousClick) {
                Icon(
                    Icons.Default.SkipPrevious,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Play/Pause button
            Surface(
                modifier = Modifier
                    .size(68.dp)
                    .clickable { onPlayPauseClick() },
                shape = CircleShape,
                color = SpotifyGreen
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AnimatedContent(
                        targetState = isPlaying,
                        transitionSpec = {
                            scaleIn(animationSpec = tween(300)) + fadeIn() togetherWith
                            scaleOut(animationSpec = tween(300)) + fadeOut()
                        },
                        label = "playPauseMain"
                    ) { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onNextClick) {
                Icon(
                    Icons.Default.SkipNext,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            IconButton(onClick = onQueueClick) {
                Icon(
                    Icons.Default.QueueMusic,
                    null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun RoomQueueContent(
    participants: List<com.example.ai_music_pro.domain.model.UserProfile>,
    queue: List<String>,
    allSongs: List<com.example.ai_music_pro.domain.model.Song>,
    onRemoveItem: (String) -> Unit
) {
    val queuedSongs = remember(queue, allSongs) {
        queue.mapNotNull { id -> allSongs.find { it._id == id } }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            "Listening Now",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Row(modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth()) {
            participants.forEach { user ->
                AsyncImage(
                    model = user.profilePhoto ?: "https://ui-avatars.com/api/?name=${user.name}",
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape).padding(4.dp)
                )
            }
            if (participants.isEmpty()) Text(
                "No other users joined yet",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
        
        HorizontalDivider(
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Text(
            "Queue",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
            items(queuedSongs) { song ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = song.coverUrl,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                    )
                    Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                        Text(
                            song.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            song.artist,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = { onRemoveItem(song._id) }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
            if (queuedSongs.isEmpty()) {
                item {
                    Text(
                        "Nothing in the queue",
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
