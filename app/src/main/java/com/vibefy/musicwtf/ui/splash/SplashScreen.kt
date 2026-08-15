package com.vibefy.musicwtf.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vibefy.musicwtf.ui.theme.Amber
import com.vibefy.musicwtf.ui.theme.YatraOne
import com.vibefy.musicwtf.ui.theme.JetBrainsMono
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Native Android Calligraphic Handwriting Splash Screen.
 *
 * "Gaane" is progressively written across the screen left-to-right
 * with a glowing ink tip tracer particle riding the stroke edge,
 * followed by the "WTF!" burst in Yatra One and Netflix zoom exit.
 */
@Composable
fun SplashScreen(onComplete: () -> Unit) {

    // ── Animation States ───────────────────────────────────────────
    val bgAlpha       = remember { Animatable(0f) }
    val bgScale       = remember { Animatable(1.08f) }

    // Handwriting stroke progress (0.0f -> 1.0f)
    val writeProgress = remember { Animatable(0f) }
    val inkTipAlpha   = remember { Animatable(0f) }

    // "WTF!" burst
    val wtfAlpha      = remember { Animatable(0f) }
    val wtfScale      = remember { Animatable(2.6f) }

    // Tagline
    val tagAlpha      = remember { Animatable(0f) }
    val tagOffsetY    = remember { Animatable(16f) }

    // Final exit
    val exitScale     = remember { Animatable(1f) }
    val exitAlpha     = remember { Animatable(1f) }

    val iosBack = spring<Float>(
        stiffness = Spring.StiffnessMedium,
        dampingRatio = Spring.DampingRatioLowBouncy,
    )

    LaunchedEffect(Unit) {
        // 1. Background fade-in
        bgAlpha.animateTo(1f, tween(1100, easing = FastOutSlowInEasing))
        bgScale.animateTo(1f, tween(1100, easing = FastOutSlowInEasing))

        delay(150)

        // 2. Ink tip spark fades in
        launch { inkTipAlpha.animateTo(1f, tween(150)) }

        // 3. PURE HANDWRITING STROKE ANIMATION (0f -> 1f)
        writeProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1350, easing = FastOutSlowInEasing)
        )

        // Ink tip spark fades out
        launch { inkTipAlpha.animateTo(0f, tween(250)) }

        delay(80)

        // 4. "WTF!" bursts in
        launch { wtfAlpha.animateTo(1f, tween(250)) }
        launch { wtfScale.animateTo(1f, iosBack) }

        delay(250)

        // 5. Tagline drifts up
        launch { tagAlpha.animateTo(1f, tween(450)) }
        launch { tagOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) }

        delay(850)

        // 6. Netflix exit zoom & fade
        launch { exitScale.animateTo(1.7f, tween(620, easing = FastOutLinearInEasing)) }
        launch { exitAlpha.animateTo(0f, tween(580, easing = FastOutLinearInEasing)) }
        bgAlpha.animateTo(0f, tween(620, easing = FastOutLinearInEasing))

        delay(620)
        onComplete()
    }

    // ── Render ─────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07080C)),
        contentAlignment = Alignment.Center,
    ) {

        // Mountain Background
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("file:///android_asset/backgrounds/mountainn.png")
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(bgAlpha.value)
                .scale(bgScale.value),
        )

        // Dark Radial Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF07080C).copy(alpha = 0.62f),
                            Color(0xFF07080C).copy(alpha = 0.88f),
                        )
                    )
                )
        )

        // Lockup Container
        Column(
            modifier = Modifier
                .scale(exitScale.value)
                .alpha(exitAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            // ── Handwriting "Gaane" Typography Canvas ─────────────
            Box(
                modifier = Modifier
                    .width(320.dp)
                    .height(90.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                // Progressive clip reveal of Yatra One "Gaane"
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            // Clip canvas left-to-right as handwriting progresses
                            clipRect(right = size.width * writeProgress.value) {
                                this@drawWithContent.drawContent()
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Gaane",
                        fontFamily = YatraOne,
                        fontSize = 82.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 82.sp,
                    )
                }

                // Glowing Ink Tip Spark riding along the stroke edge
                if (inkTipAlpha.value > 0f) {
                    Box(
                        modifier = Modifier
                            .offset(x = (320.dp * writeProgress.value) - 8.dp)
                            .size(16.dp)
                            .alpha(inkTipAlpha.value)
                            .clip(CircleShape)
                            .background(Color.White)
                            .drawBehind {
                                drawCircle(
                                    color = Amber,
                                    radius = 16.dp.toPx(),
                                    center = center,
                                )
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── "WTF!" Amber Burst ─────────────────────────────────
            Text(
                text = "WTF!",
                fontFamily = YatraOne,
                fontSize = 34.sp,
                letterSpacing = 8.sp,
                color = Amber,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(wtfAlpha.value)
                    .scale(wtfScale.value),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Tagline ───────────────────────────────────────────
            Text(
                text = "CURATED VIBE. EVERY MOOD.",
                fontFamily = JetBrainsMono,
                fontSize = 10.sp,
                letterSpacing = 3.sp,
                color = Color.White.copy(alpha = 0.42f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(tagAlpha.value)
                    .offset(y = tagOffsetY.value.dp),
            )
        }
    }
}
