package com.aimusic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SpotifyDark = Color(0xFF121212)
val SpotifyLightDark = Color(0xFF282828)
val SpotifyGreen = Color(0xFF1DB954)
val SpotifyWhite = Color(0xFFFFFFFF)
val SpotifyGray = Color(0xFFB3B3B3)

private val DarkColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    background = SpotifyDark,
    surface = SpotifyLightDark,
    onPrimary = SpotifyWhite,
    onBackground = SpotifyWhite,
    onSurface = SpotifyWhite,
    secondary = SpotifyGray
)

@Composable
fun AIMusicProTheme(
    content: @Composable () -> Unit
) {
    // Force dark theme as per requirements
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
