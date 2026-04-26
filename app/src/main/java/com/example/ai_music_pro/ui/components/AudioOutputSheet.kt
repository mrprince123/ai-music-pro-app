package com.example.ai_music_pro.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai_music_pro.domain.model.AudioDeviceType
import com.example.ai_music_pro.domain.model.AudioOutputDevice
import com.example.ai_music_pro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioOutputSheet(
    availableDevices: List<AudioOutputDevice>,
    activeDevice: AudioOutputDevice?,
    resumeAfterSwitch: Boolean,
    onDeviceSelect: (AudioOutputDevice) -> Unit,
    onResumeToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    // Ensure we always have both device options
    val speakerDevice = availableDevices.find { it.type == AudioDeviceType.SPEAKER }
        ?: AudioOutputDevice(id = -1, name = "This Phone", type = AudioDeviceType.SPEAKER)
    val bluetoothDevice = availableDevices.find { it.type == AudioDeviceType.BLUETOOTH }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        containerColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SpotifyGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speaker,
                        contentDescription = null,
                        tint = SpotifyGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Select a device",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose where to play audio",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                }
            }

            // ─── This Phone ───
            DeviceRow(
                device = speakerDevice,
                displayName = "This Phone",
                subtitle = "Phone Speaker",
                icon = Icons.Default.PhoneAndroid,
                isActive = activeDevice == null || activeDevice.type == AudioDeviceType.SPEAKER,
                onClick = { onDeviceSelect(speakerDevice) }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ─── Bluetooth ───
            if (bluetoothDevice != null) {
                DeviceRow(
                    device = bluetoothDevice,
                    displayName = bluetoothDevice.name,
                    subtitle = "Bluetooth · Connected",
                    icon = Icons.Default.Bluetooth,
                    isActive = activeDevice?.type == AudioDeviceType.BLUETOOTH,
                    onClick = { onDeviceSelect(bluetoothDevice) }
                )
            } else {
                // Show disabled Bluetooth option
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.06f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bluetooth",
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "No device connected",
                                color = Color.White.copy(alpha = 0.2f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Resume after switch toggle
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Resume After Switching",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Auto-resume playback after device change",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = resumeAfterSwitch,
                    onCheckedChange = onResumeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SpotifyGreen,
                        uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}

@Composable
private fun DeviceRow(
    device: AudioOutputDevice,
    displayName: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) SpotifyGreen.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(300),
        label = "deviceBg"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) SpotifyGreen.copy(alpha = 0.2f)
                        else Color.White.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) SpotifyGreen else DeviceIconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    color = if (isActive) SpotifyGreen else Color.White,
                    fontSize = 15.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = if (isActive) SpotifyGreen.copy(alpha = 0.7f)
                    else Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp
                )
            }

            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(SpotifyGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
