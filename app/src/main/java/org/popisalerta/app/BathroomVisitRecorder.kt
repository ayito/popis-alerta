package org.popisalerta.app

/**
 * Interfaz para registrar visitas al baño.
 *
 * Permite aislar la lógica de sensores de Room y Android en tests unitarios.
 */
interface BathroomVisitRecorder {

    /**
     * Registra una nueva visita al baño.
     *
     * @param startedAtMs instante de inicio en ms desde epoch.
     * @return el identificador o entidad de la visita creada, si hace falta para actualizarla luego.
     */
    suspend fun recordVisit(startedAtMs: Long): Long

    /**
     * Devuelve el instante de inicio de la última visita registrada, o null si no hay ninguna.
     */
    suspend fun getLastVisitStartedAtMs(): Long?

    /**
     * Devuelve el número total de visitas registradas.
     */
    suspend fun getVisitCount(): Int
}
