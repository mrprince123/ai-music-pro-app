package com.example.ai_music_pro.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "liked_songs")
data class LikedSongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val coverUrl: String,
    val songUrl: String,
    val duration: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_albums")
data class LocalAlbumEntity(
    @PrimaryKey val id: String,
    val name: String,
    val coverUrl: String?,
    val songIds: String, // Comma separated IDs or JSON string
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val coverUrl: String,
    val songUrl: String,
    val duration: Long,
    val playedAt: Long = System.currentTimeMillis()
)
