package com.example.ai_music_pro.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class ThemeMode { LIGHT, DARK, SYSTEM }

@HiltViewModel
class ThemeViewModel @Inject constructor() : ViewModel() {
    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode = _themeMode.asStateFlow()

    fun setTheme(mode: ThemeMode) {
        _themeMode.value = mode
    }
}
