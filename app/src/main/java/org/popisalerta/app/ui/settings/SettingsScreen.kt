package org.popisalerta.app.ui.settings

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.sqrt
import org.popisalerta.app.data.SensorSettingsRepository

@Composable
fun SettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
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
    var changesSaved by remember { mutableStateOf(false) }

    // Lecturas en tiempo real
    var luxCurrent by remember { mutableFloatStateOf(0f) }
    var luxStatus by remember { mutableStateOf("—") }
    var accelCurrent by remember { mutableFloatStateOf(0f) }
    var accelStatus by remember { mutableStateOf("—") }
    var isTestMode by remember { mutableStateOf(false) }

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
                        val lux = event.values[0]
                        luxCurrent = lux
                        val threshold = savedLightThreshold
                        luxStatus =
                                if (lux > threshold) {
                                    "por encima del umbral ($threshold)"
                                } else {
                                    "por debajo del umbral ($threshold)"
                                }
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        val ax = event.values[0]
                        val ay = event.values[1]
                        val az = event.values[2]
                        val magnitude = sqrt(ax * ax + ay * ay + az * az)
                        val gravity = 9.81f
                        val dynamicAccel = kotlin.math.abs(magnitude - gravity)
                        accelCurrent = dynamicAccel
                        val threshold = savedMotionThreshold
                        accelStatus =
                                if (dynamicAccel > threshold) {
                                    "en movimiento (umbral: $threshold)"
                                } else {
                                    "en reposo (umbral: $threshold)"
                                }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    LaunchedEffect(isTestMode, savedLightThreshold, savedMotionThreshold) {
        if (isTestMode) {
            lightSensor?.let {
                sensorManager.registerListener(
                        sensorListener,
                        it,
                        SensorManager.SENSOR_DELAY_NORMAL
                )
            }
            accelerometerSensor?.let {
                sensorManager.registerListener(
                        sensorListener,
                        it,
                        SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        } else {
            sensorManager.unregisterListener(sensorListener)
            luxCurrent = 0f
            luxStatus = "—"
            accelCurrent = 0f
            accelStatus = "—"
        }
    }

    DisposableEffect(Unit) { onDispose { sensorManager.unregisterListener(sensorListener) } }

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

        // Sección de calibración en tiempo real
        Text(text = "Calibración en tiempo real", style = MaterialTheme.typography.titleLarge)

        Text(text = "Sensor de luz", style = MaterialTheme.typography.titleMedium)
        Text(text = "Lux actual: ${luxCurrent.toInt()}")
        Text(text = "Estado: $luxStatus")

        Text(text = "Sensor de movimiento", style = MaterialTheme.typography.titleMedium)
        Text(text = "Aceleración: ${String.format("%.2f", accelCurrent)} m/s²")
        Text(text = "Estado: $accelStatus")

        Button(onClick = { isTestMode = !isTestMode }, modifier = Modifier.fillMaxWidth()) {
            Text(text = if (isTestMode) "Detener prueba" else "Probar sin guardar visitas")
        }
    }
}

