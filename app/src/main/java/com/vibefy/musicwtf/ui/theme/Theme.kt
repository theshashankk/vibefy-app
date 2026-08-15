package com.vibefy.musicwtf.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

// ── Dark color scheme (Player, Drawer, Jukebox) ────────────────────
private val DarkColors = darkColorScheme(
    primary          = Lamp,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    secondary        = Amber,
    onSecondary      = Moon,
    background       = PageBg,
    onBackground     = Color.White,
    surface          = DeckDark,
    onSurface        = Color.White,
    surfaceVariant   = Color(0xFF1A1D24),
    outline          = GlassWhite12,
    error            = OfflineRed,
)

// ── Light color scheme (Catalog / Browse home) ─────────────────────
private val LightColors = lightColorScheme(
    primary          = Lamp,
    onPrimary        = Color.White,
    primaryContainer = Night3,
    secondary        = Amber,
    onSecondary      = Moon,
    background       = Night,
    onBackground     = Moon,
    surface          = Night2,
    onSurface        = Moon,
    surfaceVariant   = Color(0xFFF1F7FC),
    outline          = Night3,
    error            = OfflineRed,
)

@Composable
fun MusicWtfTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    /** Accent driven by playlist accentColor for player status bar theming */
    statusBarColor: Color? = null,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    // ── Edge-to-edge + status/nav bar tint ─────────────────────────
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowInsetsControllerCompat(window, view)
            val barColor = (statusBarColor ?: Color.Transparent).toArgb()
            window.statusBarColor = barColor
            window.navigationBarColor = Color.Transparent.toArgb()
            insetsController.isAppearanceLightStatusBars = !darkTheme && statusBarColor == null
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content,
    )
}
