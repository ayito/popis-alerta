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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import java.util.Date
import org.popisalerta.app.data.DefaultAccessRepository
import org.popisalerta.app.data.local.AccessDatabase
import org.popisalerta.app.data.local.AccessEntity
import org.popisalerta.app.theme.PopisAlertaTheme

@Composable
fun MainScreen(onItemClick: (NavKey) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: MainScreenViewModel =
        viewModel {
            MainScreenViewModel(
                DefaultAccessRepository(
                    AccessDatabase.getInstance(context).accessDao()
                )
            )
        }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (state) {
        MainScreenUiState.Loading -> {
            Text(
                text = "Cargando historial…",
                modifier = modifier
            )
        }

        is MainScreenUiState.Success -> {
            MainScreenContent(
                accesses = (state as MainScreenUiState.Success).accesses,
                onRegisterTestAccess = viewModel::registerTestAccess,
                onDeleteAllAccesses = viewModel::deleteAllAccesses,
                modifier = modifier
            )
        }

        is MainScreenUiState.Error -> {
            Text(
                text =
                    "Error al cargar el historial: " +
                        (state as MainScreenUiState.Error).throwable.message,
                modifier = modifier
            )
        }
    }
}

@Composable
internal fun MainScreenContent(
    accesses: List<AccessEntity>,
    onRegisterTestAccess: () -> Unit,
    onDeleteAllAccesses: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Popis Alerta",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Eventos registrados: ${accesses.size}",
            style = MaterialTheme.typography.bodyLarge
        )

        Button(
            onClick = onRegisterTestAccess,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag("RegisterTestAccessButton")
        ) {
            Text("Registrar evento de prueba")
        }

        OutlinedButton(
            onClick = onDeleteAllAccesses,
            enabled = accesses.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Borrar historial")
        }

        if (accesses.isEmpty()) {
            Text(
                text = "Todavía no hay eventos registrados.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                text = access.triggerSource,
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
        MainScreenContent(
            accesses = emptyList(),
            onRegisterTestAccess = {},
            onDeleteAllAccesses = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenWithEventsPreview() {
    PopisAlertaTheme {
        MainScreenContent(
            accesses =
                listOf(
                    AccessEntity(
                        id = 1,
                        timestamp = 1_770_000_000_000,
                        triggerSource = "TEST"
                    )
                ),
            onRegisterTestAccess = {},
            onDeleteAllAccesses = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
