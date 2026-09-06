package org.popisalerta.app.ui.alerts

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Date
import org.popisalerta.app.R
import org.popisalerta.app.Sensors
import org.popisalerta.app.data.AlertSettingsRepository
import org.popisalerta.app.data.DefaultRoomVisitRepository
import org.popisalerta.app.data.local.AccessDatabase
import org.popisalerta.app.data.local.RoomVisitEntity

@Composable
fun AlertsScreen(
    sensors: Sensors,
    alertSettingsRepository: AlertSettingsRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: AlertsScreenViewModel = viewModel {
        AlertsScreenViewModel(
            roomVisitRepository =
                DefaultRoomVisitRepository(
                    AccessDatabase.getInstance(context).roomVisitDao()
                ),
            alertSettingsRepository = alertSettingsRepository
        )
    }

    val visitsState by viewModel.visitsUiState.collectAsStateWithLifecycle()
    val alertsEnabled by viewModel.alertsEnabled.collectAsStateWithLifecycle()
    var showClearVisitsDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.alerts_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text =
                stringResource(
                    if (alertsEnabled) {
                        R.string.alerts_active
                    } else {
                        R.string.alerts_paused
                    }
                ),
            style = MaterialTheme.typography.titleMedium
        )

        Button(onClick = viewModel::toggleAlerts, modifier = Modifier.fillMaxWidth()) {
            Text(
                text =
                    stringResource(
                        if (alertsEnabled) {
                            R.string.alerts_pause_action
                        } else {
                            R.string.alerts_resume_action
                        }
                    )
            )
        }

        Text(
            text = stringResource(R.string.visits_title),
            style = MaterialTheme.typography.titleLarge
        )

        when (visitsState) {
            AlertsVisitsUiState.Loading -> {
                Text(text = stringResource(R.string.visits_loading))
            }

            is AlertsVisitsUiState.Success -> {
                val visits = (visitsState as AlertsVisitsUiState.Success).visits

                Text(
                    text = stringResource(R.string.visit_count, visits.size),
                    style = MaterialTheme.typography.bodyLarge
                )

                if (visits.isEmpty()) {
                    Text(
                        text = stringResource(R.string.visit_empty),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Button(
                        onClick = { showClearVisitsDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(text = stringResource(R.string.visits_clear_action)) }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(items = visits, key = { visit -> visit.id }) { visit ->
                            RoomVisitRow(visit)
                        }
                    }
                }
            }

            is AlertsVisitsUiState.Error -> {
                val error = (visitsState as AlertsVisitsUiState.Error).throwable.message.orEmpty()

                Text(
                    text = stringResource(R.string.visits_load_error, error),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (showClearVisitsDialog) {
        AlertDialog(
            onDismissRequest = { showClearVisitsDialog = false },
            title = { Text(text = stringResource(R.string.visits_clear_dialog_title)) },
            text = { Text(text = stringResource(R.string.visits_clear_dialog_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearVisits()
                        showClearVisitsDialog = false
                    }
                ) { Text(text = stringResource(R.string.clear_action)) }
            },
            dismissButton = {
                Button(onClick = { showClearVisitsDialog = false }) {
                    Text(text = stringResource(R.string.cancel_action))
                }
            }
        )
    }
}

@Composable
private fun RoomVisitRow(visit: RoomVisitEntity) {
    val context = LocalContext.current
    val date = DateFormat.getMediumDateFormat(context).format(Date(visit.startedAt))
    val time = DateFormat.getTimeFormat(context).format(Date(visit.startedAt))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.visit_detected),
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = "$date · $time", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
