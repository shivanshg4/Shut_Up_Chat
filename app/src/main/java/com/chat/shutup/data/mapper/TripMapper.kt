package com.chat.shutup.data.mapper

import com.chat.shutup.data.local.entity.TripEntity
import com.chat.shutup.domain.model.Trip

fun TripEntity.toTrip(): Trip {
    return Trip(
        id = id,
        name = name,
        creatorId = creatorId,
        inviteCode = inviteCode,
        createdAt = createdAt
    )
}

fun Trip.toTripEntity(): TripEntity {
    return TripEntity(
        id = id,
        name = name,
        creatorId = creatorId,
        inviteCode = inviteCode,
        createdAt = createdAt
    )
}
