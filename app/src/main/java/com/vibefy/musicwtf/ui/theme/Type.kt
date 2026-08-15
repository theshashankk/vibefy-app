package com.vibefy.musicwtf.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vibefy.musicwtf.R

// ── Font families matching the web app exactly ─────────────────────

/** Yatra One — used for ALL headings, splash, playlist titles. Never deviate. */
val YatraOne = FontFamily(
    Font(R.font.yatra_one_regular, FontWeight.Normal)
)

/** Mukta — body text, descriptions, buttons, search, subtitles */
val Mukta = FontFamily(
    Font(R.font.mukta_light,     FontWeight.Light),
    Font(R.font.mukta_regular,   FontWeight.Normal),
    Font(R.font.mukta_medium,    FontWeight.Medium),
    Font(R.font.mukta_semibold,  FontWeight.SemiBold),
    Font(R.font.mukta_bold,      FontWeight.Bold),
)

/** JetBrains Mono — handles, view counts, timers, mono labels */
val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium,  FontWeight.Medium),
)

// ── Typography scale ───────────────────────────────────────────────
val AppTypography = Typography(
    // Display: hero & splash text
    displayLarge = TextStyle(
        fontFamily = YatraOne,
        fontWeight = FontWeight.Normal,
        fontSize = 72.sp,
        lineHeight = 72.sp,
        letterSpacing = 1.sp,
    ),
    // Playlist card titles, drawer title
    displayMedium = TextStyle(
        fontFamily = YatraOne,
        fontWeight = FontWeight.Normal,
        fontSize = 42.sp,
        lineHeight = 46.sp,
        letterSpacing = 0.5.sp,
    ),
    // Screen titles (collapsing large title)
    displaySmall = TextStyle(
        fontFamily = YatraOne,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
    ),
    // Section headings
    headlineLarge = TextStyle(
        fontFamily = YatraOne,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = YatraOne,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Mukta,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    // Card titles
    titleLarge = TextStyle(
        fontFamily = Mukta,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Mukta,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Mukta,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
    ),
    // Body
    bodyLarge = TextStyle(
        fontFamily = Mukta,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Mukta,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Mukta,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    // Labels (mono UI badges)
    labelLarge = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Normal,
        fontSize = 9.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.5.sp,
    ),
)
