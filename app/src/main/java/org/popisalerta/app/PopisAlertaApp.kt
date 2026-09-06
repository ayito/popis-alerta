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
