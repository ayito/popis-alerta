package org.popisalerta.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.popisalerta.app.data.AlertSettingsRepository

class AlertSettingsRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: AlertSettingsRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        repository = AlertSettingsRepository(context)
    }

    @After
    fun tearDown() {
        // Limpiar preferencias después de cada test
        context.getSharedPreferences(
            "alert_settings",
            Context.MODE_PRIVATE
        ).edit().clear().apply()
    }

    @Test
    fun areAlertsEnabled_sinConfiguracion_devuelveTrue() = runTest {
        val enabled = repository.areAlertsEnabled()
        assertTrue(enabled)
    }

    @Test
    fun setAlertsEnabled_cambiaEstadoDeAlertas() = runTest {
        // Estado inicial: true
        assertTrue(repository.areAlertsEnabled())

        // Cambiar a false
        repository.setAlertsEnabled(false)
        assertFalse(repository.areAlertsEnabled())

        // Cambiar a true
        repository.setAlertsEnabled(true)
        assertTrue(repository.areAlertsEnabled())
    }

    @Test
    fun setAlertsEnabled_persisteEstado() = runTest {
        repository.setAlertsEnabled(false)

        // Crear nuevo repositorio con el mismo contexto
        val newRepository = AlertSettingsRepository(context)
        assertFalse(newRepository.areAlertsEnabled())
    }
}
