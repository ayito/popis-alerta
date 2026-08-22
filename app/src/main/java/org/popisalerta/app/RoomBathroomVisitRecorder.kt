package org.popisalerta.app

import org.popisalerta.app.data.local.BathroomVisitDao
import org.popisalerta.app.data.local.BathroomVisitEntity

/**
 * Implementación de [BathroomVisitRecorder] que usa Room.
 */
class RoomBathroomVisitRecorder(
    private val dao: BathroomVisitDao,
) : BathroomVisitRecorder {

    override suspend fun recordVisit(startedAtMs: Long): Long {
        val visit = BathroomVisitEntity(
            startedAt = startedAtMs,
            notified = false,
        )
        dao.insert(visit)
        return startedAtMs
    }

    override suspend fun getLastVisitStartedAtMs(): Long? =
        dao.getLastVisit()?.startedAt

    override suspend fun getVisitCount(): Int =
        dao.getVisitCount().toInt()
}
