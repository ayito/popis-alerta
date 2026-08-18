package org.popisalerta.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.popisalerta.app.data.AccessRepository
import org.popisalerta.app.data.local.AccessEntity

class MainScreenViewModel(private val accessRepository: AccessRepository) : ViewModel() {
    val uiState: StateFlow<MainScreenUiState> =
        accessRepository
            .observeAll()
            .map<List<AccessEntity>, MainScreenUiState>(MainScreenUiState::Success)
            .catch { emit(MainScreenUiState.Error(it)) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MainScreenUiState.Loading
            )

    fun registerTestAccess() {
        viewModelScope.launch {
            accessRepository.logTestAccess()
        }
    }

    fun deleteAllAccesses() {
        viewModelScope.launch {
            accessRepository.deleteAll()
        }
    }
}

sealed interface MainScreenUiState {
    data object Loading : MainScreenUiState

    data class Error(val throwable: Throwable) : MainScreenUiState

    data class Success(val accesses: List<AccessEntity>) : MainScreenUiState
}
