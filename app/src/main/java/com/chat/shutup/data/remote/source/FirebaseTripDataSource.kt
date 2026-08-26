package com.chat.shutup.data.remote.source

import com.chat.shutup.data.remote.dto.FirebaseTripDto
import com.chat.shutup.data.remote.dto.FirebaseTripMemberDto
import com.chat.shutup.data.remote.dto.FirebaseTripRouteDto
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
class FirebaseTripDataSource @Inject constructor(
    private val db: FirebaseDatabase
) {
    private val tripsRef = db.getReference("trips")
    private val inviteCodesRef = db.getReference("tripInviteCodes")

    suspend fun createTrip(trip: FirebaseTripDto) {
        // Step 1: Create trip with members (including creator)
        tripsRef.child(trip.id).setValue(trip).await()
        
        // Step 2: Create invite code lookup with creatorId for secure deletion
        val codeData = hashMapOf(
            "tripId" to trip.id,
            "creatorId" to trip.creatorId
        )
        inviteCodesRef.child(trip.inviteCode).setValue(codeData).await()
    }

    suspend fun getTripIdByInviteCode(inviteCode: String): String? {
        val snapshot = inviteCodesRef.child(inviteCode.uppercase()).child("tripId").get().await()
        return snapshot.getValue(String::class.java)
    }

    suspend fun getTrip(tripId: String): FirebaseTripDto? {
        val snapshot = tripsRef.child(tripId).get().await()
        return snapshot.getValue(FirebaseTripDto::class.java)
    }

    suspend fun addMember(tripId: String, member: FirebaseTripMemberDto) {
        tripsRef.child(tripId).child("members").child(member.userId).setValue(member).await()
    }

    suspend fun updateTripRoute(tripId: String, route: FirebaseTripRouteDto) {
        tripsRef.child(tripId).child("route").setValue(route).await()
    }

    fun observeTripMembers(tripId: String): Flow<List<FirebaseTripMemberDto>> = callbackFlow {
        val membersRef = tripsRef.child(tripId).child("members")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val members = snapshot.children.mapNotNull { it.getValue(FirebaseTripMemberDto::class.java) }
                trySend(members)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        membersRef.addValueEventListener(listener)
        awaitClose { membersRef.removeEventListener(listener) }
    }

    suspend fun deleteTrip(tripId: String, inviteCode: String) {
        val updates = hashMapOf<String, Any?>()
        updates["/trips/$tripId"] = null
        updates["/tripInviteCodes/$inviteCode"] = null
        db.reference.updateChildren(updates).await()
    }
}
