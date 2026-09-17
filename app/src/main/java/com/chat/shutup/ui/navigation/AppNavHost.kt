package com.chat.shutup.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.chat.shutup.feature.auth.presentation.login.LoginScreen
import com.chat.shutup.feature.auth.presentation.signup.SignupScreen
import com.chat.shutup.feature.chat.presentation.screen.ChatScreen
import com.chat.shutup.feature.profile.presentation.ProfileScreen
import com.chat.shutup.feature.search.presentation.UserSearchScreen
import com.chat.shutup.ui.MainScreen
import com.chat.shutup.feature.settings.presentation.screen.SettingsScreen
import com.chat.shutup.feature.trip.presentation.screen.CreateTripScreen
import com.chat.shutup.feature.trip.presentation.screen.JoinTripScreen
import com.chat.shutup.feature.trip.presentation.screen.LocationPickerScreen
import com.chat.shutup.feature.trip.presentation.screen.TripDetailsScreen
import com.chat.shutup.feature.trip.presentation.screen.TripMapScreen
import com.chat.shutup.feature.trip.presentation.screen.TripsScreen
import com.chat.shutup.feature.trip.presentation.viewmodel.CreateTripViewModel
import com.chat.shutup.feature.trip.presentation.viewmodel.LocationPickerViewModel
import com.chat.shutup.ui.chat_list.ChatListScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Screen = Screen.Login,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<Screen.Login> {
            LoginScreen(
                onNavigateToSignup = { navController.navigate(Screen.Signup) },
                onLoginSuccess = {
                    navController.navigate(Screen.Trips) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                },
                onGuestLogin = {
                    navController.navigate(Screen.Trips) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.Signup> {
            SignupScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login) },
                onSignupSuccess = {
                    navController.navigate(Screen.Trips) {
                        popUpTo(Screen.Signup) { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.ChatList> {
            ChatListScreen(
                onChatClick = { chatId ->
                    navController.navigate(Screen.Chat(chatId))
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onSearchClick = {
                    navController.navigate(Screen.Search)
                }
            )
        }
        composable<Screen.Chat> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.Chat>()
            ChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<Screen.Profile> {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(Screen.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.Search> {
            UserSearchScreen(
                onBackClick = { navController.popBackStack() },
                onChatStarted = { chatId ->
                    navController.navigate(Screen.Chat(chatId)) {
                        popUpTo(Screen.Search) { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.Trips> {
            MainScreen(
                onNavigateToChatList = { navController.navigate(Screen.ChatList) },
                onNavigateToCreateTrip = { navController.navigate(Screen.CreateTrip) },
                onNavigateToJoinTrip = { navController.navigate(Screen.JoinTrip) },
                onNavigateToTripDetails = { tripId -> navController.navigate(Screen.TripDetails(tripId)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings) },
                onSignedOut = {
                    navController.navigate(Screen.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        // Screen.Settings is now handled within MainScreen
        composable<Screen.CreateTrip> { backStackEntry ->
            // Use the backStackEntry as the ViewModelStoreOwner to persist CreateTripViewModel
            // while navigating to the LocationPicker and back
            val viewModel: CreateTripViewModel = hiltViewModel(backStackEntry)
            CreateTripScreen(
                onBackClick = { navController.popBackStack() },
                onPickLocation = { lat, lng, mode ->
                    navController.navigate(Screen.LocationPicker(lat, lng, mode))
                },
                onTripCreated = { tripId ->
                    navController.navigate(Screen.TripDetails(tripId)) {
                        popUpTo(Screen.CreateTrip) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }
        composable<Screen.LocationPicker> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.LocationPicker>()
            // Find the CreateTrip back stack entry to get the same ViewModel
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.CreateTrip)
            }
            val createTripViewModel: CreateTripViewModel = hiltViewModel(parentEntry)

            LocationPickerScreen(
                initialLat = route.initialLat,
                initialLng = route.initialLng,
                mode = route.mode,
                onLocationConfirmed = { location ->
                    if (route.mode == "Origin") {
                        createTripViewModel.onOriginChange(location)
                    } else {
                        createTripViewModel.onDestinationChange(location)
                    }
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack()},
                viewModel = hiltViewModel()
            )
        }
        composable<Screen.JoinTrip> {
            JoinTripScreen(
                onBackClick = { navController.popBackStack() },
                onTripJoined = { tripId ->
                    navController.navigate(Screen.TripDetails(tripId)) {
                        popUpTo(Screen.JoinTrip) { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.TripDetails> {
            TripDetailsScreen(
                onBackClick = { navController.popBackStack() },
                onOpenMapClick = { tripId -> navController.navigate(Screen.TripMap(tripId)) },
                onTripDeleted = {
                    navController.navigate(Screen.Trips) {
                        popUpTo(Screen.Trips) { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.TripMap> {
            val route = it.toRoute<Screen.TripMap>()
            TripMapScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetails = { tripId -> navController.navigate(Screen.TripDetails(tripId)) },
                tripId = route.tripId
            )
        }
    }
}
