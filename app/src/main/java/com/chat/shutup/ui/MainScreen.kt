package com.chat.shutup.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chat.shutup.feature.settings.presentation.screen.SettingsScreen
import com.chat.shutup.feature.trip.presentation.screen.TripsScreen
import com.chat.shutup.ui.navigation.Screen

@Composable
fun MainScreen(
    onNavigateToChatList: () -> Unit,
    onNavigateToCreateTrip: () -> Unit,
    onNavigateToJoinTrip: () -> Unit,
    onNavigateToTripDetails: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onSignedOut: () -> Unit
) {
    val navController = rememberNavController()
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route?.contains("Trips") == true } == true,
                    onClick = {
                        navController.navigate(Screen.Trips) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Explore, contentDescription = null) },
                    label = { Text("Trips") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route?.contains("Settings") == true } == true,
                    onClick = {
                        navController.navigate(Screen.Settings) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Trips,
                modifier = Modifier.fillMaxSize()
            ) {
                composable<Screen.Trips> {
                    TripsScreen(
                        onCreateTripClick = onNavigateToCreateTrip,
                        onJoinTripClick = onNavigateToJoinTrip,
                        onTripClick = onNavigateToTripDetails,
                        onSettingsClick = { /* Handled by bottom bar */ },
                        onChatsClick = onNavigateToChatList,
                        bottomPadding = innerPadding.calculateBottomPadding()
                    )
                }
                composable<Screen.Settings> {
                    SettingsScreen(
                        onSignedOut = onSignedOut,
                        bottomPadding = innerPadding.calculateBottomPadding()
                    )
                }
            }
        }
    }
}
