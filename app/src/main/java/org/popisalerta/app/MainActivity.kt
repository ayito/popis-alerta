package org.popisalerta.app

import android.os.Bundle
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
import org.popisalerta.app.theme.PopisAlertaTheme

class MainActivity : ComponentActivity() {
    private lateinit var accessLogger: AppAccessLogger
    private var hasResumed = false
    private lateinit var roomSensors: RoomSensors

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        roomSensors = RoomSensors(this)

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
    }

    override fun onResume() {
        super.onResume()
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
