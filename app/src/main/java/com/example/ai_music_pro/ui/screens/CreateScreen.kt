package com.example.ai_music_pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ai_music_pro.ui.theme.LunkgemBlue
import com.example.ai_music_pro.ui.theme.SurfaceGray
import com.example.ai_music_pro.ui.viewmodel.CreateAlbumViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    viewModel: CreateAlbumViewModel = hiltViewModel(),
    onComplete: (String) -> Unit = {}
) {
    var albumName by remember { mutableStateOf("") }
    val songs by viewModel.songs.collectAsState()
    val selectedIds by viewModel.selectedSongIds.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        SnackbarHost(hostState = snackbarHostState)
        Text(
            text = "Create Album",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        TextField(
            value = albumName,
            onValueChange = { albumName = it },
            placeholder = { Text("Album Name", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceGray,
                unfocusedContainerColor = SurfaceGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Add Songs", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(songs) { song ->
                val isSelected = selectedIds.contains(song._id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleSongSelection(song._id) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) LunkgemBlue else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = song.title, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = song.artist, color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }

        Button(
            onClick = {
                viewModel.createAlbum(albumName) { albumId ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Album created successfully")
                    }
                    onComplete(albumId)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LunkgemBlue),
            enabled = albumName.isNotEmpty() && selectedIds.isNotEmpty()
        ) {
            Text("Create Album", color = Color.Black, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}
