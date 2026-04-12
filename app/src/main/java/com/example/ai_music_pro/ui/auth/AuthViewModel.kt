package com.example.ai_music_pro.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_music_pro.domain.model.AuthResponse
import com.example.ai_music_pro.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: AuthResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        repository.getCurrentUser()?.let { user ->
            _authState.value = AuthState.Success(
                AuthResponse(
                    token = "", 
                    role = user.role,
                    name = user.name,
                    email = user.email,
                    authProvider = user.authProvider,
                    createdAt = user.createdAt,
                    profilePhoto = user.profilePhoto,
                    _id = user._id
                )
            )
        }
    }


    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.login(email, password)
                .onSuccess { _authState.value = AuthState.Success(it) }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "Login failed") }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.register(name, email, password)
                .onSuccess { _authState.value = AuthState.Success(it) }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "Registration failed") }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.googleLogin(idToken)
                .onSuccess { _authState.value = AuthState.Success(it) }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "Google login failed") }
        }
    }

    fun loginWithPhone(phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.phoneLogin(phoneNumber)
                .onSuccess { _authState.value = AuthState.Success(it) }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "Phone login failed") }
        }
    }
    fun logout() {
        repository.logout()
        _authState.value = AuthState.Idle
    }


    fun isLoggedIn(): Boolean = repository.isLoggedIn()
}


