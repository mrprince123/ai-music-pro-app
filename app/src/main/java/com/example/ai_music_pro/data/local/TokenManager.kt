package com.example.ai_music_pro.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    fun saveUser(
        id: String,
        name: String,
        email: String,
        role: String,
        authProvider: String,
        phoneNumber: String?,
        createdAt: String?,
        profilePhoto: String?
    ) {
        prefs.edit().apply {
            putString("user_id", id)
            putString("user_name", name)
            putString("user_email", email)
            putString("user_role", role)
            putString("user_auth_provider", authProvider)
            putString("user_phone_number", phoneNumber)
            putString("user_created_at", createdAt)
            putString("user_profile_photo", profilePhoto)
            apply()
        }
    }

    fun getUser(): com.example.ai_music_pro.domain.model.User? {
        val id = prefs.getString("user_id", null) ?: return null
        return com.example.ai_music_pro.domain.model.User(
            _id = id,
            name = prefs.getString("user_name", "") ?: "",
            email = prefs.getString("user_email", "") ?: "",
            role = prefs.getString("user_role", "user") ?: "user",
            authProvider = prefs.getString("user_auth_provider", "email") ?: "email",
            phoneNumber = prefs.getString("user_phone_number", null),
            createdAt = prefs.getString("user_created_at", null),
            profilePhoto = prefs.getString("user_profile_photo", null)
        )
    }


    fun clear() {
        prefs.edit().clear().apply()
    }
}

