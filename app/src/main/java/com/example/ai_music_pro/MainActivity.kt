package com.example.ai_music_pro

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.ai_music_pro.domain.model.Song
import com.example.ai_music_pro.service.ExoPlayerService
import com.example.ai_music_pro.ui.navigation.Screen
import com.example.ai_music_pro.ui.navigation.bottomNavItems
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.ai_music_pro.ui.screens.*
import com.example.ai_music_pro.ui.viewmodel.SongViewModel
import com.example.ai_music_pro.ui.viewmodel.ThemeMode
import com.example.ai_music_pro.ui.state.PlaybackState
import com.example.ai_music_pro.ui.theme.AIMusicProTheme
import com.example.ai_music_pro.ui.components.MiniPlayer
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val songViewModel: SongViewModel by viewModels()
    private val themeViewModel: com.example.ai_music_pro.ui.viewmodel.ThemeViewModel by viewModels()
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeMode by themeViewModel.themeMode.collectAsState()
            AIMusicProTheme(themeMode = themeMode) {
                val songs by songViewModel.songs.collectAsState()
                val filteredSongs by songViewModel.filteredSongs.collectAsState()
                val searchQuery by songViewModel.searchQuery.collectAsState()
                val currentRoomId by songViewModel.currentRoomId.collectAsState()
                
                var showSplash by rememberSaveable { mutableStateOf(true) }
                var showPlayer by remember { mutableStateOf(false) }
                
                // Playback State derived from MediaController
                var playbackState by remember { mutableStateOf(PlaybackState()) }

                // Initial connection & Listener setup
                LaunchedEffect(Unit) {
                    val sessionToken = SessionToken(this@MainActivity, ComponentName(this@MainActivity, ExoPlayerService::class.java))
                    controllerFuture = MediaController.Builder(this@MainActivity, sessionToken).buildAsync()
                    controllerFuture?.addListener({
                        controller = controllerFuture?.get()
                        controller?.addListener(object : Player.Listener {
                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                playbackState = playbackState.copy(isPlaying = isPlaying)
                            }
                            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                                val currentSong = songs.find { it.songUrl == mediaItem?.localConfiguration?.uri.toString() }
                                playbackState = playbackState.copy(currentSong = currentSong)
                            }
                            override fun onPlaybackStateChanged(state: Int) {
                                playbackState = playbackState.copy(
                                    isReady = state == Player.STATE_READY,
                                    duration = controller?.duration?.coerceAtLeast(0L) ?: 0L
                                )
                            }
                        })
                    }, MoreExecutors.directExecutor())
                }

                // Progress Update Polling
                LaunchedEffect(playbackState.isPlaying) {
                    while (playbackState.isPlaying && isActive) {
                        playbackState = playbackState.copy(
                            currentPosition = controller?.currentPosition ?: 0L,
                            duration = controller?.duration?.coerceAtLeast(0L) ?: 0L
                        )
                        delay(1000)
                    }
                }

                if (showSplash) {
                    SplashScreen(onSplashComplete = { showSplash = false })
                } else {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    Scaffold(
                        bottomBar = {
                            if (!showPlayer) {
                                NavigationBar(
                                    containerColor = Color.White.copy(alpha = 0.05f),
                                    tonalElevation = 0.dp,
                                    modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                ) {
                                    bottomNavItems.forEach { screen ->
                                        NavigationBarItem(
                                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                                            label = { Text(screen.title, fontSize = 10.sp) },
                                            selected = currentRoute == screen.route,
                                            onClick = {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                unselectedIconColor = Color.White.copy(alpha = 0.4f),
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                unselectedTextColor = Color.White.copy(alpha = 0.4f),
                                                indicatorColor = Color.Transparent
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                            NavHost(navController = navController, startDestination = Screen.Home.route) {
                                composable(Screen.Home.route) {
                                    HomeScreen(
                                        allSongs = songs,
                                        filteredSongs = filteredSongs,
                                        searchQuery = searchQuery,
                                        onSearchQueryChange = { songViewModel.setSearchQuery(it) },
                                        onSettingsClick = { navController.navigate(Screen.Settings.route) },
                                        currentRoomId = currentRoomId,
                                        isLoading = songViewModel.isLoading.collectAsState().value,
                                        onSongClick = { song ->
                                            val currentIndex = filteredSongs.indexOf(song)
                                            controller?.clearMediaItems()
                                            val mediaItems = filteredSongs.map { s ->
                                                MediaItem.Builder()
                                                    .setUri(s.songUrl)
                                                    .setMediaId(s._id)
                                                    .setMediaMetadata(
                                                        MediaMetadata.Builder()
                                                            .setTitle(s.title)
                                                            .setArtist(s.artist)
                                                            .setArtworkUri(android.net.Uri.parse(s.coverUrl))
                                                            .build()
                                                    )
                                                    .build()
                                            }
                                            controller?.addMediaItems(mediaItems)
                                            controller?.seekTo(currentIndex, 0)
                                            controller?.prepare()
                                            controller?.play()
                                            playbackState = playbackState.copy(currentSong = song)
                                        },
                                        onJoinRoom = { roomId -> songViewModel.joinRoom(roomId) },
                                        onCreateRoom = { songViewModel.createRoom() }
                                    )
                                }
                                composable(Screen.Search.route) {
                                    SearchScreen(onSongClick = { song ->
                                        controller?.setMediaItems(listOf(
                                            MediaItem.Builder()
                                                .setUri(song.songUrl)
                                                .setMediaId(song._id)
                                                .setMediaMetadata(
                                                    MediaMetadata.Builder()
                                                        .setTitle(song.title)
                                                        .setArtist(song.artist)
                                                        .setArtworkUri(android.net.Uri.parse(song.coverUrl))
                                                        .build()
                                                )
                                                .build()
                                        ))
                                        controller?.prepare()
                                        controller?.play()
                                        playbackState = playbackState.copy(currentSong = song)
                                        songViewModel.syncPlay(0L, song._id)
                                    })
                                }
                                composable(Screen.Library.route) {
                                    LibraryScreen(
                                        onSongClick = { song ->
                                            controller?.setMediaItems(listOf(
                                                MediaItem.Builder()
                                                    .setUri(song.songUrl)
                                                    .setMediaId(song._id)
                                                    .setMediaMetadata(
                                                        MediaMetadata.Builder()
                                                            .setTitle(song.title)
                                                            .setArtist(song.artist)
                                                            .setArtworkUri(android.net.Uri.parse(song.coverUrl))
                                                            .build()
                                                    )
                                                    .build()
                                            ))
                                            controller?.prepare()
                                            controller?.play()
                                            playbackState = playbackState.copy(currentSong = song)
                                            songViewModel.syncPlay(0L, song._id)
                                        },
                                        onAlbumClick = { albumId ->
                                            navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                                        }
                                    )
                                }
                                composable(Screen.Create.route) {
                                    CreateScreen(onComplete = { albumId ->
                                        navController.navigate(Screen.AlbumDetail.createRoute(albumId)) {
                                            popUpTo(Screen.Create.route) {
                                                inclusive = true
                                            }
                                        }
                                    })
                                }
                                composable(
                                    Screen.AlbumDetail.ROUTE_WITH_ARGS,
                                    arguments = listOf(navArgument(Screen.AlbumDetail.ARG_ID) { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val albumId = backStackEntry.arguments?.getString(Screen.AlbumDetail.ARG_ID) ?: ""
                                    AlbumDetailScreen(
                                        albumId = albumId,
                                        onBackClick = { navController.popBackStack() },
                                        onSongClick = { song ->
                                            controller?.setMediaItems(listOf(
                                                MediaItem.Builder()
                                                    .setUri(song.songUrl)
                                                    .setMediaId(song._id)
                                                    .setMediaMetadata(
                                                        MediaMetadata.Builder()
                                                            .setTitle(song.title)
                                                            .setArtist(song.artist)
                                                            .setArtworkUri(android.net.Uri.parse(song.coverUrl))
                                                            .build()
                                                    )
                                                    .build()
                                            ))
                                            controller?.prepare()
                                            controller?.play()
                                            playbackState = playbackState.copy(currentSong = song)
                                            songViewModel.syncPlay(0L, song._id)
                                        },
                                        onDeleteAlbum = {
                                            navController.navigate(Screen.Library.route) {
                                                popUpTo(Screen.Home.route) { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }
                                composable(Screen.Settings.route) {
                                    SettingsScreen(onBackClick = { navController.popBackStack() })
                                }
                            }

                            // Fixed Mini Player
                            playbackState.currentSong?.let { song ->
                                if (!showPlayer) {
                                    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                                        MiniPlayer(
                                            song = song,
                                            isPlaying = playbackState.isPlaying,
                                            progress = playbackState.progress,
                                            onPlayPauseClick = {
                                                if (playbackState.isPlaying) {
                                                    controller?.pause()
                                                    songViewModel.syncPause(playbackState.currentPosition)
                                                } else {
                                                    controller?.play()
                                                    songViewModel.syncPlay(playbackState.currentPosition, song._id)
                                                }
                                            },
                                            onExpandClick = { showPlayer = true }
                                        )
                                    }
                                }
                            }

                            if (showPlayer && playbackState.currentSong != null) {
                                PlayerScreen(
                                    song = playbackState.currentSong,
                                    isPlaying = playbackState.isPlaying,
                                    progress = playbackState.progress,
                                    elapsedTime = playbackState.formatTime(playbackState.currentPosition),
                                    totalTime = playbackState.formatTime(playbackState.duration),
                                    onPlayPauseClick = {
                                        if (playbackState.isPlaying) {
                                            controller?.pause()
                                            songViewModel.syncPause(playbackState.currentPosition)
                                        } else {
                                            controller?.play()
                                            playbackState.currentSong?.let { songViewModel.syncPlay(playbackState.currentPosition, it._id) }
                                        }
                                    },
                                    onSeek = { progress ->
                                        val seekPos = (progress * playbackState.duration).toLong()
                                        controller?.seekTo(seekPos)
                                        songViewModel.syncSeek(seekPos)
                                    },
                                    onCloseClick = { showPlayer = false },
                                    onPreviousClick = { controller?.seekToPrevious() },
                                    onNextClick = { controller?.seekToNext() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
