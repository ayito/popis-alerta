package org.popisalerta.app.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.popisalerta.app.data.AlertSettingsRepository
import org.popisalerta.app.data.RoomVisitRepository
import org.popisalerta.app.data.local.RoomVisitEntity

sealed interface AlertsVisitsUiState {
    data object Loading : AlertsVisitsUiState

    data class Success(val visits: List<RoomVisitEntity>) : AlertsVisitsUiState

    data class Error(val throwable: Throwable) : AlertsVisitsUiState
}

class AlertsScreenViewModel(
    private val roomVisitRepository: RoomVisitRepository,
    private val alertSettingsRepository: AlertSettingsRepository
) : ViewModel() {

    private val _visitsUiState = MutableStateFlow<AlertsVisitsUiState>(AlertsVisitsUiState.Loading)

    val visitsUiState: StateFlow<AlertsVisitsUiState> = _visitsUiState.asStateFlow()

    private val _alertsEnabled = MutableStateFlow(alertSettingsRepository.areAlertsEnabled())

    val alertsEnabled: StateFlow<Boolean> = _alertsEnabled.asStateFlow()

    init {
        observeVisits()
        observeAlertsEnabled()
    }

    private fun observeVisits() {
        viewModelScope.launch {
            roomVisitRepository
                .observeAllVisits()
                .catch { throwable ->
                    _visitsUiState.value = AlertsVisitsUiState.Error(throwable)
                }
                .collect { visits ->
                    _visitsUiState.value = AlertsVisitsUiState.Success(visits)
                }
        }
    }

    private fun observeAlertsEnabled() {
        viewModelScope.launch {
            alertSettingsRepository.alertsEnabled.collect { enabled ->
                _alertsEnabled.value = enabled
            }
        }
    }

    fun toggleAlerts() {
        alertSettingsRepository.setAlertsEnabled(enabled = !alertsEnabled.value)
    }

    fun clearVisits() {
        viewModelScope.launch {
            try {
                roomVisitRepository.deleteAllVisits()
            } catch (throwable: Throwable) {
                _visitsUiState.value = AlertsVisitsUiState.Error(throwable)
            }
        }
    }
}
