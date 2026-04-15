package com.example.ai_music_pro.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShimmerItem(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    )

    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(brush)
    )
}

@Composable
fun HomeShimmer() {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        ShimmerItem(modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp)))
        Spacer(modifier = Modifier.height(24.dp))
        ShimmerItem(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)))
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Mood & Genres", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        CategoryShimmer()
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Popular Today", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        AlbumGridShimmer()
    }
}

@Composable
fun CategoryShimmer() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(5) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ShimmerItem(modifier = Modifier.size(70.dp).clip(CircleShape))
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerItem(modifier = Modifier.width(40.dp).height(12.dp))
            }
        }
    }
}

@Composable
fun AlbumGridShimmer() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            Column {
                ShimmerItem(modifier = Modifier.size(140.dp).clip(RoundedCornerShape(12.dp)))
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerItem(modifier = Modifier.width(100.dp).height(14.dp))
            }
        }
    }
}

@Composable
fun SongListShimmer() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(8) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerItem(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerItem(modifier = Modifier.width(150.dp).height(16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerItem(modifier = Modifier.width(100.dp).height(12.dp))
                }
            }
        }
    }
}
