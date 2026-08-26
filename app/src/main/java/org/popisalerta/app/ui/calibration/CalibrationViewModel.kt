package org.popisalerta.app.ui.calibration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.popisalerta.app.data.SensorSettingsRepository

class CalibrationViewModel(private val sensorSettingsRepository: SensorSettingsRepository) :
        ViewModel() {

    val lightThreshold: StateFlow<Float> =
            sensorSettingsRepository.lightThreshold.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = sensorSettingsRepository.currentLightThreshold()
            )

    val motionThreshold: StateFlow<Float> =
            sensorSettingsRepository.motionThreshold.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = sensorSettingsRepository.currentMotionThreshold()
            )
}
