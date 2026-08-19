package org.popisalerta.app

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.popisalerta.app.data.AccessRepository
import org.popisalerta.app.data.AccessRepositoryProvider
import org.popisalerta.app.data.local.AccessEntity

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    private lateinit var repository: RecordingAccessRepository

    @Before
    fun setUp() {
        repository = RecordingAccessRepository()
        AccessRepositoryProvider.factory = { _: Context -> repository }
    }

    @After
    fun tearDown() {
        AccessRepositoryProvider.reset()
    }

    @Test
    fun activityLogsAppOpenThenAppResumeAfterReturningToForeground() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)

            assertEquals(
                listOf(
                    AppAccessLogger.APP_OPEN_TRIGGER_SOURCE,
                    AppAccessLogger.APP_RESUME_TRIGGER_SOURCE,
                ),
                repository.loggedTriggerSources,
            )
        }
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
