package org.popisalerta.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.popisalerta.app.data.AlertSettingsRepository
import org.popisalerta.app.data.local.AccessDatabase

private const val TAG = "RoomSensors"

class RoomSensors(
    context: Context,
    private val clock: Clock = SystemClock(),
    private val visitRecorder: BathroomVisitRecorder
) : SensorEventListener {

    private var lastMotion = 0f

    private var lightBaseline: Float? = null
    private var lastLux: Float? = null

    private var lastMotionSpikeAt: Long? = null
    private var lastLightSpikeAt: Long? = null

    private companion object {
        // ajustar estos valores tras ver datos reales
        const val MOTION_SPIKE_THRESHOLD = 1.0f // m/s^2
        const val LIGHT_DELTA_THRESHOLD = 80f // diferencia en lux
        const val LIGHT_BASELINE_ALPHA = 0.9f // suavizado de baseline

        // margen máximo entre señales para considerarlas relacionadas (10 s)
        const val ENTRY_MAX_AGE_MS = 10_000L

        // tiempo mínimo entre visitas al baño (5 minutos)
        const val VISIT_COOLDOWN_MS = 5 * 60 * 1000L
    }

    private val applicationContext = context.applicationContext

    // Sensores
    private val sensorManager =
        applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val lightSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Preferencia persistente de avisos activos/pausados
    private val alertSettingsRepository =
        AlertSettingsRepository(applicationContext)

    private val bathroomVisitCooldown = BathroomVisitCooldown(cooldownMs = VISIT_COOLDOWN_MS)

    // Scope para hacer inserciones en background (Room exige no bloquear el hilo principal)
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
                val previousBaseline = lightBaseline

                if (previousBaseline == null) {
                    lightBaseline = lux
                    Log.d(TAG, "Light baseline initialized: $lux lx")
                } else {
                    lightBaseline =
                        LIGHT_BASELINE_ALPHA * previousBaseline +
                        (1 - LIGHT_BASELINE_ALPHA) * lux
                    val delta = kotlin.math.abs(lux - lightBaseline!!)

                    if (delta > LIGHT_DELTA_THRESHOLD) {
                        lastLightSpikeAt = clock.currentTimeMillis()
                        recordVisitIfNeeded()
                    }
                }

                lastLux = lux
            }

            Sensor.TYPE_ACCELEROMETER -> {
                val ax = event.values[0]
                val ay = event.values[1]
                val az = event.values[2]

                val magnitude = kotlin.math.sqrt(ax * ax + ay * ay + az * az)
                val motionRaw = kotlin.math.abs(magnitude - SensorManager.GRAVITY_EARTH)

                val alpha = 0.8f
                lastMotion = alpha * lastMotion + (1 - alpha) * motionRaw

                if (lastMotion > MOTION_SPIKE_THRESHOLD) {
                    lastMotionSpikeAt = clock.currentTimeMillis()
                    recordVisitIfNeeded()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // por ahora sin lógica específica
    }

    /**
     * Agrupa picos de movimiento y luz en una sola "visita al baño"
     * usando un cooldown entre visitas.
     *
     * Si los avisos están pausados, no crea visitas nuevas.
     */
    private fun recordVisitIfNeeded() {
        if (!alertSettingsRepository.areAlertsEnabled()) {
            return
        }

        val now = clock.currentTimeMillis()

        val motionRecent =
            lastMotionSpikeAt != null && now - lastMotionSpikeAt!! <= ENTRY_MAX_AGE_MS
        val lightRecent =
            lastLightSpikeAt != null && now - lastLightSpikeAt!! <= ENTRY_MAX_AGE_MS

        if (!motionRecent && !lightRecent) {
            return
        }

        ioScope.launch {
            try {
                val lastVisitStartedAtMs = visitRecorder.getLastVisitStartedAtMs()
                val shouldCreateVisit = bathroomVisitCooldown.canCreateVisit(
                    nowMs = now,
                    lastVisitStartedAtMs = lastVisitStartedAtMs
                )

                if (shouldCreateVisit) {
                    visitRecorder.recordVisit(now)
                    val visitCount = visitRecorder.getVisitCount()
                    Log.d(
                        TAG,
                        "Bathroom visit created at $now (total visits: $visitCount)"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to record bathroom visit", e)
            }
        }
    }
}
