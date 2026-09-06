package org.popisalerta.app

/**
 * Interfaz para registrar posibles accesos a una sala.
 *
 * Permite aislar la lógica de sensores de la persistencia y de Android en tests unitarios.
 */
interface RoomVisitRecorder {

    /**
     * Registra un posible acceso a una sala.
     *
     * @param startedAtMs instante de inicio en milisegundos desde epoch.
     * @return identificador de la visita creada.
     */
    suspend fun recordVisit(startedAtMs: Long): Long

    /**
     * Devuelve el instante de inicio de la última visita registrada, o `null` si no hay ninguna.
     */
    suspend fun getLastVisitStartedAtMs(): Long?

    /**
     * Devuelve el número total de visitas registradas.
     */
    suspend fun getVisitCount(): Int
}
