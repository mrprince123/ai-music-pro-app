package com.example.ai_music_pro.domain.model

import com.google.gson.annotations.SerializedName

data class User(
    val _id: String,
    val name: String,
    val email: String,
    val profilePhoto: String? = null,
    val phoneNumber: String? = null,
    val role: String,
    val authProvider: String,
    val createdAt: String? = null
)

data class AuthResponse(
    val token: String,
    val role: String,
    val name: String,
    val email: String,
    val profilePhoto: String? = null,
    val phoneNumber: String? = null,
    val _id: String,
    val authProvider: String,
    val createdAt: String? = null
)


data class UserProfile(
    val _id: String,
    val name: String,
    val email: String = "",
    val role: String = "user",
    val authProvider: String = "email",
    val profilePhoto: String? = null
) {
    val id: String get() = _id
}

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
    val lyrics: String? = null,
    val playCount: Int = 0,
    val isLiked: Boolean = false
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

data class CarouselItem(
    val _id: String,
    val image: String,
    val title: String? = null,
    val link: String? = null
)

data class QuickAccessItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color
)

// --- Lyrics Models ---

data class LyricLine(
    val timeMs: Long,
    val text: String
)

data class LyricsResponse(
    val lyrics: String? = null,
    val syncedLyrics: List<LyricLine>? = null
)

// --- Audio Output Models ---

enum class AudioDeviceType {
    SPEAKER, WIRED_HEADPHONES, BLUETOOTH, OTHER
}

data class AudioOutputDevice(
    val id: Int,
    val name: String,
    val type: AudioDeviceType
)
