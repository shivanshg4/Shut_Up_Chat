package com.chat.shutup.data.mapper

import com.chat.shutup.data.local.entity.TripEntity
import com.chat.shutup.data.local.entity.TripMemberEntity
import com.chat.shutup.data.remote.dto.FirebaseLocationDto
import com.chat.shutup.data.remote.dto.FirebaseTripDto
import com.chat.shutup.data.remote.dto.FirebaseTripLocationDto
import com.chat.shutup.data.remote.dto.FirebaseTripMemberDto
import com.chat.shutup.data.remote.dto.FirebaseTripRouteDto
import com.chat.shutup.domain.model.LocationPoint
import com.chat.shutup.domain.model.RoutePoint
import com.chat.shutup.domain.model.TravelMode
import com.chat.shutup.domain.model.Trip
import com.chat.shutup.domain.model.TripLocation
import com.chat.shutup.domain.model.TripMarkerType
import com.chat.shutup.domain.model.TripMember
import com.chat.shutup.domain.model.TripRole
import com.chat.shutup.domain.model.TripRoute

fun TripEntity.toTrip(members: List<TripMember> = emptyList()): Trip {
    return Trip(
        id = id,
        name = name,
        creatorId = creatorId,
        inviteCode = inviteCode,
        startTime = startTime,
        createdAt = createdAt,
        members = members,
        origin = if (originLat != null && originLng != null) {
            TripLocation(originLat, originLng, originAddress ?: "")
        } else null,
        destination = if (destLat != null && destLng != null) {
            TripLocation(destLat, destLng, destAddress ?: "")
        } else null,
        travelMode = try { TravelMode.valueOf(travelMode) } catch (e: Exception) { TravelMode.DRIVING },
        route = if (routePolyline != null) {
            TripRoute(
                distanceMeters = routeDistanceMeters ?: 0,
                durationSeconds = routeDurationSeconds ?: 0,
                encodedPolyline = routePolyline,
                points = PolylineDecoder.decode(routePolyline)
            )
        } else null
    )
}

fun Trip.toTripEntity(): TripEntity {
    return TripEntity(
        id = id,
        name = name,
        creatorId = creatorId,
        inviteCode = inviteCode,
        startTime = startTime,
        createdAt = createdAt,
        originLat = origin?.latitude,
        originLng = origin?.longitude,
        originAddress = origin?.address,
        destLat = destination?.latitude,
        destLng = destination?.longitude,
        destAddress = destination?.address,
        travelMode = travelMode.name,
        routeDistanceMeters = route?.distanceMeters,
        routeDurationSeconds = route?.durationSeconds,
        routePolyline = route?.encodedPolyline
    )
}

fun TripMemberEntity.toTripMember(): TripMember {
    return TripMember(
        userId = userId,
        name = name,
        role = role,
        joinedAt = joinedAt,
        markerType = try { TripMarkerType.valueOf(markerType) } catch (e: Exception) { TripMarkerType.DEFAULT }
    )
}

fun TripMember.toTripMemberEntity(tripId: String): TripMemberEntity {
    return TripMemberEntity(
        tripId = tripId,
        userId = userId,
        name = name,
        role = role,
        joinedAt = joinedAt,
        markerType = markerType.name
    )
}

fun FirebaseTripDto.toTrip(): Trip {
    return Trip(
        id = id,
        name = name,
        creatorId = creatorId,
        inviteCode = inviteCode,
        startTime = startTime,
        createdAt = createdAt,
        members = members.values.map { it.toTripMember() },
        origin = origin?.toTripLocation(),
        destination = destination?.toTripLocation(),
        travelMode = try { TravelMode.valueOf(travelMode) } catch (e: Exception) { TravelMode.DRIVING },
        route = route?.toTripRoute()
    )
}

fun Trip.toFirebaseDto(): FirebaseTripDto {
    return FirebaseTripDto(
        id = id,
        name = name,
        creatorId = creatorId,
        inviteCode = inviteCode,
        startTime = startTime,
        createdAt = createdAt,
        members = members.associate { it.userId to it.toFirebaseDto() },
        origin = origin?.toFirebaseDto(),
        destination = destination?.toFirebaseDto(),
        travelMode = travelMode.name,
        route = route?.toFirebaseDto()
    )
}

fun FirebaseTripLocationDto.toTripLocation(): TripLocation {
    return TripLocation(latitude, longitude, address)
}

fun TripLocation.toFirebaseDto(): FirebaseTripLocationDto {
    return FirebaseTripLocationDto(latitude, longitude, address)
}

fun FirebaseTripMemberDto.toTripMember(): TripMember {
    return TripMember(
        userId = userId,
        name = name,
        role = try { TripRole.valueOf(role) } catch (e: Exception) { TripRole.MEMBER },
        joinedAt = joinedAt,
        markerType = try { TripMarkerType.valueOf(markerType) } catch (e: Exception) { TripMarkerType.DEFAULT }
    )
}

fun TripMember.toFirebaseDto(): FirebaseTripMemberDto {
    return FirebaseTripMemberDto(
        userId = userId,
        name = name,
        role = role.name,
        joinedAt = joinedAt,
        markerType = markerType.name
    )
}

fun FirebaseTripRouteDto.toTripRoute(): TripRoute {
    return TripRoute(
        distanceMeters = distanceMeters,
        durationSeconds = durationSeconds,
        encodedPolyline = encodedPolyline,
        points = PolylineDecoder.decode(encodedPolyline)
    )
}

fun TripRoute.toFirebaseDto(): FirebaseTripRouteDto {
    return FirebaseTripRouteDto(
        distanceMeters = distanceMeters,
        durationSeconds = durationSeconds,
        encodedPolyline = encodedPolyline
    )
}

fun FirebaseLocationDto.toLocationPoint(): LocationPoint {
    return LocationPoint(
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        speed = speed,
        bearing = bearing,
        timestamp = timestamp
    )
}

fun LocationPoint.toFirebaseDto(): FirebaseLocationDto {
    return FirebaseLocationDto(
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        speed = speed,
        bearing = bearing,
        timestamp = timestamp
    )
}
