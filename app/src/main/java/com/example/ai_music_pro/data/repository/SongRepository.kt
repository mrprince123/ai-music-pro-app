package com.example.ai_music_pro.data.repository

import com.example.ai_music_pro.data.remote.ApiService
import com.example.ai_music_pro.domain.model.PaginatedResponse
import com.example.ai_music_pro.domain.model.Song
import com.example.ai_music_pro.data.local.dao.MusicDao
import com.example.ai_music_pro.data.local.entities.LikedSongEntity
import com.example.ai_music_pro.data.local.entities.LocalAlbumEntity
import com.example.ai_music_pro.data.local.entities.SearchHistoryEntity
import com.example.ai_music_pro.domain.model.CarouselItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val apiService: ApiService,
    private val musicDao: MusicDao
) {
    suspend fun getSongs(page: Int = 1, limit: Int = 20): Result<PaginatedResponse<Song>> {
        return try {
            val response = apiService.getSongs(page, limit)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCarousels(): Result<List<CarouselItem>> {
        return try {
            val response = apiService.getCarousels()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<String>> {
        return try {
            val response = apiService.getCategories()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSong(id: String): Result<Song> {
        return try {
            val response = apiService.getSong(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Liked Songs
    fun getLikedSongs(): Flow<List<Song>> {
        return musicDao.getLikedSongs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun toggleLikeSong(song: Song, isLiked: Boolean) {
        if (isLiked) {
            musicDao.deleteLikedSong(song.toEntity())
        } else {
            musicDao.insertLikedSong(song.toEntity())
        }
    }

    fun isSongLiked(id: String): Flow<Boolean> = musicDao.isSongLiked(id)

    // Recently Played
    fun getRecentlyPlayed(): Flow<List<Song>> {
        return musicDao.getRecentlyPlayed().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun addRecentlyPlayed(song: Song) {
        musicDao.insertRecentlyPlayed(song.toRecentlyPlayedEntity())
    }

    // Search History
    fun getSearchHistory(): Flow<List<String>> = musicDao.getSearchHistory().map { list -> list.map { it.query } }

    suspend fun addSearchQuery(query: String) {
        musicDao.insertSearchQuery(SearchHistoryEntity(query))
    }

    suspend fun deleteSearchQuery(query: String) {
        musicDao.deleteSearchQuery(query)
    }

    // Local Albums
    fun getLocalAlbums(): Flow<List<LocalAlbumEntity>> = musicDao.getLocalAlbums()

    suspend fun getLocalAlbum(albumId: String): LocalAlbumEntity? = musicDao.getLocalAlbum(albumId)

    suspend fun deleteLocalAlbum(albumId: String) {
        musicDao.deleteLocalAlbum(albumId)
    }

    suspend fun createLocalAlbum(name: String, songIds: List<String>): String {
        val albumId = java.util.UUID.randomUUID().toString()
        musicDao.insertLocalAlbum(
            LocalAlbumEntity(
                id = albumId,
                name = name,
                coverUrl = null,
                songIds = songIds.joinToString(",")
            )
        )
        return albumId
    }

    suspend fun getSongsByIds(songIds: List<String>): Result<List<Song>> {
        return try {
            val songs = songIds.mapNotNull { id ->
                getSong(id).getOrNull()
            }
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSongLyrics(songId: String): Result<com.example.ai_music_pro.domain.model.LyricsResponse> {
        return try {
            val response = apiService.getSongLyrics(songId)
            Result.success(response)
        } catch (e: Exception) {
            // Fallback: use description from the song itself
            Result.failure(e)
        }
    }
}

// Mappers
fun LikedSongEntity.toDomain() = Song(
    _id = id,
    title = title,
    artist = artist,
    duration = duration,
    songUrl = songUrl,
    coverUrl = coverUrl,
    category = "", // Category not stored locally for now
    description = null
)

fun Song.toEntity() = LikedSongEntity(
    id = _id,
    title = title,
    artist = artist,
    coverUrl = coverUrl,
    songUrl = songUrl,
    duration = duration
)

fun com.example.ai_music_pro.data.local.entities.RecentlyPlayedEntity.toDomain() = Song(
    _id = id,
    title = title,
    artist = artist,
    duration = duration,
    songUrl = songUrl,
    coverUrl = coverUrl,
    category = "",
    description = null
)

fun Song.toRecentlyPlayedEntity() = com.example.ai_music_pro.data.local.entities.RecentlyPlayedEntity(
    id = _id,
    title = title,
    artist = artist,
    coverUrl = coverUrl,
    songUrl = songUrl,
    duration = duration
)
