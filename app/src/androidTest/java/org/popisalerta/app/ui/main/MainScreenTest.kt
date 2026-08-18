package org.popisalerta.app.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

/** UI tests for [org.popisalerta.app.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun mainScreen_showsInitialLoadingOrSuccessState() {
    composeTestRule.setContent {
      MainScreen(onItemClick = {})
    }

    // En estado Success, la pantalla muestra "Popis Alerta"
    composeTestRule.onNodeWithText("Popis Alerta")
      .assertExists()
  }

  @Test
  fun mainScreen_hasTestAccessButton() {
    // Probamos directamente el contenido con datos vacíos, sin ViewModel
    composeTestRule.setContent {
      MainScreenContent(
        accesses = emptyList(),
        onRegisterTestAccess = {},
        onDeleteAllAccesses = {},
      )
    }

    composeTestRule
      .onNodeWithTag("RegisterTestAccessButton")
      .assertExists()
  }
}
