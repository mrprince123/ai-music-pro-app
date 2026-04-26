package com.example.ai_music_pro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.example.ai_music_pro.domain.model.CarouselItem
import com.example.ai_music_pro.ui.theme.Dimens

@Composable
fun BannerSection(carousels: List<CarouselItem>) {
    if (carousels.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { carousels.size })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .padding(Dimens.PaddingDefault)
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(Dimens.RadiusExtraLarge)),
        pageSpacing = Dimens.PaddingSmall
    ) { page ->
        val item = carousels[page]
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.image,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )
            item.title?.let { title ->
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }
        }
    }
}
