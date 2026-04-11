package com.example.ai_music_pro.audio

import android.media.audiofx.Equalizer
import android.util.Log

class EqualizerManager {
    private var equalizer: Equalizer? = null
    
    fun init(audioSessionId: Int) {
        try {
            if (audioSessionId != 0) {
                equalizer = Equalizer(0, audioSessionId).apply {
                    enabled = true
                }
            }
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error initializing equalizer", e)
        }
    }
    
    fun setEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
    }
    
    fun getPresetNames(): List<String> {
        val count = equalizer?.numberOfPresets?.toInt() ?: 0
        return (0 until count).map { equalizer?.getPresetName(it.toShort()) ?: "Preset $it" }
    }
    
    fun usePreset(presetIndex: Short) {
        equalizer?.usePreset(presetIndex)
    }
    
    fun getBandLevelRange(): ShortArray {
        return equalizer?.bandLevelRange ?: shortArrayOf(-1500, 1500)
    }
    
    fun getNumberOfBands(): Short {
        return equalizer?.numberOfBands ?: 0
    }
    
    fun setBandLevel(band: Short, level: Short) {
        equalizer?.setBandLevel(band, level)
    }
    
    fun getBandCenterFreq(band: Short): Int {
        return equalizer?.getCenterFreq(band) ?: 0
    }
    
    fun release() {
        equalizer?.release()
        equalizer = null
    }
}
