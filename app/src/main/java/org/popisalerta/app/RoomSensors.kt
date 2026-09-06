package org.popisalerta.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.popisalerta.app.data.local.AccessDao
import org.popisalerta.app.data.local.RoomEntryDao
import org.popisalerta.app.data.local.RoomEntryEntity

/**
 * Implementación de [Sensors] que usa sensores del dispositivo y registra picos en Room.
 *
 * Cuando [RoomVisitDetector] confirma un posible acceso, se guarda una única visita y se publica
 * una notificación. Los avisos pausados impiden procesar y guardar señales.
 */
class RoomSensors(
    private val context: Context,
    private val accessDao: AccessDao,
    private val roomEntryDao: RoomEntryDao,
    private val visitRecorder: RoomVisitRecorder,
    private val sensorThresholds: SensorThresholds,
    private val alertsEnabledProvider: () -> Boolean,
    private val clock: Clock = SystemClock(),
    private val entryMaxAgeMs: Long = 15_000L,
    private val notificationHelper: NotificationHelper
) : Sensors {

    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val lightSensor: Sensor? by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) }

    private val motionSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val cooldown = RoomVisitCooldown(cooldownMs = 60_000L)

    private val detector =
        RoomVisitDetector(
            clock = clock,
            cooldown = cooldown,
            visitRecorder = visitRecorder,
            entryMaxAgeMs = entryMaxAgeMs,
            isAlertsEnabled = alertsEnabledProvider
        )

    private val lightListener =
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (!alertsEnabledProvider()) {
                    return
                }

                val nowMs = clock.currentTimeMillis()
                val lux = event.values[0]

                if (lux >= sensorThresholds.currentLightThreshold()) {
                    Log.d(TAG, "Light threshold reached: lux=$lux")

                    coroutineScope.launch {
                        recordLightSpike(nowMs)

                        val visitId = detector.onLightSpike()
                        if (visitId != null) {
                            notificationHelper.showAlert(visitId)
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

    private val motionListener =
        object : SensorEventListener {
            private var lastMagnitude = 0f
            private val gravity = SensorManager.GRAVITY_EARTH // ~9.81 m/s²

            override fun onSensorChanged(event: SensorEvent) {
                if (!alertsEnabledProvider()) {
                    return
                }

                val nowMs = clock.currentTimeMillis()
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val magnitude = kotlin.math.sqrt(x * x + y * y + z * z)
                val accelNet = kotlin.math.abs(magnitude - gravity)

                // Log temporal para afinar umbral (lo quitaremos después)
                Log.d(
                    "Popis-Motion",
                    "accelNet=$accelNet threshold=${sensorThresholds.currentMotionThreshold()}"
                )

                lastMagnitude = magnitude

                if (accelNet > sensorThresholds.currentMotionThreshold()) {
                    Log.d(TAG, "Motion threshold reached: accelNet=$accelNet")

                    coroutineScope.launch {
                        recordMotionSpike(nowMs)

                        val visitId = detector.onMotionSpike()
                        if (visitId != null) {
                            notificationHelper.showAlert(visitId)
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

    override fun start() {
        Log.d(TAG, "Starting sensors; alertsEnabled=${alertsEnabledProvider()}")
        lightSensor?.let { sensor ->
            sensorManager.registerListener(lightListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        motionSensor?.let { sensor ->
            sensorManager.registerListener(
                motionListener,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun stop() {
        sensorManager.unregisterListener(lightListener)
        sensorManager.unregisterListener(motionListener)
    }

    private suspend fun recordLightSpike(nowMs: Long) {
        roomEntryDao.insert(
            RoomEntryEntity(timestamp = nowMs, motionSpike = false, lightSpike = true)
        )
    }

    private suspend fun recordMotionSpike(nowMs: Long) {
        roomEntryDao.insert(
            RoomEntryEntity(timestamp = nowMs, motionSpike = true, lightSpike = false)
        )
    }

    private companion object {
        const val TAG = "RoomSensors"
    }
}
