package com.chat.shutup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.chat.shutup.feature.call.manager.AgoraCallManager
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.model.CallStatus
import com.chat.shutup.feature.call.presentation.ActiveCallScreen
import com.chat.shutup.feature.call.presentation.CallViewModel
import com.chat.shutup.feature.call.presentation.IncomingCallScreen
import com.chat.shutup.feature.call.presentation.OutgoingCallScreen
import com.chat.shutup.ui.navigation.AppNavHost
import com.chat.shutup.ui.navigation.Screen
import com.chat.shutup.ui.theme.ShutUpChatTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var agoraCallManager: AgoraCallManager

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShutUpChatTheme {
                val callViewModel: CallViewModel = hiltViewModel()
                val callState by callViewModel.callState.collectAsState()
                val remoteUid by callViewModel.remoteUid.collectAsState()
                
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val allGranted = permissions.values.all { it }
                    if (!allGranted) {
                        android.util.Log.e("MainActivity", "Permissions not granted")
                    }
                }

                val requiredPermissions = arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.POST_NOTIFICATIONS
                )

                fun checkAndRequestPermissions() {
                    val missingPermissions = requiredPermissions.filter {
                        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                    }
                    if (missingPermissions.isNotEmpty()) {
                        permissionLauncher.launch(missingPermissions.toTypedArray())
                    }
                }

                val navController = rememberNavController()
                
                // Handle notification navigation
                LaunchedEffect(intent) {
                    handleIntent(intent, navController)
                }

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
                    val isCaller = call.callerId == authRepository.currentUser?.uid

                    when (call.status) {
                        CallStatus.ACCEPTED -> {
                            checkAndRequestPermissions()
                            ActiveCallScreen(
                                callInfo = call,
                                remoteUid = remoteUid,
                                agoraCallManager = agoraCallManager,
                                onEndCall = { callViewModel.onEndCall() }
                            )
                        }
                        CallStatus.RINGING -> {
                            if (isCaller) {
                                OutgoingCallScreen(
                                    callInfo = call,
                                    onCancel = { callViewModel.onEndCall() }
                                )
                            } else {
                                IncomingCallScreen(
                                    callInfo = call,
                                    onAccept = { 
                                        checkAndRequestPermissions()
                                        callViewModel.onAcceptCall() 
                                    },
                                    onReject = { callViewModel.onRejectCall() }
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleIntent(intent: Intent, navController: NavController) {
        val tripId = intent.getStringExtra("tripId")
        if (tripId != null) {
            navController.navigate(Screen.TripMap(tripId)) {
                // Ensure we don't stack multiple map instances
                launchSingleTop = true
            }
        }
    }
}
