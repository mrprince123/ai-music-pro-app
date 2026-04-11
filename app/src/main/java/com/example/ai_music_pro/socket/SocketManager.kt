package com.example.ai_music_pro.socket

import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor() {

    private var socket: Socket? = null

    // Flows for observing socket events
    private val _roomEvents = MutableSharedFlow<RoomEvent>(extraBufferCapacity = 10)
    val roomEvents = _roomEvents.asSharedFlow()

    private val _syncEvents = MutableSharedFlow<SyncEvent>(extraBufferCapacity = 10)
    val syncEvents = _syncEvents.asSharedFlow()

    var serverNetworkOffsetMs: Long = 0L
        private set

    fun getAdjustedTime(localTime: Long = System.currentTimeMillis()): Long {
        return localTime - serverNetworkOffsetMs
    }

    fun getLocalTimeFromAdjusted(adjustedTime: Long): Long {
        return adjustedTime + serverNetworkOffsetMs
    }

    init {
        try {
            val options = IO.Options().apply {
                forceNew = true
                reconnection = true
            }
            val uri = URI.create("http://192.168.1.7:5002")
            socket = IO.socket(uri, options)
            setupListeners()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun connect() {
        socket?.connect()
    }

    fun disconnect() {
        socket?.disconnect()
    }

    private fun setupListeners() {
        socket?.apply {
            on(Socket.EVENT_CONNECT) {
                android.util.Log.d("SocketManager", "Connected to server")
            }
            on(Socket.EVENT_CONNECT_ERROR) { args ->
                android.util.Log.e("SocketManager", "Connection error: ${args.getOrNull(0)}")
            }
            on(Socket.EVENT_DISCONNECT) {
                android.util.Log.d("SocketManager", "Disconnected from server")
            }
            on("server_time") { args ->
                val data = args[0] as JSONObject
                val serverTime = data.getLong("serverTime")
                val localTime = System.currentTimeMillis()
                serverNetworkOffsetMs = localTime - serverTime
            }
            on("room_joined") { args ->
                val data = args[0] as JSONObject
                val roomId = data.getString("roomId")
                _roomEvents.tryEmit(RoomEvent.RoomJoined(roomId))
            }
            on("room_full") {
                _roomEvents.tryEmit(RoomEvent.RoomFull)
            }
            on("user_joined") { args ->
                val data = args[0] as JSONObject
                _roomEvents.tryEmit(RoomEvent.UserJoined(data.getString("userId")))
            }
            on("user_left") { args ->
                val data = args[0] as JSONObject
                _roomEvents.tryEmit(RoomEvent.UserLeft(data.getString("userId")))
            }
            on("sync_play") { args ->
                (args.getOrNull(0) as? JSONObject)?.let { data ->
                    val currentTime = data.optLong("currentTime", 0L)
                    val songId = data.optString("songId", "")
                    _syncEvents.tryEmit(SyncEvent.Play(currentTime, songId))
                }
            }
            on("sync_pause") { args ->
                (args.getOrNull(0) as? JSONObject)?.let { data ->
                    _syncEvents.tryEmit(SyncEvent.Pause(data.optLong("currentTime", 0L)))
                }
            }
            on("sync_seek") { args ->
                (args.getOrNull(0) as? JSONObject)?.let { data ->
                    _syncEvents.tryEmit(SyncEvent.Seek(data.optLong("currentTime", 0L)))
                }
            }
            on("error") { args ->
                (args.getOrNull(0) as? JSONObject)?.let { data ->
                    _roomEvents.tryEmit(RoomEvent.Error(data.optString("message", "Unknown Error")))
                }
            }
        }
    }

    // Emit Events
    fun createRoom() {
        socket?.emit("create_room")
    }

    fun joinRoom(roomId: String) {
        socket?.emit("join_room", roomId)
    }

    fun play(roomId: String, currentTimeMs: Long, songId: String) {
        val payload = JSONObject().apply {
            put("roomId", roomId)
            put("currentTime", currentTimeMs)
            put("songId", songId)
        }
        socket?.emit("play", payload)
    }

    fun pause(roomId: String, currentTimeMs: Long) {
        val payload = JSONObject().apply {
            put("roomId", roomId)
            put("currentTime", currentTimeMs)
        }
        socket?.emit("pause", payload)
    }

    fun seek(roomId: String, currentTimeMs: Long) {
        val payload = JSONObject().apply {
            put("roomId", roomId)
            put("currentTime", currentTimeMs)
        }
        socket?.emit("seek", payload)
    }
}

sealed class RoomEvent {
    data class RoomJoined(val roomId: String) : RoomEvent()
    object RoomFull : RoomEvent()
    data class UserJoined(val userId: String) : RoomEvent()
    data class UserLeft(val userId: String) : RoomEvent()
    data class Error(val message: String) : RoomEvent()
}

sealed class SyncEvent {
    data class Play(val currentTimeMs: Long, val songId: String) : SyncEvent()
    data class Pause(val currentTimeMs: Long) : SyncEvent()
    data class Seek(val currentTimeMs: Long) : SyncEvent()
}
