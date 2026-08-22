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
import org.popisalerta.app.data.local.AccessDatabase
import org.popisalerta.app.data.local.BathroomVisitEntity
import org.popisalerta.app.data.local.RoomEntryEntity

private const val TAG = "RoomSensors"

class RoomSensors(context: Context) : SensorEventListener {

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

    // Sensores
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val lightSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Base de datos y DAOs
    private val database = AccessDatabase.getInstance(context.applicationContext)
    private val roomEntryDao = database.roomEntryDao()
    private val bathroomVisitDao = database.bathroomVisitDao()

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
                SensorManager.SENSOR_DELAY_NORMAL,
            )
        }
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        // de momento no cancelamos ioScope para poder reiniciar sin recrear RoomSensors
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
                        lastLightSpikeAt = System.currentTimeMillis()
                        recordEntry(source = "light")
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
                    lastMotionSpikeAt = System.currentTimeMillis()
                    recordEntry(source = "motion")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // por ahora sin lógica específica
    }

    /**
     * Registra una entrada de habitación cada vez que hay un pico
     * de movimiento o de luz. Si la otra señal ha ocurrido en los
     * últimos ENTRY_MAX_AGE_MS, se marca también y subimos confianza.
     *
     * Además, agrupa esos picos en una sola "visita al baño" usando
     * un cooldown entre visitas.
     */
    private fun recordEntry(source: String) {
        val now = System.currentTimeMillis()

        val motionRecent =
            lastMotionSpikeAt != null && now - lastMotionSpikeAt!! <= ENTRY_MAX_AGE_MS
        val lightRecent =
            lastLightSpikeAt != null && now - lastLightSpikeAt!! <= ENTRY_MAX_AGE_MS

        // Señales activas para esta entrada
        val motionFlag = motionRecent || source == "motion"
        val lightFlag = lightRecent || source == "light"

        // Confianza simple: 1.0 si tenemos ambas señales, 0.7 si solo una
        val confidence = if (motionFlag && lightFlag) 1.0f else 0.7f

        val entryTimestamp = now

        val entry = RoomEntryEntity(
            timestamp = entryTimestamp,
            motionSpike = motionFlag,
            lightSpike = lightFlag,
            confidence = confidence,
        )

        ioScope.launch {
            try {
                // 1) Guardar el evento de sensores (baja señal)
                roomEntryDao.insert(entry)

                // 2) Decidir si creamos una nueva visita al baño (alto nivel)
                val lastVisit = bathroomVisitDao.getLastVisit()
                val shouldCreateVisit =
                    lastVisit == null ||
                        (entryTimestamp - lastVisit.startedAt) >= VISIT_COOLDOWN_MS

                if (shouldCreateVisit) {
                    val visit = BathroomVisitEntity(
                        startedAt = entryTimestamp,
                        notified = false,
                    )
                    bathroomVisitDao.insert(visit)
                    val visitCount = bathroomVisitDao.getVisitCount()
                    Log.d(
                        TAG,
                        "Bathroom visit created at $entryTimestamp (total visits: $visitCount)",
                    )
                }
                // si no se crea visita, no logeamos nada para no llenar el logcat
            } catch (e: Exception) {
                Log.e(TAG, "Failed to insert RoomEntryEntity or BathroomVisitEntity", e)
            }
        }
    }
}
