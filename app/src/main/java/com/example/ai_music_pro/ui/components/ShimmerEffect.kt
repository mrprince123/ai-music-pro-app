package com.example.ai_music_pro.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerItem(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        Color.DarkGray.copy(alpha = 0.6f),
        Color.DarkGray.copy(alpha = 0.2f),
        Color.DarkGray.copy(alpha = 0.6f),
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
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                ShimmerItem(modifier = Modifier.fillMaxWidth().height(50.dp).padding(4.dp))
                ShimmerItem(modifier = Modifier.fillMaxWidth().height(50.dp).padding(4.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                ShimmerItem(modifier = Modifier.fillMaxWidth().height(50.dp).padding(4.dp))
                ShimmerItem(modifier = Modifier.fillMaxWidth().height(50.dp).padding(4.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        ShimmerItem(modifier = Modifier.fillMaxWidth().height(180.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(3) {
                ShimmerItem(modifier = Modifier.size(140.dp))
            }
        }
    }
}
