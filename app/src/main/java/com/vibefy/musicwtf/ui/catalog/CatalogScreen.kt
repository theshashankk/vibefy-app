package com.vibefy.musicwtf.ui.catalog

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.vibefy.musicwtf.data.model.PlaylistEntry
import com.vibefy.musicwtf.ui.components.CassetteSkeleton
import com.vibefy.musicwtf.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onPlaylistClick: (String) -> Unit,
    onSubmitClick: () -> Unit,
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // ── iOS large-title collapsing header ──────────────────
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Gaane",
                        fontFamily = YatraOne,
                        fontSize = 34.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                actions = {
                    IconButton(onClick = onSubmitClick) {
                        Icon(Icons.Outlined.Add, contentDescription = "Submit playlist")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 96.dp, // clear pill nav
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {

            // ── Offline Mode Status Pill ──────────────────────────
            if (uiState.isOffline) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Amber.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("⚡", fontSize = 14.sp)
                        Text(
                            "Offline Mode — Showing Cached Playlists",
                            style = MaterialTheme.typography.labelLarge,
                            color = Amber,
                        )
                    }
                }
            }

            // ── Search bar ────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                IosSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                )
            }

            // ── Mood chips ────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                MoodChipsRow(
                    categories = uiState.categories,
                    active = uiState.activeCategory,
                    onSelect = { cat ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onCategorySelect(cat)
                    }
                )
            }

            // ── Playlist count ────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "${uiState.visiblePlaylists.size} playlists" +
                        if (uiState.activeCategory != "All") " in ${uiState.activeCategory}" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }

            // ── Skeleton or real cards ────────────────────────────
            if (uiState.isLoading) {
                items(12) { CassetteSkeleton() }
            } else {
                itemsIndexed(
                    items = uiState.visiblePlaylists,
                    key = { _, p -> p.id },
                ) { index, playlist ->
                    CassetteCard(
                        playlist = playlist,
                        index = index,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPlaylistClick(playlist.id)
                        }
                    )
                }
            }
        }
    }
}

// ── iOS-style search bar ──────────────────────────────────────────
@Composable
private fun IosSearchBar(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        "Search playlists, moods, creators…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    )
                }
                inner()
            },
            modifier = Modifier.weight(1f),
        )
        if (query.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(18.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

// ── Horizontal mood chips row ─────────────────────────────────────
@Composable
private fun MoodChipsRow(
    categories: List<String>,
    active: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.forEach { cat ->
            val selected = cat == active
            val scale by animateFloatAsState(
                targetValue = if (selected) 1f else 0.97f,
                animationSpec = spring(
                    stiffness = Spring.StiffnessHigh,
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                ),
                label = "chip_scale_$cat"
            )

            FilterChip(
                selected = selected,
                onClick = { onSelect(cat) },
                label = {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                modifier = Modifier.scale(scale),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Lamp,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MoonDim,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = Night3,
                    selectedBorderColor = Color.Transparent,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 0.dp,
                )
            )
        }
    }
}

// ── Cassette Card (the hero UI element) ──────────────────────────
@Composable
fun CassetteCard(
    playlist: PlaylistEntry,
    index: Int,
    onClick: () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessHigh,
            dampingRatio = Spring.DampingRatioMediumBouncy,
        ),
        label = "card_press_scale",
    )

    // Staggered entrance for each card
    val enterAlpha = remember { Animatable(0f) }
    val enterOffsetY = remember { Animatable(28f) }
    LaunchedEffect(playlist.id) {
        kotlinx.coroutines.delay(index.coerceAtMost(20) * 40L)
        launch { enterAlpha.animateTo(1f, tween(420)) }
        launch { enterOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) }
    }

    Column(
        modifier = Modifier
            .alpha(enterAlpha.value)
            .offset(y = enterOffsetY.value.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF151821))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .padding(4.dp),
    ) {
        // ── Cassette tape window ──────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x160A0E16))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ReelDot(); TapeRibbon(Modifier.weight(1f)); ReelDot()
        }

        Spacer(Modifier.height(3.dp))

        // ── Cover image (CRT screen) ──────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF05070F))
        ) {
            AsyncImage(
                model = playlist.cover,
                contentDescription = playlist.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            // CRT scanline overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val lineH = 2.dp.toPx()
                        var y = 0f
                        while (y < size.height) {
                            drawLine(
                                color = Color(0x22000000),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1f,
                            )
                            y += lineH + 1f
                        }
                    }
            )

            // Live listener badge
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xD912212E))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                PulsingDot()
                Text(
                    text = "${playlist.listeners}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }

            // Dead overlay
            if (playlist.dead == true) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xCC000000)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "⚠ Offline",
                        style = MaterialTheme.typography.labelLarge,
                        color = OfflineRed,
                    )
                }
            }
        }

        // ── Bottom plate ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 3.dp, start = 2.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (index % 2 == 0) PowerGreen else Amber)
            )
            Text(
                text = playlist.brandLabel ?: playlist.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // ── Meta ──────────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(top = 6.dp, start = 2.dp, end = 2.dp, bottom = 2.dp),
        ) {
            Text(
                text = buildString {
                    append(playlist.title)
                    if (playlist.owner.isNotEmpty()) append("  ·  ${playlist.owner}")
                },
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                color = Night,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = playlist.description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MoonDim.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

// ── Atom components ───────────────────────────────────────────────

@Composable
private fun ReelDot() {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFE2E8F0), Color(0xFF475569), Color(0xFF0F172A))
                )
            )
            .border(0.5.dp, Color(0xFFCBD5E1), CircleShape)
    )
}

@Composable
private fun TapeRibbon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(4.dp)
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF382119), Color(0xFF543327), Color(0xFF382119))
                )
            )
    )
}

@Composable
private fun PulsingDot() {
    val alpha by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot_alpha",
    )
    Box(
        Modifier
            .size(5.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(PowerGreen)
    )
}
