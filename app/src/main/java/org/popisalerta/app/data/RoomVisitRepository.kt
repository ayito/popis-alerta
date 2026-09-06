package org.popisalerta.app.data

import kotlinx.coroutines.flow.Flow
import org.popisalerta.app.data.local.RoomVisitDao
import org.popisalerta.app.data.local.RoomVisitEntity

interface RoomVisitRepository {
    fun observeAllVisits(): Flow<List<RoomVisitEntity>>

    suspend fun getVisitCount(): Long

    suspend fun deleteAllVisits()
}

class DefaultRoomVisitRepository(private val roomVisitDao: RoomVisitDao) : RoomVisitRepository {

    override fun observeAllVisits(): Flow<List<RoomVisitEntity>> = roomVisitDao.getAllVisits()

    override suspend fun getVisitCount(): Long = roomVisitDao.getVisitCount()

    override suspend fun deleteAllVisits() {
        roomVisitDao.deleteAll()
    }
}
