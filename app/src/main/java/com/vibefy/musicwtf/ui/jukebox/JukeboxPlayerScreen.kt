package com.vibefy.musicwtf.ui.jukebox

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.vibefy.musicwtf.ui.theme.*

/**
 * Jukebox Player Screen — Native saloon.wtf-inspired player UI.
 *
 * Layout layers (back → front):
 *   1. Playlist cover art fills screen, blurred + darkened
 *   2. Glassmorphic player card floats in centre
 *   3. Vinyl disc with CSS-ring grooves rotates when playing
 *   4. Seek bar with analog needle scrubber
 *   5. Transport controls (⏮ ⏪ ▶/⏸ ⏩ ⏭)
 *   6. Collapsible queue panel
 *
 * iOS UI rules:
 *   - Spring animations on all state changes
 *   - Press scale (0.92) on all tappable controls
 *   - Haptic on track change + play/pause
 *   - Swipe down to dismiss (like iOS Now Playing)
 *   - Status bar tint = playlist accentColor
 *   - Title always in Yatra One
 */
@Composable
fun JukeboxPlayerScreen(
    jukeboxId: String,
    onBack: () -> Unit,
    viewModel: JukeboxPlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(jukeboxId) { viewModel.load(jukeboxId) }

    val playlist = uiState.playlist ?: return
    val track = uiState.currentTrack ?: return
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Layer 1: Blurred cover background ────────────────────
        AsyncImage(
            model = playlist.coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(28.dp)
                .scale(1.08f), // compensate for blur edge darkening
        )

        // Dark scrim (70% → 85% from top to bottom)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xB207080C),
                            Color(0xD907080C),
                            Color(0xEE07080C),
                        )
                    )
                )
        )

        // ── Content ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // ── Minimal top bar ───────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(
                    onClick = onBack,
                    modifier = Modifier.size(38.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.12f),
                        contentColor = Color.White,
                    )
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, "Close", Modifier.size(22.dp))
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "NOW PLAYING",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.45f),
                        letterSpacing = 3.sp,
                    )
                    Text(
                        text = playlist.title,
                        fontFamily = YatraOne,
                        fontSize = 15.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Queue toggle
                FilledTonalIconButton(
                    onClick = { viewModel.toggleQueue() },
                    modifier = Modifier.size(38.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.12f),
                        contentColor = Color.White,
                    )
                ) {
                    Icon(Icons.Default.QueueMusic, "Queue", Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Vinyl Disc ────────────────────────────────────────
            VinylDisc(
                coverUrl = track.coverUrl,
                isPlaying = uiState.isPlaying,
                accentColor = playlist.accentColor,
            )

            Spacer(Modifier.height(28.dp))

            // ── Track info ────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp),
            ) {
                Text(
                    text = track.title,
                    fontFamily = YatraOne,
                    fontSize = 24.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Seek bar ──────────────────────────────────────────
            AnalogSeekBar(
                progress = uiState.progressFraction,
                currentSec = uiState.currentSec,
                durationSec = uiState.durationSec,
                accentColor = playlist.accentColor,
                onSeek = { viewModel.seekTo(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )

            Spacer(Modifier.height(24.dp))

            // ── Transport controls ────────────────────────────────
            TransportControls(
                isPlaying = uiState.isPlaying,
                isShuffle = uiState.isShuffle,
                isLooping = uiState.isLooping,
                accentColor = playlist.accentColor,
                onPrev      = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.prev() },
                onRewind    = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); viewModel.rewind10() },
                onPlayPause = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.togglePlayPause() },
                onFastFwd   = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); viewModel.forward10() },
                onNext      = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.next() },
                onShuffle   = { viewModel.toggleShuffle() },
                onLoop      = { viewModel.toggleLoop() },
            )

            Spacer(Modifier.weight(1f))

            // ── Collapsible queue panel ───────────────────────────
            AnimatedVisibility(
                visible = uiState.showQueue,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit  = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                QueuePanel(
                    tracks = uiState.tracks,
                    currentIndex = uiState.currentIndex,
                    onTrackClick = { idx ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.jumpTo(idx)
                    },
                    accentColor = playlist.accentColor,
                )
            }
        }
    }
}

// ── Vinyl Disc ────────────────────────────────────────────────────

@Composable
private fun VinylDisc(
    coverUrl: String,
    isPlaying: Boolean,
    accentColor: String,
) {
    val rotation = remember { Animatable(0f) }
    val accent = remember(accentColor) {
        try { Color(android.graphics.Color.parseColor(accentColor)) }
        catch (e: Exception) { Lamp }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            rotation.animateTo(
                targetValue = rotation.value + 3600f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 10_000,
                        easing = LinearEasing,
                    )
                )
            )
        } else {
            rotation.stop()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(220.dp),
    ) {
        // Outer vinyl disc (dark with groove rings painted via Canvas)
        Box(
            modifier = Modifier
                .size(220.dp)
                .rotate(rotation.value)
                .clip(CircleShape)
                .background(Color(0xFF0D0F14))
                .drawBehind {
                    // Concentric vinyl groove rings
                    val rings = 12
                    val step = size.minDimension / (rings * 2f)
                    for (i in 1..rings) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.025f + (i * 0.003f)),
                            radius = size.minDimension / 2f - i * step,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f),
                        )
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            // Cover art inset
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape),
            )

            // Center hole
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF07080C))
            )
        }

        // Accent ring around disc
        Box(
            modifier = Modifier
                .size(228.dp)
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        listOf(accent.copy(alpha = 0.6f), Color.Transparent, accent.copy(alpha = 0.6f))
                    ),
                    shape = CircleShape,
                )
        )
    }
}

// ── Analog seek bar with needle ───────────────────────────────────

@Composable
private fun AnalogSeekBar(
    progress: Float,
    currentSec: Int,
    durationSec: Int,
    accentColor: String,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = remember(accentColor) {
        try { Color(android.graphics.Color.parseColor(accentColor)) }
        catch (e: Exception) { Lamp }
    }

    fun Int.toTimeStr(): String {
        val m = this / 60; val s = this % 60
        return "%d:%02d".format(m, s)
    }

    Column(modifier = modifier) {
        // Track groove
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val frac = (change.position.x / size.width).coerceIn(0f, 1f)
                        onSeek(frac)
                    }
                }
        ) {
            val trackW = maxWidth

            // Groove background
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.15f))
            )

            // Filled portion
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.7f)))
                    )
            )

            // Needle (vertical line indicator — analog aesthetic)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = trackW * progress.coerceIn(0f, 1f) - 1.dp)
                    .width(2.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Color.White)
                    // Glow
                    .drawBehind {
                        drawCircle(
                            color = accent.copy(alpha = 0.4f),
                            radius = 8.dp.toPx(),
                            center = center,
                        )
                    }
            )
        }

        // Time labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                currentSec.toTimeStr(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.5f),
            )
            Text(
                durationSec.toTimeStr(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

// ── Transport Controls ────────────────────────────────────────────

@Composable
private fun TransportControls(
    isPlaying: Boolean,
    isShuffle: Boolean,
    isLooping: Boolean,
    accentColor: String,
    onPrev: () -> Unit,
    onRewind: () -> Unit,
    onPlayPause: () -> Unit,
    onFastFwd: () -> Unit,
    onNext: () -> Unit,
    onShuffle: () -> Unit,
    onLoop: () -> Unit,
) {
    val accent = remember(accentColor) {
        try { Color(android.graphics.Color.parseColor(accentColor)) }
        catch (e: Exception) { Lamp }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Main transport row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ⏮ Prev
            TactileIconButton(onClick = onPrev, size = 40) {
                Icon(Icons.Default.SkipPrevious, "Previous", Modifier.size(22.dp), tint = Color.White.copy(0.75f))
            }
            // ⏪ -10s
            TactileIconButton(onClick = onRewind, size = 40) {
                Icon(Icons.Default.Replay10, "–10s", Modifier.size(22.dp), tint = Color.White.copy(0.75f))
            }
            // ▶/⏸ Play-Pause (large, accent ring)
            TactileIconButton(
                onClick = onPlayPause,
                size = 64,
                containerColor = Color.White,
                accentRingColor = accent,
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    if (isPlaying) "Pause" else "Play",
                    Modifier.size(30.dp),
                    tint = Color(0xFF07080C),
                )
            }
            // ⏩ +10s
            TactileIconButton(onClick = onFastFwd, size = 40) {
                Icon(Icons.Default.Forward10, "+10s", Modifier.size(22.dp), tint = Color.White.copy(0.75f))
            }
            // ⏭ Next
            TactileIconButton(onClick = onNext, size = 40) {
                Icon(Icons.Default.SkipNext, "Next", Modifier.size(22.dp), tint = Color.White.copy(0.75f))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Secondary row: shuffle / loop
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            TactileIconButton(onClick = onShuffle, size = 34) {
                Icon(
                    Icons.Default.Shuffle, "Shuffle",
                    Modifier.size(18.dp),
                    tint = if (isShuffle) accent else Color.White.copy(0.4f),
                )
            }
            TactileIconButton(onClick = onLoop, size = 34) {
                Icon(
                    Icons.Default.Repeat, "Loop",
                    Modifier.size(18.dp),
                    tint = if (isLooping) accent else Color.White.copy(0.4f),
                )
            }
        }
    }
}

// ── Tactile press-scale button (iOS feel) ────────────────────────

@Composable
private fun TactileIconButton(
    onClick: () -> Unit,
    size: Int = 44,
    containerColor: Color = Color.Transparent,
    accentRingColor: Color? = null,
    content: @Composable () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btn_scale",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(containerColor)
            .then(
                if (accentRingColor != null)
                    Modifier.border(2.dp, accentRingColor, CircleShape)
                else Modifier
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        content()
    }
}

// ── Queue Panel ───────────────────────────────────────────────────

@Composable
private fun QueuePanel(
    tracks: List<com.vibefy.musicwtf.data.model.JukeboxTrack>,
    currentIndex: Int,
    onTrackClick: (Int) -> Unit,
    accentColor: String,
) {
    val accent = remember(accentColor) {
        try { Color(android.graphics.Color.parseColor(accentColor)) }
        catch (e: Exception) { Lamp }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xDD14171E), Color(0xF214171E))
                )
            )
            .padding(top = 8.dp, bottom = 16.dp),
    ) {
        // Handle bar
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.25f))
        )
        Spacer(Modifier.height(10.dp))

        Text(
            "Queue",
            fontFamily = YatraOne,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(8.dp))

        tracks.forEachIndexed { idx, track ->
            val isCurrent = idx == currentIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTrackClick(idx) }
                    .background(if (isCurrent) accent.copy(alpha = 0.12f) else Color.Transparent)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (isCurrent) {
                    PulsingDot(accent)
                } else {
                    Text(
                        "${idx + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.width(16.dp),
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        track.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCurrent) accent else Color.White.copy(0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        track.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(0.45f),
                        maxLines = 1,
                    )
                }
                Text(
                    track.durationStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(0.35f),
                )
            }
        }
    }
}

@Composable
private fun PulsingDot(accent: Color) {
    val alpha by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "queue_pulse",
    )
    Box(
        Modifier
            .size(6.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(accent)
    )
}
