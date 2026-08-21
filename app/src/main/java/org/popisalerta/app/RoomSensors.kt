package org.popisalerta.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

private const val TAG = "RoomSensors"

class RoomSensors(context: Context) : SensorEventListener {

    private var lastMotion = 0f

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val lightSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    init {
        if (lightSensor == null) {
            Log.d(TAG, "Light sensor not available")
        }
        if (accelerometer == null) {
            Log.d(TAG, "Accelerometer not available")
        }
    }

    fun start() {
        lightSensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LIGHT -> {
                val lux = event.values[0]
                Log.d(TAG, "Light: $lux lx")
            }

            Sensor.TYPE_ACCELEROMETER -> {
                val ax = event.values[0]
                val ay = event.values[1]
                val az = event.values[2]

                // magnitud total incluyendo gravedad
                val magnitude = kotlin.math.sqrt(ax * ax + ay * ay + az * az)

                // quitar gravedad (SensorManager.GRAVITY_EARTH ≈ 9.81 m/s^2)
                val motionRaw = kotlin.math.abs(magnitude - SensorManager.GRAVITY_EARTH)

                // filtro simple (low-pass): ajusta alpha según lo suave que quieras
                val alpha = 0.8f
                lastMotion = alpha * lastMotion + (1 - alpha) * motionRaw

                Log.d(TAG, "Accel: x=$ax, y=$ay, z=$az, motion=${"%.3f".format(lastMotion)} m/s^2")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // por ahora sin lógica específica
    }
}
