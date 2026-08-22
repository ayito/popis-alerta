package org.popisalerta.app

/**
 * Interfaz para obtener el instante actual.
 *
 * Permite inyectar un reloj real en producción y un reloj controlable en tests.
 */
interface Clock {
    fun currentTimeMillis(): Long
}
