package com.chat.shutup.data.remote.source

import com.chat.shutup.data.remote.dto.FirebaseLocationDto
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseLocationDataSource @Inject constructor(
    private val db: FirebaseDatabase
) {
    private val tripsRef = db.getReference("trips")

    suspend fun updateMyLocation(tripId: String, userId: String, location: FirebaseLocationDto) {
        tripsRef.child(tripId)
            .child("locations")
            .child(userId)
            .setValue(location)
            .await()
    }

    fun observeTripLocations(tripId: String): Flow<Map<String, FirebaseLocationDto>> = callbackFlow {
        val locationsRef = tripsRef.child(tripId).child("locations")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val locations = snapshot.children.associate { 
                    it.key!! to it.getValue(FirebaseLocationDto::class.java)!!
                }
                trySend(locations)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        locationsRef.addValueEventListener(listener)
        awaitClose { locationsRef.removeEventListener(listener) }
    }

    suspend fun removeMyLocation(tripId: String, userId: String) {
        tripsRef.child(tripId)
            .child("locations")
            .child(userId)
            .removeValue()
            .await()
    }
}
