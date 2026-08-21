package com.rickandmorty.app.features.locations.presentation

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.locations.domain.model.Location
import com.rickandmorty.app.features.locations.domain.model.LocationFilter
import com.rickandmorty.app.features.locations.domain.repository.LocationRepository
import com.rickandmorty.app.features.locations.domain.usecase.GetLocationsUseCase
import com.rickandmorty.app.features.locations.presentation.list.LocationsAction
import com.rickandmorty.app.features.locations.presentation.list.LocationsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val testLocation = Location(
    id = 1,
    name = "Earth (C-137)",
    type = "Planet",
    dimension = "Dimension C-137",
    residentIds = listOf(1, 2)
)

private class FakeLocationRepository : LocationRepository {
    var locationsResult: Result<List<Location>, DataError.Network> = Result.Success(listOf(testLocation))

    override suspend fun getLocations(page: Int, filter: LocationFilter?): Result<List<Location>, DataError.Network> {
        return locationsResult
    }

    override suspend fun getLocation(id: Int): Result<Location, DataError.Network> {
        return Result.Success(testLocation)
    }

    override suspend fun getMultipleLocations(ids: List<Int>): Result<List<Location>, DataError.Network> {
        return Result.Success(listOf(testLocation))
    }

    override suspend fun getResidentsForLocation(residentIds: List<Int>): Result<List<Character>, DataError.Network> {
        return Result.Success(emptyList())
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LocationsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeLocationRepository
    private lateinit var getLocationsUseCase: GetLocationsUseCase

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeLocationRepository()
        getLocationsUseCase = GetLocationsUseCase(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsLocationsSuccessfully() = runTest(testDispatcher) {
        val viewModel = LocationsViewModel(getLocationsUseCase)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.locations.size)
        assertEquals("Earth (C-137)", state.locations.first().name)
    }

    @Test
    fun onTypeFilterSelect_updatesFilterState() = runTest(testDispatcher) {
        val viewModel = LocationsViewModel(getLocationsUseCase)
        advanceUntilIdle()

        viewModel.onAction(LocationsAction.OnTypeFilterSelect("Planet"))
        advanceUntilIdle()

        assertEquals("Planet", viewModel.state.value.typeFilter)
        assertTrue(viewModel.state.value.isFilterActive)
    }

    @Test
    fun onClearFilters_resetsAllFilters() = runTest(testDispatcher) {
        val viewModel = LocationsViewModel(getLocationsUseCase)
        advanceUntilIdle()

        viewModel.onAction(LocationsAction.OnTypeFilterSelect("Planet"))
        viewModel.onAction(LocationsAction.OnDimensionFilterSelect("Dimension C-137"))
        advanceUntilIdle()

        viewModel.onAction(LocationsAction.OnClearFilters)
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.typeFilter)
        assertEquals(null, viewModel.state.value.dimensionFilter)
        assertFalse(viewModel.state.value.isFilterActive)
    }
}
