package com.example.ai_music_pro.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.ai_music_pro.ui.viewmodel.BandState
import com.example.ai_music_pro.ui.viewmodel.EqualizerViewModel
import com.example.ai_music_pro.ui.viewmodel.ThemeMode
import com.example.ai_music_pro.ui.viewmodel.ThemeViewModel
import com.example.ai_music_pro.ui.theme.Dimens
import com.example.ai_music_pro.ui.theme.SpotifyGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    themeViewModel: ThemeViewModel = hiltViewModel(),
    eqViewModel: EqualizerViewModel = hiltViewModel(),
    authViewModel: com.example.ai_music_pro.ui.auth.AuthViewModel = hiltViewModel()
) {
    val themeMode by themeViewModel.themeMode.collectAsState()
    val isEqEnabled by eqViewModel.isEnabled.collectAsState()
    val bands by eqViewModel.bands.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    
    val currentUser = (authState as? com.example.ai_music_pro.ui.auth.AuthState.Success)?.user
    
    var notifications by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings", 
                        color = MaterialTheme.colorScheme.onSurface, 
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back", 
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Profile Section
            currentUser?.let { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .clickable { /* Profile edit? */ },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = user.profilePhoto ?: "https://ui-avatars.com/api/?name=${user.name}&background=random",
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "View Profile",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            SettingsGroup(title = "Display") {
                SettingsRow(
                    title = "Theme Mode",
                    icon = Icons.Default.Brightness4,
                    trailing = {
                        ThemeSelector(
                            currentMode = themeMode,
                            onModeSelected = { themeViewModel.setTheme(it) }
                        )
                    }
                )
            }

            SettingsGroup(title = "Audio Quality") {
                SettingsRow(
                    title = "Equalizer",
                    icon = Icons.Default.GraphicEq,
                    subtitle = if (isEqEnabled) "Custom" else "Off",
                    onClick = { eqViewModel.setEnabled(!isEqEnabled) },
                    trailing = {
                        Switch(
                            checked = isEqEnabled,
                            onCheckedChange = { eqViewModel.setEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.surface,
                                checkedTrackColor = SpotifyGreen,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                )
                
                if (isEqEnabled) {
                    Box(modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)) {
                        EqualizerPanel(bands = bands, eqViewModel = eqViewModel)
                    }
                }
            }

            SettingsGroup(title = "Notifications") {
                SettingsRow(
                    title = "Push Notifications",
                    icon = Icons.Default.Notifications,
                    trailing = {
                        Switch(
                            checked = notifications,
                            onCheckedChange = { notifications = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.surface,
                                checkedTrackColor = SpotifyGreen,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                )
            }

            SettingsGroup(title = "General") {
                SettingsRow(
                    title = "Privacy Policy",
                    icon = Icons.Default.PrivacyTip,
                    onClick = { /* Navigate */ }
                )
                SettingsRow(
                    title = "Terms of Service",
                    icon = Icons.Default.Description,
                    onClick = { /* Navigate */ }
                )
                SettingsRow(
                    title = "About AI Music Pro",
                    icon = Icons.Default.Info,
                    subtitle = "Version 1.0.0",
                    onClick = { /* Navigate */ }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = {
                        authViewModel.logout()
                        onLogout()
                    }
                ) {
                    Text(
                        "Log out",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        content()
    }
}

@Composable
fun SettingsRow(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
fun ThemeSelector(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ThemeIcon(Icons.Default.DarkMode, currentMode == ThemeMode.DARK) { onModeSelected(ThemeMode.DARK) }
        ThemeIcon(Icons.Default.LightMode, currentMode == ThemeMode.LIGHT) { onModeSelected(ThemeMode.LIGHT) }
        ThemeIcon(Icons.Default.SettingsSuggest, currentMode == ThemeMode.SYSTEM) { onModeSelected(ThemeMode.SYSTEM) }
    }
}

@Composable
fun ThemeIcon(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isSelected) SpotifyGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun EqualizerPanel(
    bands: List<BandState>,
    eqViewModel: EqualizerViewModel
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            for (band in bands) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "${band.freq} Hz", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
                        Text(text = "${band.level / 100} dB", color = SpotifyGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = band.level.toFloat(),
                        onValueChange = { eqViewModel.setBandLevel(band.bandId, it.toInt().toShort()) },
                        valueRange = band.min.toFloat()..band.max.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.onSurface,
                            activeTrackColor = SpotifyGreen,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}
