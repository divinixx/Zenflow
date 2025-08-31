package com.divinixx.zenflow.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.divinixx.zenflow.ui.screens.HomeScreen
import com.divinixx.zenflow.ui.screens.KeyboardScreen
import com.divinixx.zenflow.ui.screens.TouchpadScreen

object ZenFlowDestinations {
    const val HOME = "home"
    const val TOUCHPAD = "touchpad"
    const val KEYBOARD = "keyboard"
}

/**
 * Main navigation component for ZenFlow Remote
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenFlowNavigation(
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A0A0A),
                    Color(0xFF1A1A1A),
                    Color(0xFF0D1117),
                    Color(0xFF161B22)
                )
            )
        ),
        containerColor = Color.Transparent,
        bottomBar = {
            ZenFlowBottomBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ZenFlowDestinations.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ZenFlowDestinations.HOME) {
                HomeScreen(navController = navController)
            }

            composable(ZenFlowDestinations.TOUCHPAD) {
                TouchpadScreen(navController = navController)
            }

            composable(ZenFlowDestinations.KEYBOARD) {
                KeyboardScreen(navController = navController)
            }
        }
    }
}

/**
 * Bottom navigation bar for switching between screens
 */
@Composable
private fun ZenFlowBottomBar(
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") },
            selected = currentDestination?.hierarchy?.any { it.route == ZenFlowDestinations.HOME } == true,
            onClick = {
                navController.navigate(ZenFlowDestinations.HOME) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = "Touchpad"
                )
            },
            label = { Text("Touchpad") },
            selected = currentDestination?.hierarchy?.any { it.route == ZenFlowDestinations.TOUCHPAD } == true,
            onClick = {
                navController.navigate(ZenFlowDestinations.TOUCHPAD) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = "Keyboard"
                )
            },
            label = { Text("Keyboard") },
            selected = currentDestination?.hierarchy?.any { it.route == ZenFlowDestinations.KEYBOARD } == true,
            onClick = {
                navController.navigate(ZenFlowDestinations.KEYBOARD) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
