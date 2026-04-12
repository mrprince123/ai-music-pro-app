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
import com.example.ai_music_pro.data.repository.SongRepository
import com.example.ai_music_pro.util.Constants
import javax.inject.Inject


@AndroidEntryPoint
class ExoPlayerService : MediaSessionService() {

    @Inject
    lateinit var socketManager: SocketManager

    @Inject
    lateinit var equalizerProvider: EqualizerProvider

    @Inject
    lateinit var songRepository: SongRepository

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Keep track of current song for sync check
    private var currentSongId: String? = null

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
                        handleSyncPlay(event.songId, event.currentTimeMs)
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

    private suspend fun handleSyncPlay(songId: String, timeMs: Long) {
        if (currentSongId != songId) {
            // New song needs to be loaded
            val result = songRepository.getSong(songId)
            result.onSuccess { song ->
                playSong(song, startAtMs = timeMs)
            }.onFailure {
                android.util.Log.e("ExoPlayerService", "Failed to load song for sync: $songId")
            }
        } else {
            exoPlayer?.seekTo(timeMs)
            exoPlayer?.play()
        }
    }

    fun playSong(song: Song, startAtMs: Long = 0L) {
        exoPlayer?.let { player ->
            currentSongId = song._id
            
            val finalUrl = if (song.songUrl.startsWith("http")) {
                song.songUrl
            } else {
                "${Constants.BASE_URL}${song.songUrl.removePrefix("/")}"
            }

            
            val mediaItem = MediaItem.fromUri(finalUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            if (startAtMs > 0) {
                player.seekTo(startAtMs)
            }
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
