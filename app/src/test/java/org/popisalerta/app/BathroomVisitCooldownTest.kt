package org.popisalerta.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BathroomVisitCooldownTest {

    private val cooldownMs = 5 * 60 * 1_000L
    private val subject = BathroomVisitCooldown(cooldownMs = cooldownMs)

    @Test
    fun `permite crear una visita cuando no existe una visita anterior`() {
        val canCreateVisit =
            subject.canCreateVisit(
                nowMs = 1_000L,
                lastVisitStartedAtMs = null
            )

        assertTrue(canCreateVisit)
    }

    @Test
    fun `no permite crear una visita dentro del cooldown`() {
        val lastVisitStartedAtMs = 1_000L
        val nowMs = lastVisitStartedAtMs + cooldownMs - 1L

        val canCreateVisit =
            subject.canCreateVisit(
                nowMs = nowMs,
                lastVisitStartedAtMs = lastVisitStartedAtMs
            )

        assertFalse(canCreateVisit)
    }

    @Test
    fun `permite crear una visita exactamente al finalizar el cooldown`() {
        val lastVisitStartedAtMs = 1_000L
        val nowMs = lastVisitStartedAtMs + cooldownMs

        val canCreateVisit =
            subject.canCreateVisit(
                nowMs = nowMs,
                lastVisitStartedAtMs = lastVisitStartedAtMs
            )

        assertTrue(canCreateVisit)
    }

    @Test
    fun `permite crear una visita despues del cooldown`() {
        val lastVisitStartedAtMs = 1_000L
        val nowMs = lastVisitStartedAtMs + cooldownMs + 1L

        val canCreateVisit =
            subject.canCreateVisit(
                nowMs = nowMs,
                lastVisitStartedAtMs = lastVisitStartedAtMs
            )

        assertTrue(canCreateVisit)
    }
}
