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
import com.example.ai_music_pro.ui.screens.ProfileScreen
import com.example.ai_music_pro.ui.screens.SearchScreen
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
    private val authViewModel: com.example.ai_music_pro.ui.auth.AuthViewModel by viewModels()
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
                val carousels by songViewModel.carousels.collectAsState()
                val searchQuery by songViewModel.searchQuery.collectAsState()
                val currentRoomId by songViewModel.currentRoomId.collectAsState()
                val participants by songViewModel.participants.collectAsState()
                val queue by songViewModel.queue.collectAsState()
                
                val authState by authViewModel.authState.collectAsState()
                
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
                            val showBottomBar = currentRoute != Screen.Login.route && currentRoute != Screen.Signup.route
                            if (!showPlayer && showBottomBar) {
                                NavigationBar {
                                    bottomNavItems.forEach { screen ->
                                        NavigationBarItem(
                                            selected = currentRoute == screen.route,
                                            onClick = {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.startDestinationId)
                                                    launchSingleTop = true
                                                }
                                            },
                                            icon = { Icon(screen.icon!!, contentDescription = null) },
                                            label = { Text(screen.title!!) }
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                            val startDest = if (authViewModel.isLoggedIn()) Screen.Home.route else Screen.Login.route
                            NavHost(
                                navController = navController, 
                                startDestination = startDest
                            ) {

                                composable(Screen.Login.route) {
                                    LoginScreen(
                                        onLoginClick = { email, pass -> authViewModel.login(email, pass) },
                                        onRegisterClick = { navController.navigate(Screen.Signup.route) },
                                        onGoogleLoginClick = { /* Handle Google Sign In */ },
                                        onPhoneLoginClick = { /* Handle Phone Sign In */ },
                                        isLoading = authState is com.example.ai_music_pro.ui.auth.AuthState.Loading,
                                        errorMessage = (authState as? com.example.ai_music_pro.ui.auth.AuthState.Error)?.message
                                    )
                                    // Effect to navigate home on success
                                    LaunchedEffect(authState) {
                                        if (authState is com.example.ai_music_pro.ui.auth.AuthState.Success) {
                                            navController.navigate(Screen.Home.route) {
                                                popUpTo(Screen.Login.route) { inclusive = true }
                                            }
                                        }
                                    }
                                }
                                composable(Screen.Signup.route) {
                                    SignupScreen(
                                        onSignupClick = { name, email, pass -> authViewModel.register(name, email, pass) },
                                        onBackToLogin = { navController.popBackStack() },
                                        isLoading = authState is com.example.ai_music_pro.ui.auth.AuthState.Loading,
                                        errorMessage = (authState as? com.example.ai_music_pro.ui.auth.AuthState.Error)?.message
                                    )
                                }
                                composable(Screen.Home.route) {
                                    // Auto-navigate to Room when a room is joined
                                    LaunchedEffect(currentRoomId) {
                                        if (currentRoomId != null) {
                                            navController.navigate(Screen.Room.route) {
                                                launchSingleTop = true
                                            }
                                        }
                                    }

                                    HomeScreen(
                                        allSongs = songs,
                                        filteredSongs = filteredSongs,
                                        carousels = carousels,
                                        searchQuery = searchQuery,
                                        onSearchQueryChange = { songViewModel.setSearchQuery(it) },
                                        onSettingsClick = { navController.navigate(Screen.Settings.route) },
                                        currentRoomId = currentRoomId,
                                        isLoading = songViewModel.isLoading.collectAsState().value,
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
                                        onJoinRoom = { roomId -> songViewModel.joinRoom(roomId) },
                                        onCreateRoom = { songViewModel.createRoom() },
                                        onAddToQueue = { songId -> songViewModel.requestSong(songId) },
                                        onRefresh = { songViewModel.fetchSongs() },
                                        onLikeClick = { songId -> songViewModel.toggleLike(songId) }
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
                                composable(Screen.Profile.route) {
                                    ProfileScreen(
                                        onSettingsClick = { navController.navigate(Screen.Settings.route) },
                                        onLogoutClick = {
                                            navController.navigate(Screen.Login.route) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    )
                                }

                                composable(Screen.Settings.route) {

                                    SettingsScreen(
                                        onBackClick = { navController.popBackStack() },
                                        onLogout = {
                                            android.widget.Toast.makeText(this@MainActivity, "Logged out successfully", android.widget.Toast.LENGTH_SHORT).show()
                                            navController.navigate(Screen.Login.route) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    )
                                }

                                composable(Screen.Room.route) {
                                    RoomScreen(
                                        roomId = currentRoomId ?: "",
                                        hostId = songViewModel.hostId.collectAsState().value,
                                        isHost = songViewModel.hostId.collectAsState().value != null,
                                        participants = participants,
                                        queue = queue,
                                        allSongs = songs,
                                        currentSongId = songViewModel.currentSongId.collectAsState().value,
                                        isPlaying = playbackState.isPlaying,
                                        onPlayPause = {
                                            if (playbackState.isPlaying) {
                                                controller?.pause()
                                                songViewModel.syncPause(playbackState.currentPosition)
                                            } else {
                                                controller?.play()
                                                playbackState.currentSong?.let { songViewModel.syncPlay(playbackState.currentPosition, it._id) }
                                            }
                                        },
                                        onChangeSong = { songId ->
                                            val song = songs.find { it._id == songId }
                                            if (song != null) {
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
                                                songViewModel.changeSong(song._id)
                                            }
                                        },
                                        onAddToQueue = { songId -> songViewModel.requestSong(songId) },
                                        onRemoveFromQueue = { songId -> songViewModel.removeQueueItem(songId) },
                                        onKickUser = { userId -> songViewModel.kickUser(userId) },
                                        onLeaveRoom = { songViewModel.leaveRoom() },
                                        onBackClick = { navController.popBackStack() }
                                    )
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
                                    onNextClick = { controller?.seekToNext() },
                                    participants = participants,
                                    queue = queue,
                                    allSongs = songs,
                                    onRemoveQueueItem = { songId -> songViewModel.removeQueueItem(songId) },
                                    onLikeClick = { songId -> songViewModel.toggleLike(songId) }
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
