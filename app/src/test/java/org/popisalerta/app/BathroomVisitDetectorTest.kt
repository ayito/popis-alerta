package org.popisalerta.app

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BathroomVisitDetectorTest {

    @Test
    fun noVisitsWithoutAnySpike() = runTest {
        val clock = FakeClock()
        val cooldown = BathroomVisitCooldown(cooldownMs = 60_000L)
        val recorder = FakeBathroomVisitRecorder()
        val detector = BathroomVisitDetector(
            clock = clock,
            cooldown = cooldown,
            visitRecorder = recorder,
            entryMaxAgeMs = 15_000L,
        )

        // No se llama a ningún spike: no debería haber visita
        assertEquals(0, recorder.getVisitCount())
    }

    @Test
    fun oneVisitFromLightSpike() = runTest {
        val clock = FakeClock()
        val cooldown = BathroomVisitCooldown(cooldownMs = 60_000L)
        val recorder = FakeBathroomVisitRecorder()
        val detector = BathroomVisitDetector(
            clock = clock,
            cooldown = cooldown,
            visitRecorder = recorder,
            entryMaxAgeMs = 15_000L,
        )

        val visitId = detector.onLightSpike()

        assertEquals(1L, visitId)
        assertEquals(1, recorder.getVisitCount())
        assertEquals(1L, recorder.allVisits().first().id)
    }

    @Test
    fun oneVisitFromMotionSpike() = runTest {
        val clock = FakeClock()
        val cooldown = BathroomVisitCooldown(cooldownMs = 60_000L)
        val recorder = FakeBathroomVisitRecorder()
        val detector = BathroomVisitDetector(
            clock = clock,
            cooldown = cooldown,
            visitRecorder = recorder,
            entryMaxAgeMs = 15_000L,
        )

        val visitId = detector.onMotionSpike()

        assertEquals(1L, visitId)
        assertEquals(1, recorder.getVisitCount())
    }

    @Test
    fun secondSignalWithinWindowDoesNotCreateSecondVisit() = runTest {
        val clock = FakeClock()
        val cooldown = BathroomVisitCooldown(cooldownMs = 60_000L)
        val recorder = FakeBathroomVisitRecorder()
        val detector = BathroomVisitDetector(
            clock = clock,
            cooldown = cooldown,
            visitRecorder = recorder,
            entryMaxAgeMs = 15_000L,
        )

        detector.onLightSpike()
        val secondId = detector.onMotionSpike()

        assertNull(secondId)
        assertEquals(1, recorder.getVisitCount())
    }

    @Test
    fun newVisitAfterCooldown() = runTest {
        val clock = FakeClock()
        val cooldown = BathroomVisitCooldown(cooldownMs = 60_000L)
        val recorder = FakeBathroomVisitRecorder()
        val detector = BathroomVisitDetector(
            clock = clock,
            cooldown = cooldown,
            visitRecorder = recorder,
            entryMaxAgeMs = 15_000L,
        )

        detector.onLightSpike()

        clock.advanceBy(65_000L)

        val secondId = detector.onMotionSpike()

        assertEquals(2L, secondId)
        assertEquals(2, recorder.getVisitCount())
    }

    @Test
    fun signalTooOldDoesNotCreateVisit() = runTest {
        val clock = FakeClock()
        val cooldown = BathroomVisitCooldown(cooldownMs = 60_000L)
        val recorder = FakeBathroomVisitRecorder()
        val detector = BathroomVisitDetector(
            clock = clock,
            cooldown = cooldown,
            visitRecorder = recorder,
            entryMaxAgeMs = 15_000L,
        )

        detector.onLightSpike()

        clock.advanceBy(20_000L)

        val secondId = detector.onMotionSpike()

        assertNull(secondId)
        assertEquals(1, recorder.getVisitCount())
    }
}
