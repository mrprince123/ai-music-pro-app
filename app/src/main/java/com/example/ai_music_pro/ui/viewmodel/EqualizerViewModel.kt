package com.example.ai_music_pro.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ai_music_pro.audio.EqualizerManager
import com.example.ai_music_pro.audio.EqualizerProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val equalizerProvider: EqualizerProvider
) : ViewModel() {
    private val equalizerManager = equalizerProvider.equalizerManager

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled = _isEnabled.asStateFlow()

    private val _presets = MutableStateFlow(equalizerManager.getPresetNames())
    val presets = _presets.asStateFlow()

    private val _bands = MutableStateFlow<List<BandState>>(emptyList())
    val bands = _bands.asStateFlow()

    init {
        loadBands()
    }

    private fun loadBands() {
        val numBands = equalizerManager.getNumberOfBands()
        val range = equalizerManager.getBandLevelRange()
        val list = mutableListOf<BandState>()
        for (i in 0 until numBands) {
            list.add(BandState(
                bandId = i.toShort(),
                freq = equalizerManager.getBandCenterFreq(i.toShort()) / 1000,
                level = 0, // Initial
                min = range[0].toShort(),
                max = range[1].toShort()
            ))
        }
        _bands.value = list
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        equalizerManager.setEnabled(enabled)
    }

    fun setPreset(index: Int) {
        equalizerManager.usePreset(index.toShort())
    }

    fun setBandLevel(bandId: Short, level: Short) {
        equalizerManager.setBandLevel(bandId, level)
        val current = _bands.value.toMutableList()
        val index = current.indexOfFirst { it.bandId == bandId }
        if (index != -1) {
            current[index] = current[index].copy(level = level)
            _bands.value = current
        }
    }
}

data class BandState(
    val bandId: Short,
    val freq: Int,
    val level: Short,
    val min: Short,
    val max: Short
)
