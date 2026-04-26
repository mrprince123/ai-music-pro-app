package com.example.ai_music_pro.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai_music_pro.domain.model.LyricLine
import com.example.ai_music_pro.domain.model.Song
import com.example.ai_music_pro.ui.theme.*

@Composable
fun LyricsView(
    song: Song,
    staticLyrics: String?,
    syncedLyrics: List<LyricLine>,
    currentPositionMs: Long,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GlassWhite,
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                0.5.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        GlassBorder,
                        Color.Transparent
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = SpotifyGreen,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            syncedLyrics.isNotEmpty() -> {
                SyncedLyricsContent(
                    song = song,
                    lyrics = syncedLyrics,
                    currentPositionMs = currentPositionMs
                )
            }

            !staticLyrics.isNullOrBlank() -> {
                StaticLyricsContent(song = song, lyrics = staticLyrics)
            }

            else -> {
                NoLyricsContent(song = song)
            }
        }
    }
}

@Composable
private fun SyncedLyricsContent(
    song: Song,
    lyrics: List<LyricLine>,
    currentPositionMs: Long
) {
    val listState = rememberLazyListState()

    // Find the active line index via binary search
    val activeIndex = remember(currentPositionMs, lyrics) {
        if (lyrics.isEmpty()) -1
        else {
            var low = 0
            var high = lyrics.size - 1
            var result = -1
            while (low <= high) {
                val mid = (low + high) / 2
                if (lyrics[mid].timeMs <= currentPositionMs) {
                    result = mid
                    low = mid + 1
                } else {
                    high = mid - 1
                }
            }
            result
        }
    }

    // Auto-scroll to active line
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            listState.animateScrollToItem(
                index = (activeIndex).coerceAtMost(lyrics.size - 1),
                scrollOffset = -200
            )
        }
    }

    Column {
        // Song header
        LyricsHeader(song = song)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(lyrics) { index, line ->
                val isActive = index == activeIndex
                val isPast = index < activeIndex

                val textColor by animateColorAsState(
                    targetValue = when {
                        isActive -> LyricActive
                        isPast -> LyricInactive.copy(alpha = 0.5f)
                        else -> LyricInactive
                    },
                    animationSpec = tween(300),
                    label = "lyricColor"
                )

                Text(
                    text = line.text,
                    color = textColor,
                    fontSize = if (isActive) 22.sp else 18.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    lineHeight = if (isActive) 30.sp else 26.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (isPast) 0.5f else 1f)
                        .padding(vertical = 4.dp)
                )
            }
            // Bottom padding for scroll offset
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun StaticLyricsContent(song: Song, lyrics: String) {
    Column {
        LyricsHeader(song = song)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = lyrics,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 32.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun NoLyricsContent(song: Song) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        LyricsHeader(song = song)

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "♪",
            fontSize = 48.sp,
            color = LyricInactive
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No lyrics available",
            color = LyricInactive,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Lyrics for this song haven't been added yet",
            color = LyricInactive.copy(alpha = 0.5f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun LyricsHeader(song: Song) {
    Text(
        text = song.title,
        color = Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = song.artist,
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 16.sp
    )
}
