package org.popisalerta.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.popisalerta.app.data.SensorSettingsRepository

@Composable
fun SettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: SettingsViewModel = viewModel {
        SettingsViewModel(
                sensorSettingsRepository = SensorSettingsRepository(context.applicationContext)
        )
    }

    val savedLightThreshold by
            viewModel.lightThreshold.collectAsStateWithLifecycle(
                    initialValue = viewModel.currentLightThreshold()
            )
    val savedMotionThreshold by
            viewModel.motionThreshold.collectAsStateWithLifecycle(
                    initialValue = viewModel.currentMotionThreshold()
            )

    var lightThresholdText by
            remember(savedLightThreshold) { mutableStateOf(savedLightThreshold.toString()) }
    var motionThresholdText by
            remember(savedMotionThreshold) { mutableStateOf(savedMotionThreshold.toString()) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var changesSaved by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(onClick = onBack) { Text(text = "Volver") }

        Text(text = "Configuración", style = MaterialTheme.typography.headlineMedium)

        Text(text = "Detección de acceso", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
                value = lightThresholdText,
                onValueChange = {
                    lightThresholdText = it
                    validationError = null
                    changesSaved = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Umbral de luz (lux)") },
                keyboardOptions =
                        androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                        ),
                singleLine = true
        )

        OutlinedTextField(
                value = motionThresholdText,
                onValueChange = {
                    motionThresholdText = it
                    validationError = null
                    changesSaved = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Umbral de movimiento") },
                keyboardOptions =
                        androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                        ),
                singleLine = true
        )

        Text(
                text =
                        "Cuando una lectura supera uno de estos valores, " +
                                "puede registrarse una visita.",
                style = MaterialTheme.typography.bodyMedium
        )

        validationError?.let { error ->
            Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
            )
        }

        if (changesSaved) {
            Text(
                    text = "Cambios guardados.",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
                onClick = {
                    val lightThreshold = lightThresholdText.replace(',', '.').toFloatOrNull()
                    val motionThreshold = motionThresholdText.replace(',', '.').toFloatOrNull()

                    if (lightThreshold == null ||
                                    motionThreshold == null ||
                                    lightThreshold < 0f ||
                                    motionThreshold < 0f
                    ) {
                        validationError = "Introduce valores numéricos iguales o mayores que cero."
                        changesSaved = false
                    } else {
                        viewModel.saveThresholds(
                                lightThreshold = lightThreshold,
                                motionThreshold = motionThreshold
                        )
                        validationError = null
                        changesSaved = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
        ) { Text(text = "Guardar cambios") }
    }
}
