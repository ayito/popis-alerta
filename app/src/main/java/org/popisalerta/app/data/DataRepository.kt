package org.popisalerta.app.data

import kotlinx.coroutines.flow.Flow
import org.popisalerta.app.data.local.AccessDao
import org.popisalerta.app.data.local.AccessEntity
import org.popisalerta.app.data.local.BathroomVisitDao
import org.popisalerta.app.data.local.BathroomVisitEntity

interface AccessRepository {
    fun observeAll(): Flow<List<AccessEntity>>

    fun observeSince(startMs: Long): Flow<List<AccessEntity>>

    suspend fun logAccess(triggerSource: String): Long

    suspend fun logTestAccess(): Long

    suspend fun deleteAll()
}

class DefaultAccessRepository(private val accessDao: AccessDao) : AccessRepository {
    override fun observeAll(): Flow<List<AccessEntity>> = accessDao.observeAll()

    override fun observeSince(startMs: Long): Flow<List<AccessEntity>> =
        accessDao.observeSince(startMs)

    override suspend fun logAccess(triggerSource: String): Long = accessDao.insert(
        AccessEntity(
            timestamp = System.currentTimeMillis(),
            triggerSource = triggerSource,
        ),
    )

    override suspend fun logTestAccess(): Long = logAccess(TEST_TRIGGER_SOURCE)

    override suspend fun deleteAll() = accessDao.deleteAll()

    private companion object {
        const val TEST_TRIGGER_SOURCE = "TEST"
    }
}

// Repositorio para visitas al baño

interface BathroomVisitRepository {
    fun observeAllVisits(): Flow<List<BathroomVisitEntity>>

    suspend fun getVisitCount(): Long

    suspend fun deleteAllVisits()
}

class DefaultBathroomVisitRepository(
    private val bathroomVisitDao: BathroomVisitDao,
) : BathroomVisitRepository {

    override fun observeAllVisits(): Flow<List<BathroomVisitEntity>> =
        bathroomVisitDao.getAllVisits()

    override suspend fun getVisitCount(): Long =
        bathroomVisitDao.getVisitCount()

    override suspend fun deleteAllVisits() {
        // Por ahora no hay DAO para borrar, lo dejamos como no-op
        // Más adelante se puede añadir un @Query("DELETE FROM bathroom_visits")
    }
}
