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
import androidx.compose.ui.graphics.Color
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
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            NavigationBar(
                containerColor = Color.White,
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
                        selectedIconColor = Color(0xFF2E7D32),
                        selectedTextColor = Color(0xFF2E7D32),
                        indicatorColor = Color(0xFFE8F5E9)
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
                        selectedIconColor = Color(0xFF2E7D32),
                        selectedTextColor = Color(0xFF2E7D32),
                        indicatorColor = Color(0xFFE8F5E9)
                    )
                )
            }
        }
    ) { innerPadding ->
        // We handle innerPadding (bottom bar height) within the NavHost content
        // to allow the background gradient in Trips to extend to the very top.
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
                        /*onBackClick = { *//* Handled by bottom bar *//* },*/
                        onSignedOut = onSignedOut,
                        bottomPadding = innerPadding.calculateBottomPadding()
                    )
                }
            }
        }
    }
}
