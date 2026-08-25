package org.popisalerta.app

/**
 * Interfaz para el sistema de sensores que detecta luz y movimiento.
 */
interface Sensors {
    /**
     * Inicia la escucha de sensores.
     */
    fun start()

    /**
     * Detiene la escucha de sensores.
     */
    fun stop()
}
