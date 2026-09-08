package com.chat.shutup.feature.trip.presentation.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

/**
 * Detects device shake gestures using the accelerometer.
 */
class ShakeDetector(
    private val context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    
    private var lastUpdate: Long = 0
    private var lastShakeTime: Long = 0
    
    // Configurable thresholds
    companion object {
        private const val SHAKE_THRESHOLD = 12f
        private const val SHAKE_COOLDOWN_MS = 2000L
        private const val MIN_TIME_BETWEEN_SAMPLES_MS = 100L
    }

    fun start() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
        accelerometer = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastUpdate) > MIN_TIME_BETWEEN_SAMPLES_MS) {
            val diffTime = currentTime - lastUpdate
            lastUpdate = currentTime

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate g-force excluding gravity (approx)
            // Normal gravity is ~9.8 m/s^2
            val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
            
            if (acceleration > SHAKE_THRESHOLD) {
                if (currentTime - lastShakeTime > SHAKE_COOLDOWN_MS) {
                    lastShakeTime = currentTime
                    Log.d("NatureInteractionDebug", "SHAKE_DETECTED: acceleration=$acceleration")
                    onShake()
                } else {
                    Log.d("NatureInteractionDebug", "SHAKE_COOLDOWN: ignoring trigger")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for shake detection
    }
}
