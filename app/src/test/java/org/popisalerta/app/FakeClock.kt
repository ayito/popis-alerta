package org.popisalerta.app

/**
 * Reloj controlable para tests.
 *
 * Permite fijar un instante inicial y avanzar el tiempo de forma determinista.
 */
class FakeClock(initialTimeMs: Long = 0L) : Clock {

    private var currentTimeMs: Long = initialTimeMs

    override fun currentTimeMillis(): Long = currentTimeMs

    /**
     * Avanza el reloj la cantidad de milisegundos indicada.
     */
    fun advanceBy(deltaMs: Long) {
        require(deltaMs >= 0) { "deltaMs debe ser >= 0, pero era $deltaMs" }
        currentTimeMs += deltaMs
    }

    /**
     * Fija el reloj a un instante concreto.
     */
    fun setTime(timeMs: Long) {
        currentTimeMs = timeMs
    }
}
