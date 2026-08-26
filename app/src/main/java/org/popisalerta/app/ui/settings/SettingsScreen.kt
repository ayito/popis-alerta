package org.popisalerta.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(onClick = onBack) { Text(text = "Volver") }

        Text(text = "Configuración", style = MaterialTheme.typography.headlineMedium)

        Text(
                text = "La configuración de sensores estará disponible aquí.",
                style = MaterialTheme.typography.bodyLarge
        )
    }
}
