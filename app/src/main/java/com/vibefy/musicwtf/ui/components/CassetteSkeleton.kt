package com.vibefy.musicwtf.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp

private val shimmerColors = listOf(
    Color.White.copy(alpha = 0.04f),
    Color.White.copy(alpha = 0.10f),
    Color.White.copy(alpha = 0.04f),
)

@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -600f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_x",
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateX, 0f),
        end = Offset(translateX + 600f, 400f),
    )
}

/**
 * Cassette-deck shaped skeleton card — same shape as CassetteCard.
 * Used in CatalogScreen while playlists are loading.
 */
@Composable
fun CassetteSkeleton() {
    val brush = shimmerBrush()

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF151821))
            .padding(4.dp),
    ) {
        // Cassette window strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x160A0E16))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(brush))
            Box(Modifier.weight(1f).height(4.dp).padding(horizontal = 4.dp).clip(RoundedCornerShape(2.dp)).background(brush))
            Box(Modifier.size(12.dp).clip(CircleShape).background(brush))
        }

        Spacer(Modifier.height(3.dp))

        // CRT screen
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(6.dp))
                .background(brush)
        )

        // LED + label strip
        Row(
            Modifier.padding(top = 3.dp, start = 2.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(Modifier.size(5.dp).clip(CircleShape).background(brush))
            Box(Modifier.fillMaxWidth(0.55f).height(9.dp).clip(RoundedCornerShape(3.dp)).background(brush))
        }

        // Title + desc skeleton lines
        Column(Modifier.padding(top = 6.dp, start = 2.dp, end = 2.dp, bottom = 2.dp)) {
            Box(Modifier.fillMaxWidth(0.78f).height(13.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            Spacer(Modifier.height(5.dp))
            Box(Modifier.fillMaxWidth(0.55f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth(0.40f).height(9.dp).clip(RoundedCornerShape(4.dp)).background(brush))
        }
    }
}
