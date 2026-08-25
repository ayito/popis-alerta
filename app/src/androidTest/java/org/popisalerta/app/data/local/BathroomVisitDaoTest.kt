package org.popisalerta.app.data.local

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

class BathroomVisitDaoTest {

    private lateinit var database: AccessDatabase
    private lateinit var dao: BathroomVisitDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AccessDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.bathroomVisitDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertarVisita_y_recuperar_ultimaVisita() = runTest {
        val visit = BathroomVisitEntity(
            startedAt = 1_000L,
            notified = false
        )
        dao.insert(visit)

        val lastVisit = dao.getLastVisit()

        assertNotNull(lastVisit)
        assertEquals(1_000L, lastVisit!!.startedAt)
        assertEquals(false, lastVisit.notified)
    }

    @Test
    fun sinVisitas_getLastVisit_devuelveNull() = runTest {
        val lastVisit = dao.getLastVisit()
        assertNull(lastVisit)
    }

    @Test
    fun insertarVisitas_getVisitCount_devuelveNumeroCorrecto() = runTest {
        dao.insert(BathroomVisitEntity(startedAt = 1_000L, notified = false))
        dao.insert(BathroomVisitEntity(startedAt = 2_000L, notified = false))
        dao.insert(BathroomVisitEntity(startedAt = 3_000L, notified = false))

        val count = dao.getVisitCount()

        assertEquals(3, count)
    }

    @Test
    fun insertarVisitas_getLastVisit_devuelveLaMasRecientePorStartedAt() = runTest {
        // Insertar en orden no cronológico
        dao.insert(BathroomVisitEntity(startedAt = 1_000L, notified = false))
        dao.insert(BathroomVisitEntity(startedAt = 3_000L, notified = false))
        dao.insert(BathroomVisitEntity(startedAt = 2_000L, notified = false))

        // getLastVisit() debe devolver la de startedAt más reciente (3000L)
        val lastVisit = dao.getLastVisit()

        assertNotNull(lastVisit)
        assertEquals(3_000L, lastVisit!!.startedAt)
    }

    @Test
    fun eliminarTodasLasVisitas_dejaElHistorialVacio() = runTest {
        dao.insert(BathroomVisitEntity(startedAt = 1_000L, notified = false))
        dao.insert(BathroomVisitEntity(startedAt = 2_000L, notified = false))

        dao.deleteAll()

        assertEquals(0L, dao.getVisitCount())
        assertNull(dao.getLastVisit())
    }
}
