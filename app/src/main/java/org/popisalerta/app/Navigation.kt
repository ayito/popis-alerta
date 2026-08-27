package org.popisalerta.app

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import org.popisalerta.app.ui.main.MainScreen
import org.popisalerta.app.ui.settings.SettingsScreen

@Composable
fun MainNavigation() {
    var selectedDestination by remember { mutableStateOf(NavigationDestination.MAIN) }

    val backStack = rememberNavBackStack(Main)

    Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                            selected = selectedDestination == NavigationDestination.MAIN,
                            onClick = {
                                selectedDestination = NavigationDestination.MAIN
                                backStack.clear()
                                backStack.add(Main)
                            },
                            icon = {
                                Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Inicio"
                                )
                            },
                            label = { Text(text = "Inicio") }
                    )
                    NavigationBarItem(
                            selected = selectedDestination == NavigationDestination.SETTINGS,
                            onClick = {
                                selectedDestination = NavigationDestination.SETTINGS
                                backStack.clear()
                                backStack.add(Settings)
                            },
                            icon = {
                                Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Configuración"
                                )
                            },
                            label = { Text(text = "Configuración") }
                    )
                }
            }
    ) { paddingValues ->
        NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider =
                        entryProvider {
                            entry<Main> {
                                MainScreen(
                                        onItemClick = { navKey -> backStack.add(navKey) },
                                        modifier =
                                                Modifier.safeDrawingPadding()
                                                        .padding(paddingValues)
                                                        .padding(16.dp)
                                )
                            }
                            entry<Settings> {
                                SettingsScreen(
                                        modifier =
                                                Modifier.safeDrawingPadding()
                                                        .padding(paddingValues)
                                                        .padding(16.dp)
                                )
                            }
                        }
        )
    }
}

enum class NavigationDestination {
    MAIN,
    SETTINGS
}
