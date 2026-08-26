package org.popisalerta.app.ui.calibration

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.sqrt
import org.popisalerta.app.data.SensorSettingsRepository

@Composable
fun CalibrationScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: CalibrationViewModel = viewModel {
        CalibrationViewModel(
                sensorSettingsRepository = SensorSettingsRepository(context.applicationContext)
        )
    }

    val lightThreshold by viewModel.lightThreshold.collectAsStateWithLifecycle()
    val motionThreshold by viewModel.motionThreshold.collectAsStateWithLifecycle()

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
                        luxStatus =
                                if (lux > lightThreshold) {
                                    "por encima del umbral"
                                } else {
                                    "por debajo del umbral"
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
                        accelStatus =
                                if (dynamicAccel > motionThreshold) {
                                    "en movimiento"
                                } else {
                                    "en reposo"
                                }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    LaunchedEffect(isTestMode) {
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
        Text(text = "Calibrar sensores", style = MaterialTheme.typography.headlineMedium)

        // Luz
        Text(text = "Sensor de luz", style = MaterialTheme.typography.titleLarge)
        Text(text = "Lux actual: ${luxCurrent.toInt()}")
        Text(text = "Umbral de luz: ${lightThreshold.toInt()}")
        Text(text = "Estado: $luxStatus")

        // Movimiento
        Text(text = "Sensor de movimiento", style = MaterialTheme.typography.titleLarge)
        Text(text = "Aceleración: ${String.format("%.2f", accelCurrent)} m/s²")
        Text(text = "Umbral de movimiento: ${motionThreshold.toInt()}")
        Text(text = "Estado: $accelStatus")

        // Botón de prueba
        Button(onClick = { isTestMode = !isTestMode }, modifier = Modifier.fillMaxWidth()) {
            Text(text = if (isTestMode) "Detener prueba" else "Probar sin guardar visitas")
        }

        // Botón de volver
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(text = "Volver") }
    }
}
