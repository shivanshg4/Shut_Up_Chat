package com.chat.shutup.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.chat.shutup.domain.repository.FcmRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmRepositoryImpl @Inject constructor(
    private val db: FirebaseDatabase,
    private val auth: FirebaseAuth
) : FcmRepository {

    override suspend fun updateToken(token: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            db.getReference("users").child(uid).child("fcmToken").setValue(token).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
