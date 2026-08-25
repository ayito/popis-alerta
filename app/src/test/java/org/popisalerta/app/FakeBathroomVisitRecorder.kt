package org.popisalerta.app

/**
 * Implementación de prueba de [BathroomVisitRecorder] que no usa base de datos.
 *
 * Genera IDs deterministas (1, 2, 3...) para facilitar la verificación en tests.
 */
class FakeBathroomVisitRecorder : BathroomVisitRecorder {

    private var nextId = 1L
    private var lastVisitStartedAtMs: Long? = null
    private val visits = mutableListOf<Visit>()

    data class Visit(
        val id: Long,
        val startedAtMs: Long,
    )

    override suspend fun recordVisit(startedAtMs: Long): Long {
        val id = nextId++
        lastVisitStartedAtMs = startedAtMs
        visits += Visit(id, startedAtMs)
        return id
    }

    override suspend fun getLastVisitStartedAtMs(): Long? = lastVisitStartedAtMs

    override suspend fun getVisitCount(): Int = visits.size

    fun allVisits(): List<Visit> = visits.toList()
}
