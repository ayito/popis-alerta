package org.popisalerta.app.ui.settings

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs
import kotlin.math.sqrt
import org.popisalerta.app.data.SensorSettingsRepository

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
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
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    var luxCurrent by remember { mutableFloatStateOf(0f) }
    var hasLightReading by remember { mutableStateOf(false) }
    var accelCurrent by remember { mutableFloatStateOf(0f) }
    var hasMotionReading by remember { mutableStateOf(false) }

    val currentLightThreshold by rememberUpdatedState(savedLightThreshold)
    val currentMotionThreshold by rememberUpdatedState(savedMotionThreshold)

    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val lightSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) }
    val accelerometerSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_LIGHT -> {
                        luxCurrent = event.values[0]
                        hasLightReading = true
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        val ax = event.values[0]
                        val ay = event.values[1]
                        val az = event.values[2]
                        val magnitude = sqrt(ax * ax + ay * ay + az * az)

                        accelCurrent = abs(magnitude - GRAVITY)
                        hasMotionReading = true
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
    }

    DisposableEffect(sensorManager, lightSensor, accelerometerSensor, sensorListener) {
        lightSensor?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelerometerSensor?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        onDispose { sensorManager.unregisterListener(sensorListener) }
    }

    val lightStatus =
            when {
                lightSensor == null -> "Este teléfono no tiene sensor de luz."
                !hasLightReading -> "Esperando una lectura…"
                luxCurrent > currentLightThreshold -> "Por encima del umbral guardado."
                else -> "Por debajo del umbral guardado."
            }

    val motionStatus =
            when {
                accelerometerSensor == null -> "Este teléfono no tiene acelerómetro."
                !hasMotionReading -> "Esperando una lectura…"
                accelCurrent > currentMotionThreshold -> "En movimiento según el umbral guardado."
                else -> "En reposo según el umbral guardado."
            }

    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text(text = "Configuración", style = MaterialTheme.typography.headlineMedium) }

        item {
            Text(text = "Calibración en tiempo real", style = MaterialTheme.typography.titleLarge)
        }

        item { Text(text = "Sensor de luz", style = MaterialTheme.typography.titleMedium) }

        item {
            Text(
                    text =
                            if (hasLightReading) {
                                "Lux actual: ${luxCurrent.toInt()} lux"
                            } else {
                                "Lux actual: esperando lectura…"
                            }
            )
        }

        item { Text(text = lightStatus, style = MaterialTheme.typography.bodyMedium) }

        item {
            Text(
                    text = "Umbral guardado: ${currentLightThreshold.toInt()} lux",
                    style = MaterialTheme.typography.bodyMedium
            )
        }

        item { Text(text = "Sensor de movimiento", style = MaterialTheme.typography.titleMedium) }

        item {
            Text(
                    text =
                            if (hasMotionReading) {
                                "Movimiento actual: ${formatDecimal(accelCurrent)} m/s²"
                            } else {
                                "Movimiento actual: esperando lectura…"
                            }
            )
        }

        item { Text(text = motionStatus, style = MaterialTheme.typography.bodyMedium) }

        item {
            Text(
                    text = "Umbral guardado: ${formatDecimal(currentMotionThreshold)} m/s²",
                    style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                    text = "Configuración de los umbrales",
                    style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            Text(
                    text =
                            "Los valores se aplican al pulsar Guardar cambios. " +
                                    "Mientras esta pantalla está abierta solo se muestran mediciones; " +
                                    "no se registran visitas desde esta pantalla.",
                    style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            OutlinedTextField(
                    value = lightThresholdText,
                    onValueChange = {
                        lightThresholdText = it
                        validationError = null
                        hasUnsavedChanges = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Umbral de luz (lux)") },
                    keyboardOptions =
                            androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                            ),
                    singleLine = true
            )
        }

        item {
            OutlinedTextField(
                    value = motionThresholdText,
                    onValueChange = {
                        motionThresholdText = it
                        validationError = null
                        hasUnsavedChanges = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Umbral de movimiento (m/s²)") },
                    keyboardOptions =
                            androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                            ),
                    singleLine = true
            )
        }

        validationError?.let { error ->
            item {
                Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (hasUnsavedChanges) {
            item {
                Text(
                        text = "Hay cambios sin guardar.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Button(
                    onClick = {
                        val lightThreshold = lightThresholdText.replace(',', '.').toFloatOrNull()
                        val motionThreshold = motionThresholdText.replace(',', '.').toFloatOrNull()

                        if (lightThreshold == null ||
                                        motionThreshold == null ||
                                        lightThreshold < 0f ||
                                        motionThreshold < 0f
                        ) {
                            validationError =
                                    "Introduce valores numéricos iguales o mayores que cero."
                        } else {
                            viewModel.saveThresholds(
                                    lightThreshold = lightThreshold,
                                    motionThreshold = motionThreshold
                            )
                            validationError = null
                            hasUnsavedChanges = false
                        }
                    },
                    enabled = hasUnsavedChanges,
                    modifier = Modifier.fillMaxWidth()
            ) { Text(text = "Guardar cambios") }
        }

        item {
            Text(
                    text =
                            if (hasUnsavedChanges) {
                                "Los valores editados todavía no están activos."
                            } else {
                                "Los umbrales mostrados corresponden a la configuración guardada."
                            },
                    style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private const val GRAVITY = 9.81f

private fun formatDecimal(value: Float): String = "%.2f".format(value)
