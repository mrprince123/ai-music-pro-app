package com.example.ai_music_pro.data.local.dao

import androidx.room.*
import com.example.ai_music_pro.data.local.entities.LikedSongEntity
import com.example.ai_music_pro.data.local.entities.LocalAlbumEntity
import com.example.ai_music_pro.data.local.entities.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    // Liked Songs
    @Query("SELECT * FROM liked_songs ORDER BY addedAt DESC")
    fun getLikedSongs(): Flow<List<LikedSongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLikedSong(song: LikedSongEntity)

    @Delete
    suspend fun deleteLikedSong(song: LikedSongEntity)

    @Query("SELECT EXISTS(SELECT * FROM liked_songs WHERE id = :id)")
    fun isSongLiked(id: String): Flow<Boolean>

    // Search History
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 20")
    fun getSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(query: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearchQuery(query: String)

    // Local Albums
    @Query("SELECT * FROM local_albums ORDER BY createdAt DESC")
    fun getLocalAlbums(): Flow<List<LocalAlbumEntity>>

    @Query("SELECT * FROM local_albums WHERE id = :albumId LIMIT 1")
    suspend fun getLocalAlbum(albumId: String): LocalAlbumEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalAlbum(album: LocalAlbumEntity)

    @Query("DELETE FROM local_albums WHERE id = :albumId")
    suspend fun deleteLocalAlbum(albumId: String)

    // Recently Played
    @Query("SELECT * FROM recently_played ORDER BY playedAt DESC LIMIT 30")
    fun getRecentlyPlayed(): Flow<List<com.example.ai_music_pro.data.local.entities.RecentlyPlayedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentlyPlayed(song: com.example.ai_music_pro.data.local.entities.RecentlyPlayedEntity)
}
