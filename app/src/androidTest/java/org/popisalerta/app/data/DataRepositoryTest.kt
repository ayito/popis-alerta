package org.popisalerta.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.popisalerta.app.data.local.AccessDatabase

class DataRepositoryTest {
    
    private lateinit var database: AccessDatabase
    private lateinit var repository: DefaultAccessRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AccessDatabase::class.java,
        ).build()

        repository = DefaultAccessRepository(database.accessDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun logTestAccess_insertsTestAccess() = runBlocking {
        val id = repository.logTestAccess()

        val accesses = repository.observeAll().first()

        assertTrue(id > 0)
        assertEquals(1, accesses.size)
        assertEquals(id, accesses.single().id)
        assertEquals("TEST", accesses.single().triggerSource)
    }
}
