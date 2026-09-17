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
        val bitmap = getSpriteBitmapForBearing(row, bearing) ?: return null
        val cacheKey = "desc_row_${row}_bearing_${(bearing / 45).toInt()}"
        return spriteCache.getOrPut(cacheKey) {
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    fun getSpriteBitmapForBearing(row: Int, bearing: Float): Bitmap? {
        if (spriteSheet == null) return null

        var normalizedBearing = bearing % 360
        if (normalizedBearing < 0) normalizedBearing += 360

        val sector = (((normalizedBearing + 22.5f) % 360) / 45f).toInt()
        val sectorToColumn = intArrayOf(4, 5, 6, 7, 0, 1, 2, 3)
        val col = sectorToColumn[sector.coerceIn(0, 7)]

        return extractAndScaleBitmap(row, col)
    }

    private fun extractAndScaleBitmap(row: Int, col: Int): Bitmap {
        val sheet = spriteSheet!!
        val x = col * spriteWidth
        val y = row * spriteHeight

        val cropped = Bitmap.createBitmap(sheet, x, y, spriteWidth, spriteHeight)
        val density = context.resources.displayMetrics.density
        val targetSizePx = (48 * density).toInt()
        val scaled = Bitmap.createScaledBitmap(cropped, targetSizePx, targetSizePx, true)

        if (cropped != scaled) cropped.recycle()
        return scaled
    }
    
    fun clearCache() {
        spriteCache.clear()
    }
}
