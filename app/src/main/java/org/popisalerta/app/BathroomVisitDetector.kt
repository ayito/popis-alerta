package org.popisalerta.app

/**
 * Decide si un pico de sensor corresponde a una nueva visita al baño.
 *
 * Una señal de luz O de movimiento puede iniciar una visita. El cooldown
 * evita registrar múltiples visitas por el mismo episodio.
 */
class BathroomVisitDetector(
    private val clock: Clock,
    private val cooldown: BathroomVisitCooldown,
    private val visitRecorder: BathroomVisitRecorder,
    private val entryMaxAgeMs: Long,
) {
    private var lastMotionSpikeAtMs: Long? = null
    private var lastLightSpikeAtMs: Long? = null

    suspend fun onMotionSpike(): Long? {
        lastMotionSpikeAtMs = clock.currentTimeMillis()
        return recordVisitIfNeeded()
    }

    suspend fun onLightSpike(): Long? {
        lastLightSpikeAtMs = clock.currentTimeMillis()
        return recordVisitIfNeeded()
    }

    private suspend fun recordVisitIfNeeded(): Long? {
        val nowMs = clock.currentTimeMillis()

        val motionRecent =
            lastMotionSpikeAtMs != null &&
                nowMs - lastMotionSpikeAtMs!! <= entryMaxAgeMs

        val lightRecent =
            lastLightSpikeAtMs != null &&
                nowMs - lastLightSpikeAtMs!! <= entryMaxAgeMs

        if (!motionRecent && !lightRecent) {
            return null
        }

        val lastVisitStartedAtMs = visitRecorder.getLastVisitStartedAtMs()
        if (!cooldown.canCreateVisit(nowMs, lastVisitStartedAtMs)) {
            return null
        }

        val visitId = visitRecorder.recordVisit(nowMs)
        lastMotionSpikeAtMs = null
        lastLightSpikeAtMs = null
        return visitId
    }
}
