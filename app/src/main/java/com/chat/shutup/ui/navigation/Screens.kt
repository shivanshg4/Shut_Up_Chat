package com.chat.shutup.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object ChatList : Screen

    @Serializable
    data class Chat(val chatId: String) : Screen

    @Serializable
    data object Login : Screen

    @Serializable
    data object Signup : Screen

    @Serializable
    data object Profile : Screen

    @Serializable
    data object Search : Screen
}
