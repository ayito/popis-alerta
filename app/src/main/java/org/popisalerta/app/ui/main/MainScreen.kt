package org.popisalerta.app.ui.main

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import java.util.Date
import org.popisalerta.app.R
import org.popisalerta.app.data.AlertSettingsRepository
import org.popisalerta.app.data.DefaultAccessRepository
import org.popisalerta.app.data.DefaultBathroomVisitRepository
import org.popisalerta.app.data.local.AccessDatabase
import org.popisalerta.app.data.local.AccessEntity
import org.popisalerta.app.data.local.BathroomVisitEntity
import org.popisalerta.app.theme.PopisAlertaTheme

@Composable
fun MainScreen(onItemClick: (NavKey) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: MainScreenViewModel =
        viewModel {
            MainScreenViewModel(
                accessRepository =
                    DefaultAccessRepository(
                        AccessDatabase.getInstance(context).accessDao()
                    ),
                bathroomVisitRepository =
                    DefaultBathroomVisitRepository(
                        AccessDatabase.getInstance(context).bathroomVisitDao()
                    ),
                alertSettingsRepository =
                    AlertSettingsRepository(context.applicationContext)
            )
        }

    LaunchedEffect(Unit) {
        viewModel.registerMainScreenOpen()
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val visitsState by viewModel.visitsUiState.collectAsStateWithLifecycle()
    val alertsEnabled by viewModel.alertsEnabled.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Popis Alerta",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = if (alertsEnabled) "Avisos activos" else "Avisos pausados",
            style = MaterialTheme.typography.titleMedium
        )

        Button(
            onClick = viewModel::toggleAlerts,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text =
                    if (alertsEnabled) {
                        "Pausar avisos"
                    } else {
                        "Reactivar avisos"
                    }
            )
        }

        // Sección de accesos (tu UI anterior)
        when (state) {
            MainScreenUiState.Loading -> {
                Text(text = "Cargando historial de accesos…")
            }

            is MainScreenUiState.Success -> {
                val accesses = (state as MainScreenUiState.Success).accesses
                Text(
                    text = "Eventos de accesos: ${accesses.size}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Button(
                    onClick = viewModel::registerTestAccess,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag("RegisterTestAccessButton")
                ) {
                    Text("Registrar evento de prueba")
                }

                OutlinedButton(
                    onClick = viewModel::deleteAllAccesses,
                    enabled = accesses.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Borrar historial de accesos")
                }

                if (accesses.isEmpty()) {
                    Text(
                        text = "Todavía no hay eventos de accesos registrados.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = accesses,
                            key = { access -> access.id }
                        ) { access ->
                            AccessRow(access)
                        }
                    }
                }
            }

            is MainScreenUiState.Error -> {
                Text(
                    text =
                        "Error al cargar el historial de accesos: " +
                            (state as MainScreenUiState.Error).throwable.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Sección de visitas al baño
        Text(
            text = "Visitas al baño",
            style = MaterialTheme.typography.titleLarge
        )

        when (visitsState) {
            VisitsUiState.Loading -> {
                Text(text = "Cargando visitas…")
            }

            is VisitsUiState.Success -> {
                val visits = (visitsState as VisitsUiState.Success).visits
                Text(
                    text = "Visitas registradas: ${visits.size}",
                    style = MaterialTheme.typography.bodyLarge
                )

                if (visits.isEmpty()) {
                    Text(
                        text = "Todavía no hay visitas registradas.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = visits,
                            key = { visit -> visit.id }
                        ) { visit ->
                            BathroomVisitRow(visit)
                        }
                    }
                }
            }

            is VisitsUiState.Error -> {
                Text(
                    text =
                        "Error al cargar las visitas: " +
                            (visitsState as VisitsUiState.Error).throwable.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun accessLabel(triggerSource: String): String = when (triggerSource) {
    "APP_OPEN" -> stringResource(R.string.event_app_open)
    "APP_RESUME" -> stringResource(R.string.event_app_resume)
    "MAIN_SCREEN_OPEN" -> stringResource(R.string.event_main_screen_open)
    "TEST" -> stringResource(R.string.event_test)
    else -> stringResource(R.string.event_unknown)
}

@Composable
private fun AccessRow(access: AccessEntity) {
    val date =
        DateFormat
            .getMediumDateFormat(LocalContext.current)
            .format(Date(access.timestamp))
    val time =
        DateFormat
            .getTimeFormat(LocalContext.current)
            .format(Date(access.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = accessLabel(access.triggerSource),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$date · $time",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun BathroomVisitRow(visit: BathroomVisitEntity) {
    val date =
        DateFormat
            .getMediumDateFormat(LocalContext.current)
            .format(Date(visit.startedAt))
    val time =
        DateFormat
            .getTimeFormat(LocalContext.current)
            .format(Date(visit.startedAt))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Visita al baño",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$date · $time",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenEmptyPreview() {
    PopisAlertaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Popis Alerta",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Eventos de accesos: 0",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Visitas al baño",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Todavía no hay visitas registradas.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenWithVisitsPreview() {
    PopisAlertaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Popis Alerta",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Visitas al baño",
                style = MaterialTheme.typography.titleLarge
            )
            BathroomVisitRow(
                BathroomVisitEntity(
                    id = 1,
                    startedAt = 1_770_000_000_000,
                    notified = false
                )
            )
        }
    }
}
