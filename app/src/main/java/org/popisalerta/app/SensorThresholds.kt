package org.popisalerta.app

interface SensorThresholds {
    fun currentLightThreshold(): Float

    fun currentMotionThreshold(): Float
}
