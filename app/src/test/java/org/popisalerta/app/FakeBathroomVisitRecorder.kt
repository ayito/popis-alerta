package org.popisalerta.app

/**
 * Implementación fake de [BathroomVisitRecorder] para tests.
 *
 * Almacena las visitas en memoria y permite consultarlas de forma determinista.
 */
class FakeBathroomVisitRecorder : BathroomVisitRecorder {

    data class Visit(val startedAtMs: Long)

    private val _visits = mutableListOf<Visit>()

    val visits: List<Visit> get() = _visits.toList()

    override suspend fun recordVisit(startedAtMs: Long): Long {
        _visits.add(Visit(startedAtMs = startedAtMs))
        return startedAtMs
    }

    override suspend fun getLastVisitStartedAtMs(): Long? = _visits.lastOrNull()?.startedAtMs

    override suspend fun getVisitCount(): Int = _visits.size
}
