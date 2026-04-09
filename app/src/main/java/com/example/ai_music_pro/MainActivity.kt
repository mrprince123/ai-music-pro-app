package com.example.ai_music_pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ai_music_pro.ui.theme.AiMusicProTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mock data to preview the UI without the backend connected
        val mockSongs = listOf(
            Song("1", "Blinding Lights", "The Weeknd", 200, "", "", "Pop"),
            Song("2", "Starboy", "The Weeknd", 230, "", "", "Pop")
        )

        setContent {
            AIMusicProTheme {
                var showSplash by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }

                if (showSplash) {
                    com.aimusic.ui.screens.SplashScreen(onSplashComplete = { showSplash = false })
                } else {
                    // Stack layout with MiniPlayer anchored at the bottom
                    Box(modifier = Modifier.fillMaxSize()) {
                        HomeScreen(
                            recentSongs = mockSongs,
                            trendingSongs = mockSongs,
                            onSongClick = { /* Handle Click */ }
                        )

                        // Fixed Mini Player at the bottom
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                        ) {
                            MiniPlayer(
                                song = mockSongs.first(),
                                isPlaying = false,
                                progress = 0.3f,
                                onPlayPauseClick = { },
                                onExpandClick = { }
                            )
                        }
                    }
                }
            }
        }
    }
}
