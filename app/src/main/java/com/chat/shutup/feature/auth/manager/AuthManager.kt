package com.chat.shutup.feature.auth.manager

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)

    fun getGoogleSignInRequest(): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("378539559507-6in68fb7nm570sm5ran9n07kj52a2d34.apps.googleusercontent.com")
            .setAutoSelectEnabled(true)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    suspend fun savePasswordCredentials(email: String, password: String) {
        try {
            val request = CreatePasswordRequest(email, password)
            credentialManager.createCredential(context, request)
        } catch (e: Exception) {
            // Log or handle error
            android.util.Log.e("AuthManager", "Failed to save credentials", e)
        }
    }

    suspend fun clearCredentials() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            android.util.Log.e("AuthManager", "Failed to clear credentials", e)
        }
    }
}
