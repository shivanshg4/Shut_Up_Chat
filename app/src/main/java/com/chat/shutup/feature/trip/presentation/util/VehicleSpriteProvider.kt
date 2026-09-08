package com.chat.shutup.feature.trip.presentation.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.chat.shutup.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * Provides directional vehicle sprites from a sprite sheet.
 */
class VehicleSpriteProvider(private val context: Context) {

    private var spriteSheet: Bitmap? = null
    private val spriteCache = ConcurrentHashMap<String, BitmapDescriptor>()
    
    private val columnCount = 8
    private val rowCount = 8
    private var spriteWidth = 0
    private var spriteHeight = 0

    init {
        loadSpriteSheet()
    }

    private fun loadSpriteSheet() {
        try {
            val options = BitmapFactory.Options().apply {
                inScaled = false // Ensure we get raw pixels
            }
            spriteSheet = BitmapFactory.decodeResource(context.resources, R.drawable.yellow_vehicles, options)
            spriteSheet?.let {
                spriteWidth = it.width / columnCount
                spriteHeight = it.height / rowCount
                Log.d("TripVehicleDebug", "Sprite sheet loaded: ${it.width}x${it.height}, sprite size: ${spriteWidth}x${spriteHeight}")
            } ?: Log.e("TripVehicleDebug", "Failed to decode sprite sheet: R.drawable.yellow_vehicles is null")
        } catch (e: Exception) {
            Log.e("TripVehicleDebug", "Error loading sprite sheet", e)
        }
    }

    /**
     * Returns a [BitmapDescriptor] for the given vehicle type and bearing.
     * @param row The row in the sprite sheet (0..7) representing the vehicle design.
     * @param bearing The movement bearing (0..360).
     */
    fun getSpriteForBearing(row: Int, bearing: Float): BitmapDescriptor? {
        if (spriteSheet == null) {
            Log.e("TripVehicleDebug", "getSpriteForBearing failed: spriteSheet is null")
            return null
        }
        
        // Normalize bearing to 0..360
        var normalizedBearing = bearing % 360
        if (normalizedBearing < 0) normalizedBearing += 360
        
        // Map bearing to 8 directional sectors
        // 0: N, 1: NE, 2: E, 3: SE, 4: S, 5: SW, 6: W, 7: NW
        val sector = (((normalizedBearing + 22.5f) % 360) / 45f).toInt()
        
        // Direction mapping based on typical sprite sheet orientation
        // Sector: 0(N), 1(NE), 2(E), 3(SE), 4(S), 5(SW), 6(W), 7(NW)
        // Adjust these indices if the car faces the wrong way
        val sectorToColumn = intArrayOf(4, 5, 6, 7, 0, 1, 2, 3) 
        val col = sectorToColumn[sector.coerceIn(0, 7)]

        val cacheKey = "row_${row}_col_$col"
        return spriteCache.getOrPut(cacheKey) {
            extractAndScaleSprite(row, col)
        }
    }

    private fun extractAndScaleSprite(row: Int, col: Int): BitmapDescriptor {
        val sheet = spriteSheet!!
        val x = col * spriteWidth
        val y = row * spriteHeight
        
        Log.d("TripVehicleDebug", "Extracting sprite at row $row, col $col ($x, $y)")
        
        val cropped = Bitmap.createBitmap(sheet, x, y, spriteWidth, spriteHeight)
        
        // Scale to 48dp to ensure it's visible on all screens
        val density = context.resources.displayMetrics.density
        val targetSizePx = (48 * density).toInt()
        
        val scaled = Bitmap.createScaledBitmap(cropped, targetSizePx, targetSizePx, true)
        
        val descriptor = BitmapDescriptorFactory.fromBitmap(scaled)
        
        // Cleanup intermediate bitmaps
        if (cropped != scaled) cropped.recycle()

        return descriptor
    }
    
    fun clearCache() {
        spriteCache.clear()
    }
}
