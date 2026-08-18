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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val accessLogger =
                AppAccessLogger(
                    DefaultAccessRepository(
                        AccessDatabase.getInstance(applicationContext).accessDao()
                    )
                )

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
                    MainNavigation()
                }
            }
        }
    }

}
