package com.example.ai_music_pro.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import com.example.ai_music_pro.domain.model.Song
import com.example.ai_music_pro.ui.theme.Dimens
import com.example.ai_music_pro.ui.theme.LunkgemBlue
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
    onNextClick: () -> Unit
) {
    if (song == null) return

    var showLyrics by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main Content (Artwork or Lyrics)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.PaddingDefault)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            PlayerTopBar(onCloseClick = onCloseClick, showLyrics = showLyrics, onLyricsToggle = { showLyrics = it })

            if (showLyrics) {
                LyricsSection(song = song)
            } else {
                Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
                // Artwork
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(Dimens.RadiusExtraLarge))
                )
                Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
                // Info
                Text(
                    text = song.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = song.artist,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Padding for the fixed controls
            Spacer(modifier = Modifier.height(200.dp))
        }

        // Persistent Player Controls at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                    )
                )
                .padding(Dimens.PaddingDefault)
        ) {
            PlayerControls(
                isPlaying = isPlaying,
                progress = progress,
                elapsedTime = elapsedTime,
                totalTime = totalTime,
                onPlayPauseClick = onPlayPauseClick,
                onSeek = onSeek,
                onPreviousClick = onPreviousClick,
                onNextClick = onNextClick
            )
        }
    }
}

@Composable
fun PlayerTopBar(onCloseClick: () -> Unit, showLyrics: Boolean, onLyricsToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.PaddingDefault),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCloseClick) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(Dimens.IconSizeLarge))
        }

        Text(
            text = if (showLyrics) "Lyrics" else "Playing from Album",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        IconButton(onClick = { onLyricsToggle(!showLyrics) }) {
            Icon(
                imageVector = if (showLyrics) Icons.Default.Star else Icons.Default.Menu,
                contentDescription = null,
                tint = if (showLyrics) LunkgemBlue else MaterialTheme.colorScheme.onSurface
            )
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
    onNextClick: () -> Unit
) {
    Column {
        Slider(
            value = progress,
            onValueChange = onSeek,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onSurface,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = elapsedTime, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
            Text(text = totalTime, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) { Icon(Icons.Default.List, null, tint = MaterialTheme.colorScheme.onSurface) }
            IconButton(onClick = onPreviousClick) { Icon(Icons.Default.SkipPrevious, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(Dimens.IconSizeLarge)) }
            
            Surface(
                modifier = Modifier.size(72.dp).clickable { onPlayPauseClick() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onSurface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            IconButton(onClick = onNextClick) { Icon(Icons.Default.SkipNext, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(Dimens.IconSizeLarge)) }
            IconButton(onClick = { }) { Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.onSurface) }
        }
    }
}

@Composable
fun LyricsSection(song: Song) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.PaddingDefault),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = song.title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = song.artist,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "I don't relate to you, no\n\n'Cause I'd never treat me this shitty\n\nYou made me feel like it was my fault, you were the victim...\n\nYou're the target, I'm the one who's bruised\n\nI'm the one who's always being used...",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 36.sp
        )
    }
}
