package org.popisalerta.app

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.popisalerta.app.data.AccessRepository
import org.popisalerta.app.data.local.AccessEntity

class AppAccessLoggerTest {
    @Test
    fun logAppOpen_logsAppOpenTriggerSource() = runTest {
        val repository = RecordingAccessRepository()
        val logger = AppAccessLogger(repository)

        logger.logAppOpen()

        assertEquals(
            listOf(AppAccessLogger.APP_OPEN_TRIGGER_SOURCE),
            repository.loggedTriggerSources,
        )
    }
}

private class RecordingAccessRepository : AccessRepository {
    val loggedTriggerSources = mutableListOf<String>()

    override fun observeAll(): Flow<List<AccessEntity>> = emptyFlow()

    override fun observeSince(startMs: Long): Flow<List<AccessEntity>> = emptyFlow()

    override suspend fun logAccess(triggerSource: String): Long {
        loggedTriggerSources += triggerSource
        return loggedTriggerSources.size.toLong()
    }

    override suspend fun logTestAccess(): Long =
        logAccess("TEST")

    override suspend fun deleteAll() = Unit
}
