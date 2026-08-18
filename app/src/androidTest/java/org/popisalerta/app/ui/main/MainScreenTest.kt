package org.popisalerta.app.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** UI tests for [org.popisalerta.app.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun mainScreenContent_showsEmptyState() {
      composeTestRule.setContent {
          MainScreenContent(
              accesses = emptyList(),
              onRegisterTestAccess = {},
              onDeleteAllAccesses = {},
          )
      }

      composeTestRule.onNodeWithText("Popis Alerta")
        .assertExists()
     
      composeTestRule.onNodeWithText("Eventos registrados: 0")
        .assertExists()

      composeTestRule.onNodeWithText("Todavía no hay eventos registrados.")
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

  @Test
  fun mainScreenContent_registerTestAccessButtonInvokesCallback() {
      var registerTestAccessCalls = 0

      composeTestRule.setContent {
          MainScreenContent(
              accesses = emptyList(),
              onRegisterTestAccess = { registerTestAccessCalls++ },
              onDeleteAllAccesses = {},
          )
      }

      composeTestRule
        .onNodeWithText("Todavía no hay eventos registrados.")
        .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("RegisterTestAccessButton")
            .performClick()

        assertEquals(1, registerTestAccessCalls)
    }

}
