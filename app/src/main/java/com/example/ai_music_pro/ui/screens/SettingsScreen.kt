package com.example.ai_music_pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.RadioButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ai_music_pro.ui.viewmodel.EqualizerViewModel
import com.example.ai_music_pro.ui.viewmodel.ThemeMode
import com.example.ai_music_pro.ui.viewmodel.ThemeViewModel
import com.example.ai_music_pro.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    themeViewModel: ThemeViewModel = hiltViewModel(),
    eqViewModel: EqualizerViewModel = hiltViewModel()
) {
    val themeMode by themeViewModel.themeMode.collectAsState()
    val isEqEnabled by eqViewModel.isEnabled.collectAsState()
    val bands by eqViewModel.bands.collectAsState()
    val presets by eqViewModel.presets.collectAsState()
    
    var notifications by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Settings", color = MaterialTheme.colorScheme.onSurface) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        Column(
            modifier = Modifier
                .padding(Dimens.PaddingDefault)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "Display", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(Dimens.PaddingDefault))
            
            ThemeButton("Dark", themeMode == ThemeMode.DARK) { themeViewModel.setTheme(ThemeMode.DARK) }
            ThemeButton("Light", themeMode == ThemeMode.LIGHT) { themeViewModel.setTheme(ThemeMode.LIGHT) }
            ThemeButton("System", themeMode == ThemeMode.SYSTEM) { themeViewModel.setTheme(ThemeMode.SYSTEM) }

            Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Equalizer", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Switch(checked = isEqEnabled, onCheckedChange = { eqViewModel.setEnabled(it) })
            }
            
            if (isEqEnabled) {
                Spacer(modifier = Modifier.height(Dimens.PaddingDefault))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(Dimens.RadiusMedium)
                ) {
                    Column(modifier = Modifier.padding(Dimens.PaddingDefault)) {
                        bands.forEach { band ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "${band.freq} Hz", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                                    Text(text = "${band.level / 100} dB", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                                }
                                Slider(
                                    value = band.level.toFloat(),
                                    onValueChange = { eqViewModel.setBandLevel(band.bandId, it.toInt().toShort()) },
                                    valueRange = band.min.toFloat()..band.max.toFloat(),
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
            Text(text = "Account", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(Dimens.PaddingDefault))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Push Notifications", color = MaterialTheme.colorScheme.onSurface)
                Switch(checked = notifications, onCheckedChange = { notifications = it })
            }

            Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
            Text(text = "Other", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(Dimens.PaddingDefault))
            Text(text = "About AI Music Pro", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 8.dp))
            Text(text = "Privacy Policy", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 8.dp))
            Text(text = "Terms of Service", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun ThemeButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        Spacer(modifier = Modifier.width(Dimens.PaddingDefault))
        Text(text = label, color = MaterialTheme.colorScheme.onSurface)
    }
}
