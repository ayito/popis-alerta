package org.popisalerta.app.data

import kotlinx.coroutines.flow.Flow
import org.popisalerta.app.data.local.BathroomVisitDao
import org.popisalerta.app.data.local.BathroomVisitEntity

interface BathroomVisitRepository {
    fun observeAllVisits(): Flow<List<BathroomVisitEntity>>

    suspend fun getVisitCount(): Long

    suspend fun deleteAllVisits()
}

class DefaultBathroomVisitRepository(private val bathroomVisitDao: BathroomVisitDao) :
    BathroomVisitRepository {

    override fun observeAllVisits(): Flow<List<BathroomVisitEntity>> =
        bathroomVisitDao.getAllVisits()

    override suspend fun getVisitCount(): Long = bathroomVisitDao.getVisitCount()

    override suspend fun deleteAllVisits() {
        bathroomVisitDao.deleteAll()
    }
}
