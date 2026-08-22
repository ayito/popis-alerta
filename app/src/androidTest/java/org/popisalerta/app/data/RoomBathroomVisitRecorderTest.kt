package org.popisalerta.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.popisalerta.app.RoomBathroomVisitRecorder
import org.popisalerta.app.data.local.AccessDatabase
import org.popisalerta.app.data.local.BathroomVisitEntity

class RoomBathroomVisitRecorderTest {

    private lateinit var database: AccessDatabase
    private lateinit var recorder: RoomBathroomVisitRecorder

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AccessDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        recorder = RoomBathroomVisitRecorder(database.bathroomVisitDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun recordVisit_insertaVisitaCorrectamente() = runTest {
        val startedAtMs = 1_000L
        recorder.recordVisit(startedAtMs)

        val lastVisit = database.bathroomVisitDao().getLastVisit()

        assertNotNull(lastVisit)
        assertEquals(1_000L, lastVisit!!.startedAt)
    }

    @Test
    fun getLastVisitStartedAtMs_sinVisitas_devuelveNull() = runTest {
        val lastVisitStartedAtMs = recorder.getLastVisitStartedAtMs()
        assertNull(lastVisitStartedAtMs)
    }

    @Test
    fun getLastVisitStartedAtMs_conVisitas_devuelveInicioDeLaMasReciente() = runTest {
        // Insertar en orden no cronológico
        recorder.recordVisit(1_000L)
        recorder.recordVisit(3_000L)
        recorder.recordVisit(2_000L)

        // Debe devolver la de startedAt más reciente (3000L)
        val lastVisitStartedAtMs = recorder.getLastVisitStartedAtMs()

        assertEquals(3_000L, lastVisitStartedAtMs)
    }

    @Test
    fun getVisitCount_sinVisitas_devuelveCero() = runTest {
        val count = recorder.getVisitCount()
        assertEquals(0, count)
    }

    @Test
    fun getVisitCount_conVisitas_devuelveNumeroCorrecto() = runTest {
        recorder.recordVisit(1_000L)
        recorder.recordVisit(2_000L)
        recorder.recordVisit(3_000L)

        val count = recorder.getVisitCount()

        assertEquals(3, count)
    }
}
