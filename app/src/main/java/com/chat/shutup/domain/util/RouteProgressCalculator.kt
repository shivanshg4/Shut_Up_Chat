package com.chat.shutup.domain.util

import com.chat.shutup.domain.model.*
import kotlin.math.*

object RouteProgressCalculator {

    private const val EARTH_RADIUS_METERS = 6371000.0
    private const val OFF_ROUTE_THRESHOLD_METERS = 500.0
    private const val NEAR_THRESHOLD_METERS = 500.0

    /**
     * Calculates the progress of a member along a route.
     */
    fun calculateProgress(
        userId: String,
        location: LocationPoint,
        routePoints: List<RoutePoint>,
        totalDistanceMeters: Int,
        currentUserProgress: Double? = null
    ): MemberRouteProgress {
        if (routePoints.isEmpty()) {
            return MemberRouteProgress(
                userId = userId,
                progressDistanceMeters = 0.0,
                progressPercentage = 0f,
                distanceRemainingMeters = totalDistanceMeters.toDouble(),
                lastUpdated = location.timestamp
            )
        }

        var minDistanceToSegment = Double.MAX_VALUE
        var progressAtClosestPoint = 0.0
        var currentCumulativeDistance = 0.0

        for (i in 0 until routePoints.size - 1) {
            val p1 = routePoints[i]
            val p2 = routePoints[i + 1]
            val segmentLength = distance(p1.latitude, p1.longitude, p2.latitude, p2.longitude)

            val projection = projectPointOnSegment(
                location.latitude, location.longitude,
                p1.latitude, p1.longitude,
                p2.latitude, p2.longitude
            )

            val distanceToSegment = distance(location.latitude, location.longitude, projection.first, projection.second)

            if (distanceToSegment < minDistanceToSegment) {
                minDistanceToSegment = distanceToSegment
                val distanceToStartOfSegment = distance(p1.latitude, p1.longitude, projection.first, projection.second)
                progressAtClosestPoint = currentCumulativeDistance + distanceToStartOfSegment
            }

            currentCumulativeDistance += segmentLength
        }

        val isOffRoute = minDistanceToSegment > OFF_ROUTE_THRESHOLD_METERS
        val progressPercentage = if (totalDistanceMeters > 0) {
            (progressAtClosestPoint / totalDistanceMeters).coerceIn(0.0, 1.0).toFloat()
        } else 0f

        var aheadBehindDistance = 0.0
        var isAhead = false
        var isBehind = false
        var isNear = false

        if (currentUserProgress != null) {
            aheadBehindDistance = progressAtClosestPoint - currentUserProgress
            if (abs(aheadBehindDistance) < NEAR_THRESHOLD_METERS) {
                isNear = true
            } else if (aheadBehindDistance > 0) {
                isAhead = true
            } else {
                isBehind = true
            }
        }

        return MemberRouteProgress(
            userId = userId,
            progressDistanceMeters = progressAtClosestPoint,
            progressPercentage = progressPercentage,
            distanceRemainingMeters = (totalDistanceMeters - progressAtClosestPoint).coerceAtLeast(0.0),
            isAhead = isAhead,
            isBehind = isBehind,
            isNear = isNear,
            aheadBehindDistanceMeters = abs(aheadBehindDistance),
            isOffRoute = isOffRoute,
            distanceFromRouteMeters = minDistanceToSegment,
            lastUpdated = location.timestamp
        )
    }

    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    private fun projectPointOnSegment(
        plat: Double, plon: Double,
        alat: Double, alon: Double,
        blat: Double, blon: Double
    ): Pair<Double, Double> {
        val cosLat = cos(Math.toRadians(alat))
        val dLat = blat - alat
        val dLon = (blon - alon) * cosLat
        val pLat = plat - alat
        val pLon = (plon - alon) * cosLat

        var t = (pLat * dLat + pLon * dLon) / (dLat * dLat + dLon * dLon)
        if (t.isNaN()) t = 0.0

        return when {
            t < 0 -> Pair(alat, alon)
            t > 1 -> Pair(blat, blon)
            else -> Pair(alat + t * (blat - alat), alon + t * (blon - alon))
        }
    }
}
