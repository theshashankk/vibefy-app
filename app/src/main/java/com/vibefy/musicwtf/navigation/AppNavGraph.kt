package com.vibefy.musicwtf.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vibefy.musicwtf.ui.catalog.CatalogScreen
import com.vibefy.musicwtf.ui.player.PlayerScreen
import com.vibefy.musicwtf.ui.jukebox.JukeboxScreen
import com.vibefy.musicwtf.ui.jukebox.JukeboxPlayerScreen
import com.vibefy.musicwtf.ui.saved.SavedScreen
import com.vibefy.musicwtf.ui.about.AboutScreen
import com.vibefy.musicwtf.ui.splash.SplashScreen

// ── Shared spring spec — matches iOS UISpringTimingParameters ───────
private val iosSpring = spring<Float>(
    stiffness = Spring.StiffnessMediumLow,
    dampingRatio = Spring.DampingRatioMediumBouncy,
)

// iOS-style horizontal push for forward navigation
private val enterPush = slideInHorizontally(
    animationSpec = tween(380),
    initialOffsetX = { it }
) + fadeIn(tween(200))

private val exitPush = slideOutHorizontally(
    animationSpec = tween(380),
    targetOffsetX = { -it / 3 }
) + fadeOut(tween(200))

private val enterPop = slideInHorizontally(
    animationSpec = tween(380),
    initialOffsetX = { -it / 3 }
) + fadeIn(tween(200))

private val exitPop = slideOutHorizontally(
    animationSpec = tween(380),
    targetOffsetX = { it }
) + fadeOut(tween(200))

// iOS bottom-sheet style for modals that float up
private val enterModal = scaleIn(
    initialScale = 0.92f,
    animationSpec = tween(340)
) + fadeIn(tween(220))

private val exitModal = scaleOut(
    targetScale = 0.95f,
    animationSpec = tween(280)
) + fadeOut(tween(200))

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {

        // ── Splash ──────────────────────────────────────────────────
        composable(
            route = Screen.Splash.route,
            exitTransition = { fadeOut(tween(350)) },
        ) {
            SplashScreen(
                onComplete = {
                    navController.navigate(Screen.Catalog.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Catalog (home) ──────────────────────────────────────────
        composable(
            route = Screen.Catalog.route,
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { exitPush },
            popEnterTransition = { enterPop },
            popExitTransition = { exitPop },
        ) {
            CatalogScreen(
                onPlaylistClick = { id ->
                    navController.navigate(Screen.Player.build(id))
                },
                onSubmitClick = {
                    navController.navigate(Screen.Submit.route)
                }
            )
        }

        // ── Player (vibe-site WebView) ──────────────────────────────
        composable(
            route = Screen.Player.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType }),
            enterTransition = { enterPush },
            exitTransition = { exitPush },
            popEnterTransition = { enterPop },
            popExitTransition = { exitPop },
        ) { backStack ->
            val id = backStack.arguments?.getString("playlistId") ?: return@composable
            PlayerScreen(
                playlistId = id,
                onBack = { navController.popBackStack() },
            )
        }

        // ── Jukebox browse ──────────────────────────────────────────
        composable(
            route = Screen.Jukebox.route,
            enterTransition = { fadeIn(tween(260)) },
            exitTransition = { exitPush },
            popEnterTransition = { enterPop },
            popExitTransition = { exitPop },
        ) {
            JukeboxScreen(
                onPlaylistClick = { id -> navController.navigate(Screen.JukeboxPlayer.build(id)) }
            )
        }

        // ── Jukebox player ──────────────────────────────────────────
        composable(
            route = Screen.JukeboxPlayer.route,
            arguments = listOf(navArgument("jukeboxId") { type = NavType.StringType }),
            enterTransition = { enterModal },
            exitTransition  = { exitModal },
            popEnterTransition  = { fadeIn(tween(260)) },
            popExitTransition   = { exitModal },
        ) { backStack ->
            val id = backStack.arguments?.getString("jukeboxId") ?: return@composable
            JukeboxPlayerScreen(
                jukeboxId = id,
                onBack = { navController.popBackStack() },
            )
        }

        // ── Saved ───────────────────────────────────────────────────
        composable(
            route = Screen.Saved.route,
            enterTransition = { fadeIn(tween(260)) },
        ) {
            SavedScreen(
                onPlaylistClick = { id -> navController.navigate(Screen.Player.build(id)) }
            )
        }

        // ── About / Settings ────────────────────────────────────────
        composable(
            route = Screen.About.route,
            enterTransition = { enterPush },
            exitTransition  = { exitPush },
            popEnterTransition  = { enterPop },
            popExitTransition   = { exitPop },
        ) {
            AboutScreen()
        }

        // ── Submit (bottom-sheet style push) ────────────────────────
        composable(
            route = Screen.Submit.route,
            enterTransition = { enterModal },
            exitTransition  = { exitModal },
        ) {
            // Submit renders inside a ModalBottomSheet via its own scaffold
            com.vibefy.musicwtf.ui.submit.SubmitScreen(
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}
