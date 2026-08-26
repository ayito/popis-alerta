package org.popisalerta.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.popisalerta.app.data.AccessRepositoryProvider
import org.popisalerta.app.data.SensorSettingsRepository
import org.popisalerta.app.data.local.AccessDatabase
import org.popisalerta.app.theme.PopisAlertaTheme

class MainActivity : ComponentActivity() {
    private lateinit var accessLogger: AppAccessLogger
    private var hasResumed = false
    private lateinit var roomSensors: RoomSensors

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("PopisAlerta", "MainActivity.onCreate() started")

        accessLogger =
            AppAccessLogger(
                AccessRepositoryProvider.create(applicationContext)
            )

        hasResumed = savedInstanceState?.getBoolean(HAS_RESUMED_STATE_KEY) ?: false

        if (savedInstanceState == null) {
            lifecycleScope.launch {
                accessLogger.logAppOpen()
            }
        }

        enableEdgeToEdge()

        Log.d("PopisAlerta", "About to get AccessDatabase instance")
        val db = AccessDatabase.getInstance(applicationContext)
        Log.d("PopisAlerta", "AccessDatabase instance obtained: $db")

        val dao = db.bathroomVisitDao()
        Log.d("PopisAlerta", "BathroomVisitDao obtained: $dao")

        val visitRecorder = RoomBathroomVisitRecorder(dao)
        Log.d("PopisAlerta", "RoomBathroomVisitRecorder created: $visitRecorder")

                val sensorThresholds = SensorSettingsRepository(applicationContext)

        roomSensors = RoomSensors(
            context = this,
            accessDao = db.accessDao(),
            roomEntryDao = db.roomEntryDao(),
            visitRecorder = visitRecorder,
            sensorThresholds = sensorThresholds,
        )
        Log.d("PopisAlerta", "RoomSensors created: $roomSensors")

        setContent {
            PopisAlertaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
        Log.d("PopisAlerta", "Content set")
    }

    override fun onResume() {
        super.onResume()
        Log.d("PopisAlerta", "onResume() called")
        roomSensors.start()

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
        roomSensors.stop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(HAS_RESUMED_STATE_KEY, hasResumed)
        super.onSaveInstanceState(outState)
    }

    private companion object {
        const val HAS_RESUMED_STATE_KEY = "has_resumed"
    }
}
