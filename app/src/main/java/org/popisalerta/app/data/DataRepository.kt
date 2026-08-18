package org.popisalerta.app.data

import kotlinx.coroutines.flow.Flow
import org.popisalerta.app.data.local.AccessDao
import org.popisalerta.app.data.local.AccessEntity

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
            triggerSource = triggerSource
        )
    )

    override suspend fun logTestAccess(): Long = logAccess(TEST_TRIGGER_SOURCE)

    override suspend fun deleteAll() = accessDao.deleteAll()

    private companion object {
        const val TEST_TRIGGER_SOURCE = "TEST"
    }
}
