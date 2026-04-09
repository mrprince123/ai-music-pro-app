package com.aimusic.socket

import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
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

    private var serverNetworkOffsetMs: Long = 0L

    init {
        try {
            val options = IO.Options().apply {
                forceNew = true
                reconnection = true
            }
            socket = IO.socket("ws://10.0.2.2:5002", options)
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
                // Connected
            }
            on("server_time") { args ->
                val serverTime = (args[0] as Number).toLong()
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
                val data = args[0] as JSONObject
                _syncEvents.tryEmit(SyncEvent.Play(data.getLong("currentTime")))
            }
            on("sync_pause") { args ->
                val data = args[0] as JSONObject
                _syncEvents.tryEmit(SyncEvent.Pause(data.getLong("currentTime")))
            }
            on("sync_seek") { args ->
                val data = args[0] as JSONObject
                _syncEvents.tryEmit(SyncEvent.Seek(data.getLong("currentTime")))
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
}

sealed class SyncEvent {
    data class Play(val currentTimeMs: Long) : SyncEvent()
    data class Pause(val currentTimeMs: Long) : SyncEvent()
    data class Seek(val currentTimeMs: Long) : SyncEvent()
}
