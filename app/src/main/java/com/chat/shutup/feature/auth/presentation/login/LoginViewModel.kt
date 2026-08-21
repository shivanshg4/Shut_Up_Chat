package com.chat.shutup.feature.auth.presentation.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.model.User
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.ChatRepository
import com.chat.shutup.feature.auth.manager.AuthManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onGoogleSignIn(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val credentialManager = CredentialManager.create(context)
            val request = authManager.getGoogleSignInRequest()

            try {
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        
        when {
            credential is GoogleIdTokenCredential -> {
                performGoogleSignIn(credential.idToken)
            }
            credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    performGoogleSignIn(googleIdTokenCredential.idToken)
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to parse Google credential") }
                }
            }
            else -> {
                _uiState.update { it.copy(isLoading = false, error = "Unknown credential type: ${credential.type}") }
            }
        }
    }

    private suspend fun performGoogleSignIn(idToken: String) {
        authRepository.signInWithGoogle(idToken).fold(
            onSuccess = { firebaseUser ->
                viewModelScope.launch {
                    firebaseUser?.let { user ->
                        chatRepository.updateCurrentUserProfile(
                            User(
                                id = user.uid,
                                name = user.displayName ?: "User",
                                email = user.email ?: "",
                                imageUrl = user.photoUrl?.toString(),
                                online = true
                            )
                        )
                    }
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
            },
            onFailure = { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        )
    }

    fun onEmailSignIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.signInWithEmail(email, password).fold(
                onSuccess = { firebaseUser ->
                    viewModelScope.launch {
                        firebaseUser?.let { user ->
                            authManager.savePasswordCredentials(email, password)
                            chatRepository.updateCurrentUserProfile(
                                User(
                                    id = user.uid,
                                    name = user.displayName ?: email.substringBefore("@"),
                                    email = email,
                                    online = true
                                )
                            )
                        }
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun onFacebookSignIn(accessToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.signInWithFacebook(accessToken).fold(
                onSuccess = { firebaseUser ->
                    viewModelScope.launch {
                        firebaseUser?.let { user ->
                            chatRepository.updateCurrentUserProfile(
                                User(
                                    id = user.uid,
                                    name = user.displayName ?: "User",
                                    email = user.email ?: "",
                                    imageUrl = user.photoUrl?.toString(),
                                    online = true
                                )
                            )
                        }
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun onGuestSignIn() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.signInAnonymously().fold(
                onSuccess = { firebaseUser ->
                    viewModelScope.launch {
                        firebaseUser?.let { user ->
                            chatRepository.updateCurrentUserProfile(
                                User(
                                    id = user.uid,
                                    name = "Guest",
                                    email = "",
                                    online = true
                                )
                            )
                        }
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
