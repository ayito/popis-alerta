package org.popisalerta.app

/**
 * Reloj que usa el tiempo del sistema.
 */
class SystemClock : Clock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
