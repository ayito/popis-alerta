package org.popisalerta.app.ui.main

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.popisalerta.app.data.local.AccessEntity

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial state is Success with empty list`() = runTest {
    val fakeRepo = FakeAccessRepository()
    val viewModel = MainScreenViewModel(fakeRepo)

    val state = viewModel.uiState.first()
    assertTrue(state is MainScreenUiState.Success)
    assertEquals(0, (state as MainScreenUiState.Success).accesses.size)
  }

  @Test
  fun `registerTestAccess adds one event to the list`() = runTest {
    val fakeRepo = FakeAccessRepository()
    val viewModel = MainScreenViewModel(fakeRepo)

    viewModel.registerTestAccess()

    val state = viewModel.uiState.first()
    assertTrue(state is MainScreenUiState.Success)
    val accesses = (state as MainScreenUiState.Success).accesses
    assertEquals(1, accesses.size)
    assertEquals("TEST", accesses.first().triggerSource)
  }

  @Test
  fun `deleteAllAccesses clears the list`() = runTest {
    val fakeRepo = FakeAccessRepository()
    val viewModel = MainScreenViewModel(fakeRepo)

    viewModel.registerTestAccess()
    viewModel.registerTestAccess()
    viewModel.deleteAllAccesses()

    val state = viewModel.uiState.first()
    assertTrue(state is MainScreenUiState.Success)
    assertEquals(0, (state as MainScreenUiState.Success).accesses.size)
  }
}

class FakeAccessRepository : org.popisalerta.app.data.AccessRepository {

  private val _data = MutableStateFlow<List<AccessEntity>>(emptyList())

  override fun observeAll(): Flow<List<AccessEntity>> = _data

  override fun observeSince(startMs: Long): Flow<List<AccessEntity>> = _data

  override suspend fun logTestAccess(): Long {
    val newAccess = AccessEntity(
      id = (_data.value.maxOfOrNull { it.id } ?: 0) + 1,
      timestamp = System.currentTimeMillis(),
      triggerSource = "TEST",
    )
    _data.value = _data.value + newAccess
    return newAccess.id
  }

  override suspend fun deleteAll() {
    _data.value = emptyList()
  }
}
