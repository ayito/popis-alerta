package org.popisalerta.app

import org.junit.Assert.assertEquals
import org.junit.Test

class RoomSensorsTest {

    private val cooldownMs = 5 * 60 * 1_000L
    private val entryMaxAgeMs = 10_000L

    @Test
    fun `un pico de movimiento genera una visita si los avisos están activos`() {
        val clock = FakeClock(1_000L)
        val visitRecorder = FakeBathroomVisitRecorder()
        val sensors = TestableRoomSensors(
            clock = clock,
            cooldownMs = cooldownMs,
            entryMaxAgeMs = entryMaxAgeMs,
            visitRecorder = visitRecorder,
        )

        sensors.simulateMotionSpike()

        assertEquals(1, visitRecorder.visits.size)
        assertEquals(1_000L, visitRecorder.visits[0].startedAtMs)
    }

    @Test
    fun `un pico de luz genera una visita si los avisos están activos`() {
        val clock = FakeClock(2_000L)
        val visitRecorder = FakeBathroomVisitRecorder()
        val sensors = TestableRoomSensors(
            clock = clock,
            cooldownMs = cooldownMs,
            entryMaxAgeMs = entryMaxAgeMs,
            visitRecorder = visitRecorder,
        )

        sensors.simulateLightSpike()

        assertEquals(1, visitRecorder.visits.size)
        assertEquals(2_000L, visitRecorder.visits[0].startedAtMs)
    }

    @Test
    fun `no se genera visita si los avisos están pausados`() {
        val clock = FakeClock(1_000L)
        val visitRecorder = FakeBathroomVisitRecorder()
        val sensors = TestableRoomSensors(
            clock = clock,
            cooldownMs = cooldownMs,
            entryMaxAgeMs = entryMaxAgeMs,
            visitRecorder = visitRecorder,
        )
        sensors.setAlertsEnabled(false)

        sensors.simulateMotionSpike()

        assertEquals(0, visitRecorder.visits.size)
    }

    @Test
    fun `se respeta el cooldown entre visitas`() {
        val clock = FakeClock(1_000L)
        val visitRecorder = FakeBathroomVisitRecorder()
        val sensors = TestableRoomSensors(
            clock = clock,
            cooldownMs = cooldownMs,
            entryMaxAgeMs = entryMaxAgeMs,
            visitRecorder = visitRecorder,
        )

        // Primera visita en t = 1_000
        sensors.simulateMotionSpike()

        // Avanzar menos que el cooldown
        clock.advanceBy(cooldownMs - 1L)
        sensors.simulateMotionSpike()

        // Solo debe haber una visita
        assertEquals(1, visitRecorder.visits.size)
        assertEquals(1_000L, visitRecorder.visits[0].startedAtMs)
    }

    @Test
    fun `permite nueva visita justo al vencer el cooldown`() {
        val clock = FakeClock(1_000L)
        val visitRecorder = FakeBathroomVisitRecorder()
        val sensors = TestableRoomSensors(
            clock = clock,
            cooldownMs = cooldownMs,
            entryMaxAgeMs = entryMaxAgeMs,
            visitRecorder = visitRecorder,
        )

        // Primera visita en t = 1_000
        sensors.simulateMotionSpike()

        // Avanzar exactamente el cooldown
        clock.advanceBy(cooldownMs)
        sensors.simulateMotionSpike()

        // Debe haber dos visitas
        assertEquals(2, visitRecorder.visits.size)
        assertEquals(1_000L, visitRecorder.visits[0].startedAtMs)
        assertEquals(1_000L + cooldownMs, visitRecorder.visits[1].startedAtMs)
    }

    /**
     * Versión testeable de RoomSensors que expone métodos para simular picos
     * y permite inyectar cooldown y entryMaxAge.
     */
    private class TestableRoomSensors(
        private val clock: Clock,
        private val cooldownMs: Long,
        private val entryMaxAgeMs: Long,
        private val visitRecorder: BathroomVisitRecorder,
    ) {

        private var lastMotionSpikeAt: Long? = null
        private var lastLightSpikeAt: Long? = null
        private var alertsEnabled = true

        private val cooldown = BathroomVisitCooldown(cooldownMs = cooldownMs)

        fun setAlertsEnabled(enabled: Boolean) {
            alertsEnabled = enabled
        }

        fun simulateMotionSpike() {
            lastMotionSpikeAt = clock.currentTimeMillis()
            recordVisitIfNeeded()
        }

        fun simulateLightSpike() {
            lastLightSpikeAt = clock.currentTimeMillis()
            recordVisitIfNeeded()
        }

        private fun recordVisitIfNeeded() {
            if (!alertsEnabled) {
                return
            }

            val now = clock.currentTimeMillis()

            val motionRecent =
                lastMotionSpikeAt != null && now - lastMotionSpikeAt!! <= entryMaxAgeMs
            val lightRecent =
                lastLightSpikeAt != null && now - lastLightSpikeAt!! <= entryMaxAgeMs

            if (!motionRecent && !lightRecent) {
                return
            }

            // En tests, ejecutamos de forma síncrona
            kotlinx.coroutines.runBlocking {
                val lastVisitStartedAtMs = visitRecorder.getLastVisitStartedAtMs()
                val shouldCreateVisit = cooldown.canCreateVisit(
                    nowMs = now,
                    lastVisitStartedAtMs = lastVisitStartedAtMs,
                )

                if (shouldCreateVisit) {
                    visitRecorder.recordVisit(now)
                }
            }
        }
    }
}
