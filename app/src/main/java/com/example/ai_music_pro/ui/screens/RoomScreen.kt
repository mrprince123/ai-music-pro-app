package com.example.ai_music_pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai_music_pro.domain.model.Song
import com.example.ai_music_pro.domain.model.UserProfile
import com.example.ai_music_pro.ui.theme.LunkgemBlue
import com.example.ai_music_pro.ui.theme.SpotifyGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(
    roomId: String,
    hostId: String?,
    isHost: Boolean,
    participants: List<UserProfile>,
    queue: List<String>,
    allSongs: List<Song>,
    currentSongId: String?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onChangeSong: (String) -> Unit,
    onAddToQueue: (String) -> Unit,
    onRemoveFromQueue: (String) -> Unit,
    onKickUser: (String) -> Unit,
    onLeaveRoom: () -> Unit,
    onBackClick: () -> Unit
) {
    val currentSong = remember(currentSongId, allSongs) {
        allSongs.find { it._id == currentSongId }
    }
    val queuedSongs = remember(queue, allSongs) {
        queue.mapNotNull { id -> allSongs.find { it._id == id } }
    }
    var showSongPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Room", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = roomId,
                            fontSize = 12.sp,
                            color = SpotifyGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        onLeaveRoom()
                        onBackClick()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Leave", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ─── NOW PLAYING ────────────────────────────
            item {
                NowPlayingCard(
                    song = currentSong,
                    isPlaying = isPlaying,
                    isHost = isHost,
                    onPlayPause = onPlayPause
                )
            }

            // ─── HOST CONTROLS ──────────────────────────
            if (isHost) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showSongPicker = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Song", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ─── QUEUE SECTION ──────────────────────────
            item {
                SectionTitle(title = "Queue", subtitle = "${queuedSongs.size} songs")
            }

            if (queuedSongs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No songs in the queue", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            items(queuedSongs) { song ->
                QueueSongItem(
                    song = song,
                    isHost = isHost,
                    onPlay = { onChangeSong(song._id) },
                    onRemove = { onRemoveFromQueue(song._id) }
                )
            }

            // ─── PARTICIPANTS SECTION ────────────────────
            item {
                SectionTitle(
                    title = "Listening Now",
                    subtitle = "${participants.size} ${if (participants.size == 1) "person" else "people"}"
                )
            }

            items(participants) { user ->
                ParticipantItem(
                    user = user,
                    isHost = isHost,
                    isRoomHost = user.id == hostId,
                    onKick = { onKickUser(user.id) }
                )
            }

            // ─── BROWSE SONGS (any user can request) ────
            if (!isHost) {
                item {
                    SectionTitle(title = "Request a Song", subtitle = "Ask the host to play")
                }
                items(allSongs.take(20)) { song ->
                    RequestSongItem(song = song, onRequest = { onAddToQueue(song._id) })
                }
            }
        }

        // Song Picker Bottom Sheet (host only)
        if (showSongPicker) {
            ModalBottomSheet(
                onDismissRequest = { showSongPicker = false },
                containerColor = Color(0xFF1E1E1E)
            ) {
                Text(
                    "Pick a Song",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn(modifier = Modifier.heightIn(max = 500.dp)) {
                    items(allSongs) { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onChangeSong(song._id)
                                    showSongPicker = false
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = song.coverUrl,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(song.artist, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                            }
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun NowPlayingCard(song: Song?, isPlaying: Boolean, isHost: Boolean, onPlayPause: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(SpotifyGreen.copy(alpha = 0.3f), Color(0xFF1A1A2E))
                )
            )
            .padding(20.dp)
    ) {
        if (song != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                    Text("NOW PLAYING", color = SpotifyGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(song.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, maxLines = 1)
                }
                if (isHost) {
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SpotifyGreen)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                }
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text("NO SONG PLAYING", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Pick a song to start", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun QueueSongItem(song: Song, isHost: Boolean, onPlay: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (isHost) onPlay() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
        }
        if (isHost) {
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun ParticipantItem(user: UserProfile, isHost: Boolean, isRoomHost: Boolean, onKick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.profilePhoto ?: "https://ui-avatars.com/api/?name=${user.name}&background=random",
            contentDescription = null,
            modifier = Modifier.size(44.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(user.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                if (isRoomHost) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = SpotifyGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "HOST",
                            color = SpotifyGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(user.email.ifEmpty { "Listening" }, color = Color.Gray, fontSize = 12.sp)
        }
        if (isHost && !isRoomHost) {
            IconButton(onClick = onKick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.PersonRemove, contentDescription = "Kick", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun RequestSongItem(song: Song, onRequest: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(song.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
        }
        TextButton(onClick = onRequest) {
            Text("Request", color = LunkgemBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
