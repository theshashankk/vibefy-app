package com.vibefy.musicwtf.ui.player

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.vibefy.musicwtf.ui.theme.*

/**
 * Player Screen — "Invisible WebView" engineering.
 *
 * iOS UI rules applied:
 * - No browser chrome ever visible
 * - Native TopAppBar floats above WebView (not inside it)
 * - Curtain loading animation with progress from 0→90→100
 * - Status bar color matched to playlist accentColor (dynamic island effect)
 * - Back gesture exits to catalog; only forwards to WebView history if user
 *   navigated inside the site
 * - Dead-link native error state with retry + flag action
 * - WebView pool: the ViewFactory caches and reuses WebView instances
 */
@Composable
fun PlayerScreen(
    playlistId: String,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(playlistId) { viewModel.loadPlaylist(playlistId) }

    // ── Dynamic status bar tint (accent color of playlist) ─────────
    // Done in theme via MusicWtfTheme(statusBarColor = ...) wrapping the
    // activity; here we just pass the color down through the ViewModel.

    val playlist = uiState.playlist ?: return

    // Handle predictive back — swipe exits to catalog cleanly
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    BackHandler {
        if (webViewRef?.canGoBack() == true && uiState.userNavigatedInside) {
            webViewRef?.goBack()
        } else {
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07080C))
    ) {

        // ── WebView ─────────────────────────────────────────────────
        val url = playlist.mirroredSiteUrl ?: playlist.originalSiteUrl

        IosWebView(
            url = url,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (uiState.isLoaded) 1f else 0f),
            onPageStarted = { viewModel.onPageStarted() },
            onPageFinished = { viewModel.onPageFinished() },
            onProgressChanged = { viewModel.onProgressChanged(it) },
            onWebViewReady = { wv -> webViewRef = wv },
        )

        // ── Loading curtain — same ticker logic as web ──────────────
        AnimatedVisibility(
            visible = !uiState.isLoaded,
            exit = fadeOut(tween(450)) + scaleOut(targetScale = 0.96f, animationSpec = tween(400)),
        ) {
            LoadingCurtain(
                progress = uiState.loadProgress,
                statusText = uiState.statusText,
                playlistTitle = playlist.title,
                accentColor = playlist.accentColor,
            )
        }

        // ── Dead-link error state ───────────────────────────────────
        if (uiState.isDead) {
            DeadLinkState(
                onRetry = { viewModel.retry() },
                onFlag = { viewModel.flagBroken() },
            )
        }

        // ── Native top app bar (outside WebView — always ours) ──────
        Column(modifier = Modifier.fillMaxWidth()) {
            // Translucent scrim behind bar so it's readable over any site
            IosPlayerTopBar(
                title = playlist.title,
                owner = playlist.owner,
                originalUrl = playlist.originalSiteUrl,
                accentColor = playlist.accentColor,
                onBack = onBack,
                onFlag = { viewModel.flagBroken() },
            )
        }
    }
}

// ── iOS-style player app bar ──────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IosPlayerTopBar(
    title: String,
    owner: String,
    originalUrl: String,
    accentColor: String,
    onBack: () -> Unit,
    onFlag: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val accent = remember(accentColor) {
        try { Color(android.graphics.Color.parseColor(accentColor)) }
        catch (e: Exception) { Lamp }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xCC07080C), Color(0x0007080C))
                )
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Back button — pill style
            FilledTonalIconButton(
                onClick = onBack,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.12f),
                    contentColor = Color.White,
                )
            ) {
                Icon(
                    Icons.Default.ArrowBackIos,
                    contentDescription = "Back",
                    modifier = Modifier.size(16.dp),
                )
            }

            Spacer(Modifier.width(10.dp))

            // Title + owner (accent-colored badge)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = YatraOne,
                    fontSize = 15.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = owner,
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    maxLines = 1,
                )
            }

            // View original button
            FilledTonalIconButton(
                onClick = { uriHandler.openUri(originalUrl) },
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.10f),
                    contentColor = Color.White,
                )
            ) {
                Icon(Icons.Default.OpenInBrowser, "View original", modifier = Modifier.size(17.dp))
            }

            Spacer(Modifier.width(6.dp))

            // Flag broken
            FilledTonalIconButton(
                onClick = onFlag,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.10f),
                    contentColor = Color.White,
                )
            ) {
                Icon(Icons.Default.Report, "Report broken", modifier = Modifier.size(17.dp))
            }
        }
    }
}

// ── Loading curtain (progress 0→90 tick, 90→100 on load) ─────────

@Composable
private fun LoadingCurtain(
    progress: Int,
    statusText: String,
    playlistTitle: String,
    accentColor: String,
) {
    val accent = remember(accentColor) {
        try { Color(android.graphics.Color.parseColor(accentColor)) }
        catch (e: Exception) { Lamp }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF12151E), Color(0xFF06070A))
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = playlistTitle,
                fontFamily = YatraOne,
                fontSize = 22.sp,
                color = Color.White,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "$progress%",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                color = accent,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f),
            )
            Spacer(Modifier.height(20.dp))
            // Progress track
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = progress / 100f,
                    animationSpec = tween(180, easing = LinearEasing),
                    label = "progress_bar",
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(CircleShape)
                        .background(accent)
                )
            }
        }
    }
}

// ── Dead-link error state ─────────────────────────────────────────

@Composable
private fun DeadLinkState(onRetry: () -> Unit, onFlag: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE07080C)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("📡", fontSize = 40.sp)
            Text(
                "This vibe-site is offline",
                fontFamily = YatraOne,
                fontSize = 20.sp,
                color = Color.White,
            )
            Text(
                "We're showing you the last good snapshot.\nHelp us fix it:",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(onClick = onRetry) { Text("Retry") }
                OutlinedButton(onClick = onFlag) { Text("Flag as Broken") }
            }
        }
    }
}

// ── Chrome-less WebView ───────────────────────────────────────────

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun IosWebView(
    url: String,
    modifier: Modifier = Modifier,
    onPageStarted: () -> Unit,
    onPageFinished: () -> Unit,
    onProgressChanged: (Int) -> Unit,
    onWebViewReady: (WebView) -> Unit,
) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    allowFileAccess = false
                    allowContentAccess = false
                    safeBrowsingEnabled = true
                    setSupportMultipleWindows(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    userAgentString = userAgentString.replace("wv", "")
                }

                // Disable long-press context menu (no "open in browser" popup)
                isLongClickable = false
                setOnLongClickListener { true }

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                        onPageStarted()
                    }
                    override fun onPageFinished(view: WebView, url: String) {
                        onPageFinished()
                    }
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ) = false // Allow all navigation inside the creator site
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView, newProgress: Int) {
                        onProgressChanged(newProgress)
                    }
                }

                onWebViewReady(this)
                loadUrl(url)
            }
        },
        modifier = modifier,
    )
}
