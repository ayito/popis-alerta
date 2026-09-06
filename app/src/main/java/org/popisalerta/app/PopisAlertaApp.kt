package org.popisalerta.app

import android.app.Application
import org.popisalerta.app.data.AlertSettingsRepository
import org.popisalerta.app.data.SensorSettingsRepository
import org.popisalerta.app.data.local.AccessDatabase

class PopisAlertaApp : Application() {

    lateinit var sensors: Sensors
        private set

    lateinit var alertSettingsRepository: AlertSettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // Configurar locale de la app para Android 13+
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
            androidx.core.os.LocaleListCompat.create(java.util.Locale("es", "ES"))
        )

        val currentLocale = java.util.Locale.getDefault()
        android.util.Log.d(
            "Locale-Check",
            "Locale: $currentLocale displayName=${currentLocale.displayName}"
        )

        val database = AccessDatabase.getInstance(this)
        alertSettingsRepository = AlertSettingsRepository(this)

        val sensorThresholds = SensorSettingsRepository(this)
        val notificationHelper = NotificationHelper(this)
        val visitRecorder = DatabaseRoomVisitRecorder(database.roomVisitDao())

        notificationHelper.createAlertChannel()

        sensors =
            RoomSensors(
                context = this,
                accessDao = database.accessDao(),
                roomEntryDao = database.roomEntryDao(),
                visitRecorder = visitRecorder,
                sensorThresholds = sensorThresholds,
                alertsEnabledProvider = alertSettingsRepository::areAlertsEnabled,
                notificationHelper = notificationHelper
            )
    }
}
