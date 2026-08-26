package org.popisalerta.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.popisalerta.app.SensorThresholds

class SensorSettingsRepository(context: Context) : SensorThresholds {

    private val preferences: SharedPreferences =
            context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val _lightThreshold =
            MutableStateFlow(preferences.getFloat(LIGHT_THRESHOLD_KEY, DEFAULT_LIGHT_THRESHOLD))

    private val _motionThreshold =
            MutableStateFlow(preferences.getFloat(MOTION_THRESHOLD_KEY, DEFAULT_MOTION_THRESHOLD))

    val lightThreshold: Flow<Float> = _lightThreshold.asStateFlow()
    val motionThreshold: Flow<Float> = _motionThreshold.asStateFlow()

    override fun currentLightThreshold(): Float = _lightThreshold.value

    override fun currentMotionThreshold(): Float = _motionThreshold.value

    fun setLightThreshold(threshold: Float) {
        preferences.edit().putFloat(LIGHT_THRESHOLD_KEY, threshold).apply()

        _lightThreshold.value = threshold
    }

    fun setMotionThreshold(threshold: Float) {
        preferences.edit().putFloat(MOTION_THRESHOLD_KEY, threshold).apply()

        _motionThreshold.value = threshold
    }

    private companion object {
        const val PREFERENCES_NAME = "sensor_settings"
        const val LIGHT_THRESHOLD_KEY = "light_threshold"
        const val MOTION_THRESHOLD_KEY = "motion_threshold"
        const val DEFAULT_LIGHT_THRESHOLD = 100f
        const val DEFAULT_MOTION_THRESHOLD = 2f
    }
}
