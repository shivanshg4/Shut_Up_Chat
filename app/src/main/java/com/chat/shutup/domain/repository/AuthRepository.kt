package com.chat.shutup.domain.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?
    
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser?>
    suspend fun signInWithFacebook(accessToken: String): Result<FirebaseUser?>
    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser?>
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser?>
    suspend fun signInAnonymously(): Result<FirebaseUser?>
    suspend fun signOut()
}
