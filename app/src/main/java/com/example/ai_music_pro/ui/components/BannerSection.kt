package com.example.ai_music_pro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai_music_pro.ui.theme.Dimens

@Composable
fun BannerSection() {
    Box(
        modifier = Modifier
            .padding(Dimens.PaddingDefault)
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(Dimens.RadiusExtraLarge))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1496293455970-f8581aae0e3c?q=80&w=2070&auto=format&fit=crop",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
                .padding(Dimens.PaddingDefault),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = "LOVE IS",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "BLIND",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
                Text(
                    text = "Discover 21 songs",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
