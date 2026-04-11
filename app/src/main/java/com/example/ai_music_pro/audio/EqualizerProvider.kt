package com.example.ai_music_pro.audio

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EqualizerProvider @Inject constructor() {
    val equalizerManager = EqualizerManager()
}
