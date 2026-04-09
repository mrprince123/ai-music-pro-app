package com.aimusic.service

import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.aimusic.domain.model.Song
import com.aimusic.socket.SocketManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ExoPlayerService : MediaSessionService() {

    @Inject
    lateinit var socketManager: SocketManager

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    // You could emit sync events from here if the local user is driving
                }
            })
        }

        mediaSession = MediaSession.Builder(this, exoPlayer!!)
            .build()
        
        observeSyncEvents()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    private fun observeSyncEvents() {
        serviceScope.launch {
            socketManager.syncEvents.collectLatest { event ->
                when (event) {
                    is com.aimusic.socket.SyncEvent.Play -> {
                        exoPlayer?.seekTo(event.currentTimeMs)
                        exoPlayer?.play()
                    }
                    is com.aimusic.socket.SyncEvent.Pause -> {
                        exoPlayer?.seekTo(event.currentTimeMs)
                        exoPlayer?.pause()
                    }
                    is com.aimusic.socket.SyncEvent.Seek -> {
                        exoPlayer?.seekTo(event.currentTimeMs)
                    }
                }
            }
        }
    }

    fun playSong(song: Song) {
        exoPlayer?.let { player ->
            val mediaItem = MediaItem.fromUri(song.songUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
