package org.popisalerta.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.popisalerta.app.data.AlertSettingsRepository
import org.popisalerta.app.data.BathroomVisitRepository
import org.popisalerta.app.data.local.BathroomVisitEntity

class MainScreenViewModel(
    private val bathroomVisitRepository: BathroomVisitRepository,
    private val alertSettingsRepository: AlertSettingsRepository,
) : ViewModel() {

    val alertsEnabled: StateFlow<Boolean> =
        alertSettingsRepository
            .alertsEnabled
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = alertSettingsRepository.areAlertsEnabled(),
            )

    val visitsUiState: StateFlow<VisitsUiState> =
        bathroomVisitRepository
            .observeAllVisits()
            .map<List<BathroomVisitEntity>, VisitsUiState>(VisitsUiState::Success)
            .catch { emit(VisitsUiState.Error(it)) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = VisitsUiState.Loading,
            )

    fun toggleAlerts() {
        alertSettingsRepository.setAlertsEnabled(
            enabled = !alertSettingsRepository.areAlertsEnabled(),
        )
    }
}

sealed interface VisitsUiState {
    data object Loading : VisitsUiState

    data class Error(val throwable: Throwable) : VisitsUiState

    data class Success(val visits: List<BathroomVisitEntity>) : VisitsUiState
}
