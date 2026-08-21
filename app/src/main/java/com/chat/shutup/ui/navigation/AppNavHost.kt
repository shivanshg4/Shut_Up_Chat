package com.chat.shutup.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.chat.shutup.feature.auth.presentation.login.LoginScreen
import com.chat.shutup.feature.auth.presentation.signup.SignupScreen
import com.chat.shutup.feature.chat.presentation.screen.ChatScreen
import com.chat.shutup.feature.profile.presentation.ProfileScreen
import com.chat.shutup.feature.search.presentation.UserSearchScreen
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
                    navController.navigate(Screen.ChatList) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                },
                onGuestLogin = {
                    navController.navigate(Screen.ChatList) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.Signup> {
            SignupScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login) },
                onSignupSuccess = {
                    navController.navigate(Screen.ChatList) {
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
                onSearchClick = {
                    navController.navigate(Screen.Search)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile)
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
    }
}
