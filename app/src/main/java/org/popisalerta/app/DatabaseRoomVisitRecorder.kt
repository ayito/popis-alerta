package org.popisalerta.app

import org.popisalerta.app.data.local.RoomVisitDao
import org.popisalerta.app.data.local.RoomVisitEntity

/** Implementación de [RoomVisitRecorder] respaldada por la base de datos Room. */
class DatabaseRoomVisitRecorder(private val dao: RoomVisitDao) : RoomVisitRecorder {

    override suspend fun recordVisit(startedAtMs: Long): Long =
        dao.insert(RoomVisitEntity(startedAt = startedAtMs, notified = false))

    override suspend fun getLastVisitStartedAtMs(): Long? = dao.getLastVisit()?.startedAt

    override suspend fun getVisitCount(): Int = dao.getVisitCount().toInt()
}
