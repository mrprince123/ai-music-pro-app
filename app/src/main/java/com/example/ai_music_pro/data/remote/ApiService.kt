package com.example.ai_music_pro.data.remote

import com.example.ai_music_pro.domain.model.*
import retrofit2.http.*

interface ApiService {

    // 1. Auth
    @POST("/auth/login")
    suspend fun login(@Body body: Map<String, String>): AuthResponse

    // 2. Songs (Public)
    @GET("/songs")
    suspend fun getSongs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("category") category: String? = null
    ): PaginatedResponse<Song>

    @GET("/songs/{id}")
    suspend fun getSong(@Path("id") id: String): Song

    // 3. Favorites (Protected)
    @GET("/users/favorites")
    suspend fun getFavorites(): StandardResponse<List<Song>>

    @POST("/users/favorites/{songId}")
    suspend fun toggleFavorite(@Path("songId") songId: String): StandardResponse<Any>

    // 4. Albums (Protected)
    @GET("/albums/my-albums")
    suspend fun getMyAlbums(): List<Album>

    @POST("/albums")
    suspend fun createAlbum(@Body request: CreateAlbumRequest): Album

    @PUT("/albums/{id}/songs")
    suspend fun addSongToAlbum(
        @Path("id") albumId: String,
        @Body request: AddSongToAlbumRequest
    ): Album
}
