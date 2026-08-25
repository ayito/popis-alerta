package org.popisalerta.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlertSettingsRepository(context: Context) {

    private val preferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    private val _alertsEnabled =
        MutableStateFlow(
            preferences.getBoolean(ALERTS_ENABLED_KEY, DEFAULT_ALERTS_ENABLED)
        )

    val alertsEnabled: Flow<Boolean> = _alertsEnabled.asStateFlow()

    fun areAlertsEnabled(): Boolean = _alertsEnabled.value

    fun setAlertsEnabled(enabled: Boolean) {
        preferences
            .edit()
            .putBoolean(ALERTS_ENABLED_KEY, enabled)
            .apply()

        _alertsEnabled.value = enabled
    }

    private companion object {
        const val PREFERENCES_NAME = "alert_settings"
        const val ALERTS_ENABLED_KEY = "alerts_enabled"
        const val DEFAULT_ALERTS_ENABLED = true
    }
}
