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
import org.popisalerta.app.data.DefaultAccessRepository
import org.popisalerta.app.data.local.AccessDatabase
import org.popisalerta.app.theme.PopisAlertaTheme

class MainActivity : ComponentActivity() {
    private lateinit var accessLogger: AppAccessLogger
    private var hasResumed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accessLogger =
            AppAccessLogger(
                DefaultAccessRepository(
                    AccessDatabase.getInstance(applicationContext).accessDao()
                )
            )

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
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainNavigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (hasResumed) {
            lifecycleScope.launch {
                accessLogger.logAppResume()
            }
        } else {
            hasResumed = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(HAS_RESUMED_STATE_KEY, hasResumed)
        super.onSaveInstanceState(outState)
    }

    private companion object {
        const val HAS_RESUMED_STATE_KEY = "has_resumed"
    }
}
