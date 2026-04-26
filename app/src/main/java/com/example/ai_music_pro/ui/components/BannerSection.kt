package com.example.ai_music_pro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.ai_music_pro.ui.theme.SpotifyGreen

@Composable
fun BannerSection(carousels: List<CarouselItem>) {
    val displayItems = if (carousels.isEmpty()) {
        listOf(
            CarouselItem(
                _id = "default_1",
                title = "Discover New AI Music",
                image = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?q=80&w=2070&auto=format&fit=crop"
            )
        )
    } else carousels

    val pagerState = rememberPagerState(pageCount = { displayItems.size })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .padding(Dimens.PaddingDefault)
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(Dimens.RadiusExtraLarge))
            .border(
                0.5.dp, 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), 
                RoundedCornerShape(Dimens.RadiusExtraLarge)
            ),
        pageSpacing = Dimens.PaddingSmall
    ) { page ->
        val item = displayItems[page]
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
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                item.title?.let { title ->
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = SpotifyGreen,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "Listen Now",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
