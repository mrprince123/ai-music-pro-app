package com.example.ai_music_pro.domain.repository

import com.example.ai_music_pro.data.local.TokenManager
import com.example.ai_music_pro.data.remote.ApiService
import com.example.ai_music_pro.domain.model.AuthResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(mapOf("email" to email, "password" to password))
            tokenManager.saveToken(response.token)
            tokenManager.saveUser(
                id = response._id,
                name = response.name,
                email = response.email,
                role = response.role,
                authProvider = response.authProvider,
                phoneNumber = response.phoneNumber,
                createdAt = response.createdAt,
                profilePhoto = response.profilePhoto
            )

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.register(
                mapOf("name" to name, "email" to email, "password" to password)
            )
            tokenManager.saveToken(response.token)
            tokenManager.saveUser(
                id = response._id,
                name = response.name,
                email = response.email,
                role = response.role,
                authProvider = response.authProvider,
                phoneNumber = response.phoneNumber,
                createdAt = response.createdAt,
                profilePhoto = response.profilePhoto
            )

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun googleLogin(idToken: String): Result<AuthResponse> {
        return try {
            val response = apiService.googleLogin(mapOf("idToken" to idToken))
            tokenManager.saveToken(response.token)
            tokenManager.saveUser(
                id = response._id,
                name = response.name,
                email = response.email,
                role = response.role,
                authProvider = response.authProvider,
                phoneNumber = response.phoneNumber,
                createdAt = response.createdAt,
                profilePhoto = response.profilePhoto
            )

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun phoneLogin(phoneNumber: String, name: String? = null): Result<AuthResponse> {
        return try {
            val params = mutableMapOf("phoneNumber" to phoneNumber)
            name?.let { params["name"] = it }
            val response = apiService.phoneLogin(params)
            tokenManager.saveToken(response.token)
            tokenManager.saveUser(
                id = response._id,
                name = response.name,
                email = response.email ?: "",
                role = response.role,
                authProvider = response.authProvider,
                phoneNumber = response.phoneNumber,
                createdAt = response.createdAt,
                profilePhoto = response.profilePhoto
            )

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager.clear()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.getToken() != null
    }

    fun getCurrentUser(): com.example.ai_music_pro.domain.model.User? {
        return tokenManager.getUser()
    }
}

