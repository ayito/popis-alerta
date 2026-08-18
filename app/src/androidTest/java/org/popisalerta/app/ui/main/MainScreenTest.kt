package org.popisalerta.app.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.rules.ActivityScenarioRule
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

    // Either loading or success state should be present
    composeTestRule.onNodeWithText("Access Logs")
      .assertExists()
  }

  @Test
  fun mainScreen_hasTestAccessButton() {
    composeTestRule.setContent {
      MainScreen(onItemClick = {})
    }

    composeTestRule.onNodeWithContentDescription("Register test access")
      .assertExists()
  }
}
