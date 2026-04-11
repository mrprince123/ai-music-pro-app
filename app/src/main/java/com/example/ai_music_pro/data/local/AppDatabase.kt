package com.example.ai_music_pro.data.local

import androidx.room.*
import com.example.ai_music_pro.data.local.dao.MusicDao
import com.example.ai_music_pro.data.local.entities.LikedSongEntity
import com.example.ai_music_pro.data.local.entities.LocalAlbumEntity
import com.example.ai_music_pro.data.local.entities.SearchHistoryEntity

@Database(
    entities = [LikedSongEntity::class, SearchHistoryEntity::class, LocalAlbumEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
}
