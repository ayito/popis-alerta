package org.popisalerta.app

import android.util.Log

/**
 * Agrupa picos de movimiento y luz en una única visita a una sala.
 *
 * Un pico válido de movimiento o de luz basta para registrar una visita. Las señales posteriores
 * del mismo episodio no crean visitas adicionales, gracias al periodo de cooldown.
 */
class RoomVisitDetector(
    private val clock: Clock,
    private val cooldown: RoomVisitCooldown,
    private val visitRecorder: RoomVisitRecorder,
    private val entryMaxAgeMs: Long,
    private val isAlertsEnabled: () -> Boolean
) {
    private var lastMotionSpikeAtMs: Long? = null
    private var lastLightSpikeAtMs: Long? = null

    suspend fun onMotionSpike(): Long? {
        if (!isAlertsEnabled()) {
            Log.d(TAG, "Motion ignored: alerts paused")
            return null
        }

        val nowMs = clock.currentTimeMillis()
        lastMotionSpikeAtMs = nowMs
        return recordVisitIfNeeded(nowMs)
    }

    suspend fun onLightSpike(): Long? {
        if (!isAlertsEnabled()) {
            Log.d(TAG, "Light ignored: alerts paused")
            return null
        }

        val nowMs = clock.currentTimeMillis()
        lastLightSpikeAtMs = nowMs
        return recordVisitIfNeeded(nowMs)
    }

    private suspend fun recordVisitIfNeeded(nowMs: Long): Long? {
        if (!isAlertsEnabled()) {
            Log.d(TAG, "Visit ignored: alerts paused")
            return null
        }

        val lastVisitStartedAtMs = visitRecorder.getLastVisitStartedAtMs()

        if (!cooldown.canCreateVisit(nowMs, lastVisitStartedAtMs)) {
            return null
        }

        val motionRecent =
            lastMotionSpikeAtMs != null && nowMs - lastMotionSpikeAtMs!! <= entryMaxAgeMs

        val lightRecent =
            lastLightSpikeAtMs != null && nowMs - lastLightSpikeAtMs!! <= entryMaxAgeMs

        if (!motionRecent && !lightRecent) {
            return null
        }

        val visitId = visitRecorder.recordVisit(nowMs)

        Log.d(TAG, "Visit recorded: id=$visitId")

        lastMotionSpikeAtMs = null
        lastLightSpikeAtMs = null

        return visitId
    }

    private companion object {
        const val TAG = "RoomVisitDetector"
    }
}
