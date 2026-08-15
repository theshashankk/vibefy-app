package com.vibefy.musicwtf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vibefy.musicwtf.navigation.AppNavGraph
import com.vibefy.musicwtf.navigation.Screen
import com.vibefy.musicwtf.ui.theme.DeckDeep
import com.vibefy.musicwtf.ui.theme.GlassWhite12
import com.vibefy.musicwtf.ui.theme.Lamp
import com.vibefy.musicwtf.ui.theme.MusicWtfTheme
import dagger.hilt.android.AndroidEntryPoint

data class NavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val iconSelected: ImageVector,
)

private val navItems = listOf(
    NavItem(Screen.Catalog,  "Gaane",   Icons.Outlined.Radio,         Icons.Filled.Radio),
    NavItem(Screen.Jukebox,  "Jukebox", Icons.Outlined.MusicNote,     Icons.Filled.MusicNote),
    NavItem(Screen.Saved,    "Saved",   Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark),
    NavItem(Screen.About,    "About",   Icons.Outlined.Info,           Icons.Filled.Info),
)

/** Screens that should hide the bottom nav bar (full-screen experiences) */
private val fullScreenRoutes = setOf(
    Screen.Splash.route,
    Screen.Player.route,
    Screen.JukeboxPlayer.route,
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge (draws behind status bar + nav bar)
        enableEdgeToEdge()

        setContent {
            MusicWtfTheme {
                val navController = rememberNavController()
                val backStack by navController.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.route

                // Hide bottom bar on splash/player/jukeboxPlayer
                val showBottomBar = currentRoute !in fullScreenRoutes

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showBottomBar,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = spring(
                                    stiffness = Spring.StiffnessMediumLow,
                                    dampingRatio = Spring.DampingRatioMediumBouncy
                                )
                            ) + fadeIn(),
                            exit = slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = spring(stiffness = Spring.StiffnessMedium)
                            ) + fadeOut(),
                        ) {
                            IosStyleBottomNav(
                                items = navItems,
                                currentRoute = currentRoute,
                                onItemClick = { screen ->
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    AppNavGraph(
                        navController = navController,
                        startDestination = Screen.Splash.route,
                    )
                }
            }
        }
    }
}

/**
 * iOS-style floating pill bottom navigation bar.
 *
 * Design rules:
 * - Frosted glass background (blur + semi-transparent)
 * - Floating above content, rounded 28dp pill shape
 * - Selected item: accent dot below icon + label scales up
 * - Spring bounce on tap (scale 0.88 → 1.0)
 * - HapticFeedbackType.TextHandleMove on tap
 * - Never shows a colored background behind selected item (no Android ripple)
 */
@Composable
private fun IosStyleBottomNav(
    items: List<NavItem>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .navigationBarsPadding(),
    ) {
        // Frosted glass pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            DeckDeep.copy(alpha = 0.88f),
                            DeckDeep.copy(alpha = 0.96f),
                        )
                    )
                )
                // Subtle glass border
                .then(
                    Modifier.padding(1.dp).background(
                        color = GlassWhite12,
                        shape = RoundedCornerShape(27.dp)
                    )
                )
                .padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.screen.route

                    IosNavItem(
                        item = item,
                        selected = selected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onItemClick(item.screen)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun IosNavItem(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }

    val scale by animateDpAsState(
        targetValue = if (pressed) (-2).dp else 0.dp, // scale trick via offset
        animationSpec = spring(
            stiffness = Spring.StiffnessHigh,
            dampingRatio = Spring.DampingRatioMediumBouncy,
        ),
        label = "nav_item_scale"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // iOS has no ripple
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (selected) item.iconSelected else item.icon,
            contentDescription = item.label,
            tint = if (selected) Lamp else Color.White.copy(alpha = 0.45f),
            modifier = Modifier.size(22.dp),
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Lamp else Color.White.copy(alpha = 0.4f),
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Selection dot — iOS-style accent pip
        Box(
            modifier = Modifier
                .size(width = if (selected) 18.dp else 4.dp, height = 3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    if (selected) Lamp else Color.Transparent
                )
        )
    }
}
