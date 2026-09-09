package org.popisalerta.app.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import org.popisalerta.app.data.SensorSettingsRepository

class SettingsViewModel(private val sensorSettingsRepository: SensorSettingsRepository) :
    ViewModel() {

    val lightThreshold: Flow<Float> = sensorSettingsRepository.lightThreshold
    val motionThreshold: Flow<Float> = sensorSettingsRepository.motionThreshold
    val alertPhone: Flow<String> = sensorSettingsRepository.alertPhone

    fun currentLightThreshold(): Float = sensorSettingsRepository.currentLightThreshold()
    fun currentMotionThreshold(): Float = sensorSettingsRepository.currentMotionThreshold()
    fun currentAlertPhone(): String = sensorSettingsRepository.currentAlertPhone()

    fun saveThresholds(lightThreshold: Float, motionThreshold: Float) {
        sensorSettingsRepository.setLightThreshold(lightThreshold)
        sensorSettingsRepository.setMotionThreshold(motionThreshold)
    }

    fun saveAlertPhone(phone: String) {
        sensorSettingsRepository.setAlertPhone(phone)
    }
}
