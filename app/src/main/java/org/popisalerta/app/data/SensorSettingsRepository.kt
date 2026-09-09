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

    private val _alertPhone =
        MutableStateFlow(
            preferences.getString(ALERT_PHONE_KEY, DEFAULT_ALERT_PHONE)
                ?: DEFAULT_ALERT_PHONE
        )

    val lightThreshold: Flow<Float> = _lightThreshold.asStateFlow()
    val motionThreshold: Flow<Float> = _motionThreshold.asStateFlow()
    val alertPhone: Flow<String> = _alertPhone.asStateFlow()

    override fun currentLightThreshold(): Float = _lightThreshold.value

    override fun currentMotionThreshold(): Float = _motionThreshold.value

    fun currentAlertPhone(): String = _alertPhone.value

    fun setLightThreshold(threshold: Float) {
        preferences.edit().putFloat(LIGHT_THRESHOLD_KEY, threshold).apply()

        _lightThreshold.value = threshold
    }

    fun setMotionThreshold(threshold: Float) {
        preferences.edit().putFloat(MOTION_THRESHOLD_KEY, threshold).apply()

        _motionThreshold.value = threshold
    }

    fun setAlertPhone(phone: String) {
        preferences.edit().putString(ALERT_PHONE_KEY, phone).apply()

        _alertPhone.value = phone
    }

    private companion object {
        const val PREFERENCES_NAME = "sensor_settings"
        const val LIGHT_THRESHOLD_KEY = "light_threshold"
        const val MOTION_THRESHOLD_KEY = "motion_threshold"
        const val ALERT_PHONE_KEY = "alert_phone"
        const val DEFAULT_ALERT_PHONE = ""
        const val DEFAULT_LIGHT_THRESHOLD = 30f
        const val DEFAULT_MOTION_THRESHOLD = 1.5f
    }
}
