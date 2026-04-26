package com.example.ai_music_pro.audio

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import com.example.ai_music_pro.domain.model.AudioDeviceType
import com.example.ai_music_pro.domain.model.AudioOutputDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioOutputManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _availableDevices = MutableStateFlow<List<AudioOutputDevice>>(emptyList())
    val availableDevices: StateFlow<List<AudioOutputDevice>> = _availableDevices.asStateFlow()

    private val _activeDevice = MutableStateFlow<AudioOutputDevice?>(null)
    val activeDevice: StateFlow<AudioOutputDevice?> = _activeDevice.asStateFlow()

    private val _deviceSwitchRequested = MutableStateFlow(false)
    val deviceSwitchRequested: StateFlow<Boolean> = _deviceSwitchRequested.asStateFlow()

    private val _resumeAfterSwitch = MutableStateFlow(false)
    val resumeAfterSwitch: StateFlow<Boolean> = _resumeAfterSwitch.asStateFlow()

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            refreshDevices()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            _deviceSwitchRequested.value = true
            refreshDevices()
        }
    }

    init {
        audioManager.registerAudioDeviceCallback(deviceCallback, null)
        refreshDevices()
    }

    fun refreshDevices() {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val mapped = devices
            .filter { it.isSink }
            .map { info ->
                AudioOutputDevice(
                    id = info.id,
                    name = info.productName?.toString()?.ifBlank { getDeviceTypeName(info.type) }
                        ?: getDeviceTypeName(info.type),
                    type = mapDeviceType(info.type)
                )
            }
            // Only show Speaker and Bluetooth devices
            .filter { it.type == AudioDeviceType.SPEAKER || it.type == AudioDeviceType.BLUETOOTH }
            .distinctBy { it.type }

        // Always include the phone speaker
        val hasBuiltinSpeaker = mapped.any { it.type == AudioDeviceType.SPEAKER }
        val finalList = if (!hasBuiltinSpeaker) {
            listOf(
                AudioOutputDevice(
                    id = -1,
                    name = "Phone Speaker",
                    type = AudioDeviceType.SPEAKER
                )
            ) + mapped
        } else {
            mapped
        }

        _availableDevices.value = finalList
        _activeDevice.value = detectActiveDevice(finalList)
    }

    private fun detectActiveDevice(devices: List<AudioOutputDevice>): AudioOutputDevice? {
        // Priority: Bluetooth > Wired > Speaker
        return devices.firstOrNull { it.type == AudioDeviceType.BLUETOOTH }
            ?: devices.firstOrNull { it.type == AudioDeviceType.WIRED_HEADPHONES }
            ?: devices.firstOrNull { it.type == AudioDeviceType.SPEAKER }
    }

    fun switchToDevice(device: AudioOutputDevice) {
        _activeDevice.value = device
        _deviceSwitchRequested.value = true

        // On Android 12+ (API 31), use setCommunicationDevice
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val targetDevice = audioDevices.firstOrNull { it.id == device.id }
            if (targetDevice != null) {
                audioManager.setCommunicationDevice(targetDevice)
            }
        }
    }

    fun setResumeAfterSwitch(enabled: Boolean) {
        _resumeAfterSwitch.value = enabled
    }

    fun consumeSwitchEvent() {
        _deviceSwitchRequested.value = false
    }

    private fun mapDeviceType(type: Int): AudioDeviceType {
        return when (type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> AudioDeviceType.SPEAKER

            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> AudioDeviceType.WIRED_HEADPHONES

            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> AudioDeviceType.BLUETOOTH

            else -> AudioDeviceType.OTHER
        }
    }

    private fun getDeviceTypeName(type: Int): String {
        return when (type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Phone Speaker"
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "Earpiece"
            AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired Headset"
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired Headphones"
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth Audio"
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth SCO"
            AudioDeviceInfo.TYPE_USB_DEVICE -> "USB Audio"
            else -> "Audio Device"
        }
    }

    fun release() {
        audioManager.unregisterAudioDeviceCallback(deviceCallback)
    }
}
