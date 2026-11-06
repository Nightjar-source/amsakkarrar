package com.karrar.amsakiah.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.karrar.amsakiah.core.AndroidAppContext

actual class CompassProvider {

    private var manager: SensorManager? = null
    private var listener: SensorEventListener? = null

    private fun sensorManager(): SensorManager? =
        AndroidAppContext.optional()?.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    actual fun isAvailable(): Boolean =
        sensorManager()?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null

    actual fun start(onHeading: (Float) -> Unit) {
        val sm = sensorManager() ?: return
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) ?: return
        stop()
        manager = sm
        val rotation = FloatArray(9)
        val orientation = FloatArray(3)
        val l = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
                SensorManager.getRotationMatrixFromVector(rotation, event.values)
                SensorManager.getOrientation(rotation, orientation)
                var degrees = (orientation[0] * 180.0 / Math.PI).toFloat()
                if (degrees < 0) degrees += 360f
                // اتجاه الشمال المغناطيسي (0 = شمال)
                onHeading(degrees)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        listener = l
        sm.registerListener(l, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    actual fun stop() {
        listener?.let { manager?.unregisterListener(it) }
        listener = null
    }
}

actual fun createCompassProvider(): CompassProvider = CompassProvider()
