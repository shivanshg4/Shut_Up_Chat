package com.chat.shutup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.feature.call.presentation.ActiveCallScreen
import com.chat.shutup.feature.call.presentation.CallViewModel
import com.chat.shutup.feature.call.presentation.IncomingCallScreen
import com.chat.shutup.ui.navigation.AppNavHost
import com.chat.shutup.ui.navigation.Screen
import com.chat.shutup.ui.theme.ShutUpChatTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShutUpChatTheme {
                val callViewModel: CallViewModel = hiltViewModel()
                val callState by callViewModel.callState.collectAsState()
                
                val navController = rememberNavController()
                
                val startDestination = if (authRepository.currentUser != null) {
                    Screen.ChatList
                } else {
                    Screen.Login
                }

                AppNavHost(
                    navController = navController,
                    startDestination = startDestination
                )

                // Show call UI
                callState?.let { call ->
                    if (call.status == com.chat.shutup.domain.model.CallStatus.ACCEPTED) {
                        ActiveCallScreen(
                            callInfo = call,
                            onEndCall = { callViewModel.onEndCall() }
                        )
                    } else if (call.status == com.chat.shutup.domain.model.CallStatus.RINGING) {
                        IncomingCallScreen(
                            callInfo = call,
                            onAccept = { callViewModel.onAcceptCall() },
                            onReject = { callViewModel.onRejectCall() }
                        )
                    }
                }
            }
        }
    }
}
