@file:Suppress("UnsafeOptInUsageError")

package com.example.ai_music_pro.service

import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.ai_music_pro.domain.model.Song
import com.example.ai_music_pro.socket.SocketManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.ai_music_pro.audio.EqualizerProvider
import javax.inject.Inject

@AndroidEntryPoint
class ExoPlayerService : MediaSessionService() {

    @Inject
    lateinit var socketManager: SocketManager

    @Inject
    lateinit var equalizerProvider: EqualizerProvider

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        equalizerProvider.equalizerManager.init(audioSessionId)
                    }
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
                    is com.example.ai_music_pro.socket.SyncEvent.Play -> {
                        exoPlayer?.seekTo(event.currentTimeMs)
                        exoPlayer?.play()
                    }
                    is com.example.ai_music_pro.socket.SyncEvent.Pause -> {
                        exoPlayer?.seekTo(event.currentTimeMs)
                        exoPlayer?.pause()
                    }
                    is com.example.ai_music_pro.socket.SyncEvent.Seek -> {
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
        equalizerProvider.equalizerManager.release()
        super.onDestroy()
    }
}
