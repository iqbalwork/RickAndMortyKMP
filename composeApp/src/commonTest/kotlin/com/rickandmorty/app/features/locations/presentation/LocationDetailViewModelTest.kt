package com.rickandmorty.app.features.locations.presentation

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.model.CharacterGender
import com.rickandmorty.app.features.characters.domain.model.CharacterStatus
import com.rickandmorty.app.features.locations.domain.model.Location
import com.rickandmorty.app.features.locations.domain.model.LocationFilter
import com.rickandmorty.app.features.locations.domain.repository.LocationRepository
import com.rickandmorty.app.features.locations.domain.usecase.GetLocationDetailUseCase
import com.rickandmorty.app.features.locations.presentation.detail.LocationDetailViewModel
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private val detailTestLocation = Location(
    id = 1,
    name = "Earth (C-137)",
    type = "Planet",
    dimension = "Dimension C-137",
    residentIds = listOf(1)
)

private val residentCharacter = Character(
    id = 1,
    name = "Rick Sanchez",
    status = CharacterStatus.ALIVE,
    species = "Human",
    type = "",
    gender = CharacterGender.MALE,
    originName = "Earth (C-137)",
    locationName = "Earth (C-137)",
    imageUrl = "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
    episodeIds = listOf(1)
)

private class FakeDetailLocationRepository : LocationRepository {
    var locationResult: Result<Location, DataError.Network> = Result.Success(detailTestLocation)
    var residentsResult: Result<List<Character>, DataError.Network> = Result.Success(listOf(residentCharacter))

    override suspend fun getLocations(page: Int, filter: LocationFilter?): Result<List<Location>, DataError.Network> {
        return Result.Success(emptyList())
    }

    override suspend fun getLocation(id: Int): Result<Location, DataError.Network> {
        return locationResult
    }

    override suspend fun getMultipleLocations(ids: List<Int>): Result<List<Location>, DataError.Network> {
        return Result.Success(emptyList())
    }

    override suspend fun getResidentsForLocation(residentIds: List<Int>): Result<List<Character>, DataError.Network> {
        return residentsResult
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LocationDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeDetailLocationRepository
    private lateinit var getLocationDetailUseCase: GetLocationDetailUseCase

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeDetailLocationRepository()
        getLocationDetailUseCase = GetLocationDetailUseCase(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsLocationAndResidentsSuccessfully() = runTest(testDispatcher) {
        val viewModel = LocationDetailViewModel(1, getLocationDetailUseCase)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingResidents)
        assertNotNull(state.location)
        assertEquals("Earth (C-137)", state.location?.name)
        assertEquals(1, state.residents.size)
        assertEquals("Rick Sanchez", state.residents.first().name)
        assertNull(state.errorMessage)
    }

    @Test
    fun init_locationNotFound_setsErrorMessage() = runTest(testDispatcher) {
        repository.locationResult = Result.Error(DataError.Network.NOT_FOUND)

        val viewModel = LocationDetailViewModel(999, getLocationDetailUseCase)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.location)
        assertEquals("Location not found in planetary matrix.", state.errorMessage)
    }
}
