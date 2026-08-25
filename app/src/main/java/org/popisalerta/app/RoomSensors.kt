package org.popisalerta.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.popisalerta.app.data.local.AccessDao
import org.popisalerta.app.data.local.RoomEntryDao
import org.popisalerta.app.data.local.RoomEntryEntity

/**
 * Implementación de [Sensors] que usa Room y delega la detección de visitas
 * al baño en [BathroomVisitDetector].
 *
 * Esta clase registra picos de luz y movimiento en Room y, cuando el detector
 * decide que hay una nueva visita, la persiste en la tabla bathroom_visits.
 */
class RoomSensors(
    private val context: Context,
    private val accessDao: AccessDao,
    private val roomEntryDao: RoomEntryDao,
    private val visitRecorder: BathroomVisitRecorder,
    private val clock: Clock = SystemClock(),
    private val entryMaxAgeMs: Long = 15_000L,
) : Sensors {

    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val lightSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    private val motionSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val cooldown = BathroomVisitCooldown(cooldownMs = 60_000L)

    private val detector = BathroomVisitDetector(
        clock = clock,
        cooldown = cooldown,
        visitRecorder = visitRecorder,
        entryMaxAgeMs = entryMaxAgeMs,
    )

    private val lightListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val nowMs = clock.currentTimeMillis()
            val lux = event.values[0]
            if (lux >= 100f) {
                coroutineScope.launch {
                    recordLightSpike(nowMs)
                    detector.onLightSpike()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val motionListener = object : SensorEventListener {
        private var lastX: Float = 0f
        private var lastY: Float = 0f
        private var lastZ: Float = 0f

        override fun onSensorChanged(event: SensorEvent) {
            val nowMs = clock.currentTimeMillis()
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val delta =
                kotlin.math.abs(x - lastX) +
                    kotlin.math.abs(y - lastY) +
                    kotlin.math.abs(z - lastZ)

            lastX = x
            lastY = y
            lastZ = z

            if (delta > 2f) {
                coroutineScope.launch {
                    recordMotionSpike(nowMs)
                    detector.onMotionSpike()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun start() {
        lightSensor?.let {
            sensorManager.registerListener(
                lightListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
            )
        }

        motionSensor?.let {
            sensorManager.registerListener(
                motionListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
            )
        }
    }

    override fun stop() {
        sensorManager.unregisterListener(lightListener)
        sensorManager.unregisterListener(motionListener)
    }

    private suspend fun recordLightSpike(nowMs: Long) {
        roomEntryDao.insert(
            RoomEntryEntity(
                timestamp = nowMs,
                motionSpike = false,
                lightSpike = true,
            ),
        )
    }

    private suspend fun recordMotionSpike(nowMs: Long) {
        roomEntryDao.insert(
            RoomEntryEntity(
                timestamp = nowMs,
                motionSpike = true,
                lightSpike = false,
            ),
        )
    }
}
