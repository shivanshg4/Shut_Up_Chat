package com.chat.shutup.domain.util

import com.chat.shutup.domain.model.LocationPoint
import com.chat.shutup.domain.model.RoutePoint
import org.junit.Assert.*
import org.junit.Test

class RouteProgressCalculatorTest {

    @Test
    fun `calculateProgress returns 0 for user at start of straight route`() {
        val routePoints = listOf(
            RoutePoint(0.0, 0.0),
            RoutePoint(1.0, 0.0) // 1 degree lat is ~111km
        )
        val location = LocationPoint(0.0, 0.0)
        
        val result = RouteProgressCalculator.calculateProgress(
            userId = "user1",
            location = location,
            routePoints = routePoints,
            totalDistanceMeters = 111000
        )
        
        assertEquals(0.0, result.progressDistanceMeters, 1.0)
        assertEquals(0.0f, result.progressPercentage, 0.01f)
    }

    @Test
    fun `calculateProgress returns total distance for user at end of route`() {
        val routePoints = listOf(
            RoutePoint(0.0, 0.0),
            RoutePoint(1.0, 0.0)
        )
        val location = LocationPoint(1.0, 0.0)
        
        val result = RouteProgressCalculator.calculateProgress(
            userId = "user1",
            location = location,
            routePoints = routePoints,
            totalDistanceMeters = 111319 // Approximate meters for 1 degree lat
        )
        
        // Use a more accurate distance check or ignore small variations
        assertTrue(result.progressDistanceMeters > 111000)
        assertEquals(1.0f, result.progressPercentage, 0.01f)
    }

    @Test
    fun `calculateProgress calculates ahead behind correctly`() {
        val routePoints = listOf(
            RoutePoint(0.0, 0.0),
            RoutePoint(1.0, 0.0)
        )
        
        // User A at 20km (approx 0.18 deg)
        val userAProgress = 20000.0
        
        // User B at 30km (approx 0.27 deg)
        val locationB = LocationPoint(0.2697, 0.0) 
        
        val result = RouteProgressCalculator.calculateProgress(
            userId = "userB",
            location = locationB,
            routePoints = routePoints,
            totalDistanceMeters = 111000,
            currentUserProgress = userAProgress
        )
        
        assertEquals(30000.0, result.progressDistanceMeters, 100.0)
        assertTrue(result.isAhead)
        assertEquals(10000.0, result.aheadBehindDistanceMeters, 100.0)
    }

    @Test
    fun `calculateProgress detects off route correctly`() {
        val routePoints = listOf(
            RoutePoint(0.0, 0.0),
            RoutePoint(1.0, 0.0)
        )
        
        // User far from route (approx 1km east)
        val locationOff = LocationPoint(0.5, 0.01) // 0.01 deg lon is ~1.1km at equator
        
        val result = RouteProgressCalculator.calculateProgress(
            userId = "user1",
            location = locationOff,
            routePoints = routePoints,
            totalDistanceMeters = 111000
        )
        
        assertTrue(result.isOffRoute)
        assertTrue(result.distanceFromRouteMeters > 500)
    }
}
