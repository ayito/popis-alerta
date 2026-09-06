package org.popisalerta.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.popisalerta.app.R

class NotificationHelper(private val context: Context) {

    fun createAlertChannel() {
        val channel =
            NotificationChannel(
                ALERT_CHANNEL_ID,
                context.getString(R.string.alerts_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.alerts_channel_description)
            }

        context
            .getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun showTestNotification() {
        val notification =
            NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(context.getString(R.string.notification_test_title))
                .setContentText(context.getString(R.string.notification_test_message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(context).notify(TEST_NOTIFICATION_ID, notification)
    }

    fun showAlert(visitId: Long) {
        val notification =
            NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(context.getString(R.string.notification_visit_title))
                .setContentText(context.getString(R.string.notification_visit_message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(context).notify(visitId.toInt(), notification)
    }

    private companion object {
        const val ALERT_CHANNEL_ID = "room_alerts"
        const val TEST_NOTIFICATION_ID = 1001
    }
}
