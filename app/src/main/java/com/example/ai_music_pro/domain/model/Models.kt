package com.example.ai_music_pro.domain.model

import com.google.gson.annotations.SerializedName

data class User(
    val _id: String,
    val email: String,
    val role: String
)

data class AuthResponse(
    val token: String,
    val role: String,
    val email: String,
    val _id: String
)

data class Song(
    val _id: String,
    @SerializedName("songName")
    val title: String,
    @SerializedName("singerName")
    val artist: String,
    @SerializedName("length")
    val duration: Long,
    val songUrl: String,
    @SerializedName("thumbnailUrl")
    val coverUrl: String,
    val category: String,
    val description: String? = null,
    val playCount: Int = 0
)

data class PaginatedResponse<T>(
    val songs: List<T>,
    val totalPages: Int,
    val currentPage: Int
)

data class StandardResponse<T>(
    val success: Boolean,
    val data: T
)

data class Album(
    val _id: String,
    val name: String,
    val coverUrl: String?,
    val songs: List<Song>
)

data class CreateAlbumRequest(
    val name: String
)

data class AddSongToAlbumRequest(
    val songId: String
)
