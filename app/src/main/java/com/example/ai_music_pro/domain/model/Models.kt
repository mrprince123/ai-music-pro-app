package com.aimusic.domain.model

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
    val title: String,
    val artist: String,
    val duration: Long,
    val songUrl: String,
    val coverUrl: String,
    val category: String
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
