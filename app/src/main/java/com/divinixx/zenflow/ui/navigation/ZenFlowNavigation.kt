package com.divinixx.zenflow.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.divinixx.zenflow.ui.screens.ConnectionScreen
import com.divinixx.zenflow.ui.screens.KeyboardScreen
import com.divinixx.zenflow.ui.screens.TouchpadScreen

/**
 * Navigation destinations for ZenFlow Remote app
 */
object ZenFlowDestinations {
    const val CONNECTION = "connection"
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
        containerColor = Color(0xFF1a1a1a),
        bottomBar = {
            ZenFlowBottomBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ZenFlowDestinations.CONNECTION,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ZenFlowDestinations.CONNECTION) {
                ConnectionScreen(navController = navController)
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
        containerColor = Color(0xFF2d2d2d),
        contentColor = Color.White
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Connection"
                )
            },
            label = { Text("Connection") },
            selected = currentDestination?.hierarchy?.any { it.route == ZenFlowDestinations.CONNECTION } == true,
            onClick = {
                navController.navigate(ZenFlowDestinations.CONNECTION) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Cyan,
                selectedTextColor = Color.Cyan,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF1a1a1a)
            )
        )
        
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Tablet,
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
                selectedIconColor = Color.Cyan,
                selectedTextColor = Color.Cyan,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF1a1a1a)
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
                selectedIconColor = Color.Cyan,
                selectedTextColor = Color.Cyan,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF1a1a1a)
            )
        )
    }
}
