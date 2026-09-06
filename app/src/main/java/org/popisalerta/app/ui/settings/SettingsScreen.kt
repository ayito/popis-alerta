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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs
import kotlin.math.sqrt
import org.popisalerta.app.R
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
    val accelerometerSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

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
        lightSensor?.let { sensor ->
            sensorManager.registerListener(
                sensorListener,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        accelerometerSensor?.let { sensor ->
            sensorManager.registerListener(
                sensorListener,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    val lightStatus =
        when {
            lightSensor == null -> stringResource(R.string.settings_light_sensor_unavailable)

            !hasLightReading -> stringResource(R.string.settings_waiting_reading)

            luxCurrent > currentLightThreshold ->
                stringResource(R.string.settings_light_above_threshold)

            else -> stringResource(R.string.settings_light_below_threshold)
        }

    val motionStatus =
        when {
            accelerometerSensor == null ->
                stringResource(R.string.settings_motion_sensor_unavailable)

            !hasMotionReading -> stringResource(R.string.settings_waiting_reading)

            accelCurrent > currentMotionThreshold ->
                stringResource(R.string.settings_motion_above_threshold)

            else -> stringResource(R.string.settings_motion_below_threshold)
        }

    val validationErrorMessage = stringResource(R.string.settings_validation_error)

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Text(
                text = stringResource(R.string.settings_live_calibration_title),
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            Text(
                text = stringResource(R.string.settings_light_sensor_title),
                style = MaterialTheme.typography.titleMedium
            )
        }

        item {
            Text(
                text =
                    if (hasLightReading) {
                        stringResource(
                            R.string.settings_current_lux,
                            luxCurrent.toInt()
                        )
                    } else {
                        stringResource(R.string.settings_current_lux_waiting)
                    }
            )
        }

        item {
            Text(
                text = lightStatus,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text =
                    stringResource(
                        R.string.settings_saved_lux_threshold,
                        currentLightThreshold.toInt()
                    ),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = stringResource(R.string.settings_motion_sensor_title),
                style = MaterialTheme.typography.titleMedium
            )
        }

        item {
            Text(
                text =
                    if (hasMotionReading) {
                        stringResource(
                            R.string.settings_current_motion,
                            formatDecimal(accelCurrent)
                        )
                    } else {
                        stringResource(R.string.settings_current_motion_waiting)
                    }
            )
        }

        item {
            Text(
                text = motionStatus,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text =
                    stringResource(
                        R.string.settings_saved_motion_threshold,
                        formatDecimal(currentMotionThreshold)
                    ),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = stringResource(R.string.settings_thresholds_title),
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            Text(
                text = stringResource(R.string.settings_thresholds_help),
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
                label = {
                    Text(text = stringResource(R.string.settings_light_threshold_label))
                },
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
                label = {
                    Text(text = stringResource(R.string.settings_motion_threshold_label))
                },
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
                    text = stringResource(R.string.settings_unsaved_changes),
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

                    if (
                        lightThreshold == null ||
                        motionThreshold == null ||
                        lightThreshold < 0f ||
                        motionThreshold < 0f
                    ) {
                        validationError = validationErrorMessage
                    } else {
                        viewModel.saveThresholds(
                            lightThreshold = lightThreshold,
                            motionThreshold = motionThreshold
                        )
                        // Normalizar el texto mostrado
                        lightThresholdText =
                            String.format(java.util.Locale.getDefault(), "%.2f", lightThreshold)
                        motionThresholdText =
                            String.format(java.util.Locale.getDefault(), "%.2f", motionThreshold)
                        validationError = null
                        hasUnsavedChanges = false
                    }
                },
                enabled = hasUnsavedChanges,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.settings_save_action))
            }
        }

        item {
            Text(
                text =
                    stringResource(
                        if (hasUnsavedChanges) {
                            R.string.settings_values_not_active
                        } else {
                            R.string.settings_values_saved
                        }
                    ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private const val GRAVITY = 9.81f

private fun formatDecimal(value: Float): String =
    String.format(java.util.Locale.getDefault(), "%.2f", value)
