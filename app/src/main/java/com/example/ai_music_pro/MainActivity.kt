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
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import com.example.ai_music_pro.ui.viewmodel.LibraryViewModel
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
    private val libraryViewModel: LibraryViewModel by viewModels()
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            songViewModel.fetchLocalSongs(this@MainActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkPermissions()

        setContent {
            val themeMode by themeViewModel.themeMode.collectAsState()
            AIMusicProTheme(themeMode = themeMode) {
                val songs by songViewModel.songs.collectAsState()
                val localSongs by songViewModel.localSongs.collectAsState()
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

                // Combine lists for lookup
                val allAvailableSongs = remember(songs, localSongs) { songs + localSongs }
                val updatedAllSongs by rememberUpdatedState(allAvailableSongs)

                fun handleSongClick(song: Song) {
                    controller?.let { player ->
                        val allItems = updatedAllSongs.map { s ->
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
                        val index = updatedAllSongs.indexOfFirst { it._id == song._id }
                        player.setMediaItems(allItems)
                        if (index >= 0) {
                            player.seekTo(index, 0L)
                        }
                        player.prepare()
                        player.play()
                        playbackState = playbackState.copy(currentSong = song)
                        songViewModel.recordRecentPlay(song)
                        songViewModel.syncPlay(0L, song._id)
                    }
                }

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
                                // Use the mediaId we set in handleSongClick for reliable lookups
                                val mediaId = mediaItem?.mediaId
                                val currentSong = updatedAllSongs.find { it._id == mediaId }
                                playbackState = playbackState.copy(currentSong = currentSong)
                            }
                            override fun onPlaybackStateChanged(state: Int) {
                                playbackState = playbackState.copy(
                                    isReady = state == Player.STATE_READY,
                                    duration = controller?.duration?.coerceAtLeast(0L) ?: 0L
                                )
                            }
                        })
                    }, mainExecutor)
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
                        val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Main content area with bottom bar padding
                            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                                val startDest = if (authViewModel.isLoggedIn()) Screen.Home.route else Screen.Login.route
                                NavHost(
                                    navController = navController, 
                                    startDestination = startDest,
                                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(500)) + fadeIn(animationSpec = tween(500)) },
                                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(500)) + fadeOut(animationSpec = tween(500)) },
                                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(500)) + fadeIn(animationSpec = tween(500)) },
                                    popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(500)) + fadeOut(animationSpec = tween(500)) }
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
                                            categories = songViewModel.categories.collectAsState().value,
                                            searchQuery = searchQuery,
                                            onSearchQueryChange = { songViewModel.setSearchQuery(it) },
                                            onSettingsClick = { navController.navigate(Screen.Settings.route) },
                                            currentRoomId = currentRoomId,
                                            isLoading = songViewModel.isLoading.collectAsState().value,
                                            onSongClick = { 
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                handleSongClick(it) 
                                            },
                                            onJoinRoom = { roomId -> songViewModel.joinRoom(roomId) },
                                            onCreateRoom = { songViewModel.createRoom() },
                                            onCreateMusicClick = { navController.navigate(Screen.Create.route) },
                                            onQuickAccessClick = { title ->
                                                when(title) {
                                                    "Recently Played" -> navController.navigate(Screen.RecentList.route)
                                                    "Liked Songs" -> navController.navigate(Screen.LikedList.route)
                                                    "Your Albums" -> navController.navigate(Screen.Library.route)
                                                    "Trending" -> navController.navigate(Screen.TrendingList.route)
                                                }
                                            },
                                            onAddToQueue = { songId -> songViewModel.requestSong(songId) },
                                            onRefresh = { songViewModel.fetchSongs() },
                                            onLikeClick = { songId -> songViewModel.toggleLike(songId) }
                                        )
                                    }
                                    composable(Screen.Search.route) {
                                        SearchScreen(onSongClick = { 
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            handleSongClick(it) 
                                        })
                                    }
                                    composable(Screen.Library.route) {
                                        LibraryScreen(
                                            viewModel = libraryViewModel,
                                            localSongs = localSongs,
                                            onSongClick = { 
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                handleSongClick(it) 
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
                                            onSongClick = { 
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                handleSongClick(it) 
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
                                            authViewModel = authViewModel,
                                            onSettingsClick = { navController.navigate(Screen.Settings.route) },
                                            onLikedSongsClick = { navController.navigate(Screen.LikedList.route) },
                                            onRecentPlayedClick = { navController.navigate(Screen.RecentList.route) },
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
                                            },
                                            themeViewModel = themeViewModel
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
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                if (playbackState.isPlaying) {
                                                    controller?.pause()
                                                    songViewModel.syncPause(playbackState.currentPosition)
                                                } else {
                                                    controller?.play()
                                                    playbackState.currentSong?.let { songViewModel.syncPlay(playbackState.currentPosition, it._id) }
                                                }
                                            },
                                            onChangeSong = { songId ->
                                                val song = updatedAllSongs.find { it._id == songId }
                                                if (song != null) {
                                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                    handleSongClick(song)
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

                                    composable(Screen.RecentList.route) {
                                        val recentSongs by songViewModel.recentlyPlayed.collectAsState()
                                        ListScreen(
                                            title = "Recently Played",
                                            songs = recentSongs,
                                            onBackClick = { navController.popBackStack() },
                                            onSongClick = { 
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                handleSongClick(it) 
                                            },
                                            onLikeClick = { songId -> songViewModel.toggleLike(songId) }
                                        )
                                    }
                                    composable(Screen.LikedList.route) {
                                        val likedSongs by songViewModel.likedSongsList.collectAsState()
                                        ListScreen(
                                            title = "Liked Songs",
                                            songs = likedSongs,
                                            onBackClick = { navController.popBackStack() },
                                            onSongClick = { 
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                handleSongClick(it) 
                                            },
                                            onLikeClick = { songId -> songViewModel.toggleLike(songId) }
                                        )
                                    }
                                    composable(Screen.TrendingList.route) {
                                        ListScreen(
                                            title = "Trending",
                                            songs = songs.sortedByDescending { it.artist }.take(20),
                                            onBackClick = { navController.popBackStack() },
                                            onSongClick = { 
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                handleSongClick(it) 
                                            },
                                            onLikeClick = { songId -> songViewModel.toggleLike(songId) }
                                        )
                                    }
                                }

                                // Mini Player above the bottom bar
                                AnimatedVisibility(
                                    visible = !showPlayer && playbackState.currentSong != null,
                                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                ) {
                                    playbackState.currentSong?.let { song ->
                                        MiniPlayer(
                                            song = song,
                                            isPlaying = playbackState.isPlaying,
                                            progress = playbackState.progress,
                                            onPlayPauseClick = {
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                if (playbackState.isPlaying) {
                                                    controller?.pause()
                                                    songViewModel.syncPause(playbackState.currentPosition)
                                                } else {
                                                    controller?.play()
                                                    songViewModel.syncPlay(playbackState.currentPosition, song._id)
                                                }
                                            },
                                            onExpandClick = { 
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                showPlayer = true 
                                            }
                                        )
                                    }
                                }
                            }

                            // Full Player Screen Overlay (Outside the padded content to prevent jumps)
                            AnimatedVisibility(
                                visible = showPlayer && playbackState.currentSong != null,
                                enter = slideInVertically(initialOffsetY = { it }),
                                exit = slideOutVertically(targetOffsetY = { it })
                            ) {
                                playbackState.currentSong?.let { song ->
                                    PlayerScreen(
                                        song = song,
                                        isPlaying = playbackState.isPlaying,
                                        progress = playbackState.progress,
                                        elapsedTime = playbackState.formatTime(playbackState.currentPosition),
                                        totalTime = playbackState.formatTime(playbackState.duration),
                                        onPlayPauseClick = {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
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
    }

    private fun checkPermissions() {
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (androidx.core.content.ContextCompat.checkSelfPermission(this, storagePermission) 
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(arrayOf(storagePermission))
        } else {
            songViewModel.fetchLocalSongs(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
