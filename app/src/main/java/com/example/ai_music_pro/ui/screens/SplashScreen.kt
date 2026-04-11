package com.example.ai_music_pro.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.ai_music_pro.R
import com.example.ai_music_pro.ui.theme.SpotifyGreen
import kotlin.math.roundToInt

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        // Hero Image Background (Darker/Sleeker)
        Image(
            painter = rememberAsyncImagePainter(
                model = "https://images.unsplash.com/photo-1514525253361-bee8d40d421c?q=80&w=2070&auto=format&fit=crop",
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.6f)
        )

        // Gradient Overlay (Spotify-like Dark)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f),
                            Color.Black
                        )
                    )
                )
        )

        // Contents
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MILLIONS OF SONGS.\nFREE ON AI MUSIC PRO.",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 38.sp,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Swipe to Get Started Button
            SwipeToStartButton(onComplete = onSplashComplete)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SwipeToStartButton(onComplete: () -> Unit) {
    val buttonWidth = 300.dp
    val buttonHeight = 64.dp
    val thumbSize = 52.dp
    val density = LocalDensity.current
    val maxOffset = with(density) { (buttonWidth - thumbSize - 12.dp).toPx() }
    
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX)

    LaunchedEffect(offsetX) {
        if (offsetX >= maxOffset) {
            onComplete()
        }
    }

    Box(
        modifier = Modifier
            .width(buttonWidth)
            .height(buttonHeight)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.1f)),
        contentAlignment = Alignment.CenterStart
    ) {
        // Track Text
        Text(
            text = "READY TO LISTEN?",
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        // Thumb
        Box(
            modifier = Modifier
                .padding(6.dp)
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .size(thumbSize)
                .clip(CircleShape)
                .background(SpotifyGreen)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        offsetX = (offsetX + delta).coerceIn(0f, maxOffset)
                    },
                    onDragStopped = {
                        if (offsetX < maxOffset) {
                            offsetX = 0f
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
