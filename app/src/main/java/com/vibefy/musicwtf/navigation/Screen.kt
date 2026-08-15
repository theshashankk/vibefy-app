package com.vibefy.musicwtf.navigation

/** Every destination in the app. Typed routes prevent string typos. */
sealed class Screen(val route: String) {

    /** Animated splash — shown once per app cold-start (not per session like web) */
    object Splash : Screen("splash")

    /** Home: catalog browse, search, chips */
    object Catalog : Screen("catalog")

    /** Full-screen WebView for creator vibe-sites */
    object Player : Screen("player/{playlistId}") {
        fun build(playlistId: String) = "player/$playlistId"
    }

    /** Jukebox: rights-held native playlist browse */
    object Jukebox : Screen("jukebox")

    /** Jukebox individual playlist player */
    object JukeboxPlayer : Screen("jukebox/{jukeboxId}") {
        fun build(jukeboxId: String) = "jukebox/$jukeboxId"
    }

    /** Saved / offline list */
    object Saved : Screen("saved")

    /** About + settings + privacy policy */
    object About : Screen("about")

    /** Submit a new vibe-site */
    object Submit : Screen("submit")
}

/** Bottom-nav tab destinations */
val bottomNavItems = listOf(
    Screen.Catalog,
    Screen.Jukebox,
    Screen.Saved,
    Screen.About,
)
