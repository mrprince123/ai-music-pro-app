package com.example.ai_music_pro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ai_music_pro.ui.theme.Dimens
import com.example.ai_music_pro.ui.theme.LunkgemBlue
import com.example.ai_music_pro.ui.theme.SurfaceGray

@Composable
fun RoomJoinDialog(
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit,
    onCreate: () -> Unit
) {
    var roomId by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.PaddingDefault),
            shape = RoundedCornerShape(Dimens.RadiusExtraLarge),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp, 
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Sync & Listen",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
                
                Text(
                    text = "Enter a Room ID to join your friends or create a new room.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = roomId,
                    onValueChange = { roomId = it },
                    label = { Text("Room ID") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.RadiusMedium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { if (roomId.isNotEmpty()) onJoin(roomId) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(Dimens.RadiusMedium),
                    enabled = roomId.isNotEmpty()
                ) {
                    Text("Join Session", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(Dimens.PaddingSmall))

                TextButton(
                    onClick = onCreate,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Create New Room", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
