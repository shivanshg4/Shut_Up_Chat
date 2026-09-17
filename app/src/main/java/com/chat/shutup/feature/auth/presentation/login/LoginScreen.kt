package com.chat.shutup.feature.auth.presentation.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.chat.shutup.R
import com.chat.shutup.feature.trip.presentation.screen.components.HomeEnvironment
import com.chat.shutup.ui.components.ShutUpPrimaryButton
import com.chat.shutup.ui.components.ShutUpSecondaryButton
import com.chat.shutup.ui.theme.ShutUpChatTheme
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToSignup: () -> Unit,
    onLoginSuccess: () -> Unit,
    onGuestLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val callbackManager = remember { CallbackManager.Factory.create() }
    
    val loginLauncher = rememberLauncherForActivityResult(
        contract = LoginManager.getInstance().createLogInActivityResultContract(callbackManager),
        onResult = {}
    )

    DisposableEffect(Unit) {
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    viewModel.onFacebookSignIn(result.accessToken.token)
                }

                override fun onCancel() {}

                override fun onError(error: FacebookException) {}
            }
        )
        onDispose {
            LoginManager.getInstance().unregisterCallback(callbackManager)
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    LoginContent(
        uiState = uiState,
        onNavigateToSignup = onNavigateToSignup,
        onGoogleSignIn = { viewModel.onGoogleSignIn(context) },
        onGuestSignIn = { viewModel.onGuestSignIn() },
        onClearError = { viewModel.clearError() }
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onNavigateToSignup: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onGuestSignIn: () -> Unit,
    onClearError: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // 1. Nature/travel visual area (Matching Home Screen)
        HomeEnvironment(
            modifier = Modifier.fillMaxWidth().height(360.dp),
            isAnimated = true,
            isInteractive = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(280.dp))

            // 2. ShutUp Trips Together
           /* Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "ShutUp Logo",
                modifier = Modifier.size(100.dp)
            )*/
            Spacer(modifier = Modifier.height(16.dp))
           /* Text(
                text = "ShutUp",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Trips Together",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.offset(y = (-4).dp)
            )*/

            Spacer(modifier = Modifier.height(48.dp))

            // 3. Welcome / greeting
            Text(
                text = "Discover the world with your favorite people.",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Share live locations, chat in real-time, and never lose your group again.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            // 4. [ Continue with Google ]
            ShutUpPrimaryButton(
                text = "Continue with Google",
                onClick = onGoogleSignIn,
                enabled = !uiState.isLoading,
                containerColor = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            ShutUpSecondaryButton(
                text = "Continue as Guest",
                onClick = onGuestSignIn,
                enabled = !uiState.isLoading,
                icon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. small supporting text
            Text(
                text = "By continuing, you agree to our Terms and Privacy Policy.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                action = {
                    TextButton(onClick = onClearError) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(text = error)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ShutUpChatTheme {
        LoginContent(
            uiState = LoginUiState(),
            onNavigateToSignup = {},
            onGoogleSignIn = {},
            onGuestSignIn = {},
            onClearError = {}
        )
    }
}
