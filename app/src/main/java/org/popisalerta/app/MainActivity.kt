package org.popisalerta.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.popisalerta.app.data.AccessRepositoryProvider
import org.popisalerta.app.theme.PopisAlertaTheme

class MainActivity : ComponentActivity() {

    private lateinit var accessLogger: AppAccessLogger
    private lateinit var notificationHelper: NotificationHelper
    private var hasResumed = false

    private val sensors: Sensors
        get() = (application as PopisAlertaApp).sensors

    private val requestNotificationsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d(
                "PopisAlerta",
                if (granted) {
                    "Notification permission granted"
                } else {
                    "Notification permission denied"
                }
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("PopisAlerta", "MainActivity.onCreate() started")

        notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.createAlertChannel()

        accessLogger = AppAccessLogger(AccessRepositoryProvider.create(applicationContext))

        hasResumed = savedInstanceState?.getBoolean(HAS_RESUMED_STATE_KEY) ?: false

        if (savedInstanceState == null) {
            lifecycleScope.launch {
                accessLogger.logAppOpen()
            }
        }

        enableEdgeToEdge()

        setContent {
            PopisAlertaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(
                        sensors = sensors,
                        alertSettingsRepository = (application as PopisAlertaApp).alertSettingsRepository
                    )
                }
            }
        }

        requestNotificationPermissionIfNeeded()

        Log.d("PopisAlerta", "Content set")
    }

    override fun onResume() {
        super.onResume()
        Log.d("PopisAlerta", "onResume() called")
        sensors.start()

        if (hasResumed) {
            lifecycleScope.launch {
                accessLogger.logAppResume()
            }
        } else {
            hasResumed = true
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("PopisAlerta", "onPause() called")
        sensors.stop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(HAS_RESUMED_STATE_KEY, hasResumed)
        super.onSaveInstanceState(outState)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permissionGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            requestNotificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private companion object {
        const val HAS_RESUMED_STATE_KEY = "has_resumed"
    }
}
