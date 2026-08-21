package com.rickandmorty.app.features.characters.presentation

import app.cash.turbine.test
import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.model.CharacterFilter
import com.rickandmorty.app.features.characters.domain.model.CharacterGender
import com.rickandmorty.app.features.characters.domain.model.CharacterStatus
import com.rickandmorty.app.features.characters.domain.repository.CharacterRepository
import com.rickandmorty.app.features.characters.domain.usecase.GetCharactersUseCase
import com.rickandmorty.app.features.characters.domain.usecase.GetFavoriteCharactersUseCase
import com.rickandmorty.app.features.characters.domain.usecase.ToggleCharacterFavoriteUseCase
import com.rickandmorty.app.features.characters.presentation.list.CharactersAction
import com.rickandmorty.app.features.characters.presentation.list.CharactersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
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

private val testCharacter = Character(
    id = 1,
    name = "Rick Sanchez",
    status = CharacterStatus.ALIVE,
    species = "Human",
    type = "",
    gender = CharacterGender.MALE,
    originName = "Earth (C-137)",
    locationName = "Citadel of Ricks",
    imageUrl = "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
    episodeIds = listOf(1, 2)
)

private class FakeCharacterRepository : CharacterRepository {
    var charactersResult: Result<List<Character>, DataError.Network> = Result.Success(listOf(testCharacter))
    var characterDetailResult: Result<Character, DataError.Network> = Result.Success(testCharacter)
    val favorites = MutableStateFlow<Map<Int, Character>>(emptyMap())

    override suspend fun getCharacters(page: Int, filter: CharacterFilter?): Result<List<Character>, DataError.Network> {
        return charactersResult
    }

    override suspend fun getCharacter(id: Int): Result<Character, DataError.Network> {
        return characterDetailResult
    }

    override suspend fun getMultipleCharacters(ids: List<Int>): Result<List<Character>, DataError.Network> {
        return Result.Success(emptyList())
    }

    override fun getFavorites(): Flow<List<Character>> {
        return favorites.map { it.values.toList() }
    }

    override fun isFavorite(id: Int): Flow<Boolean> {
        return favorites.map { it.containsKey(id) }
    }

    override suspend fun toggleFavorite(character: Character) {
        if (favorites.value.containsKey(character.id)) {
            favorites.update { it - character.id }
        } else {
            favorites.update { it + (character.id to character) }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CharactersViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeCharacterRepository
    private lateinit var getCharactersUseCase: GetCharactersUseCase
    private lateinit var toggleFavoriteUseCase: ToggleCharacterFavoriteUseCase
    private lateinit var getFavoritesUseCase: GetFavoriteCharactersUseCase

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeCharacterRepository()
        getCharactersUseCase = GetCharactersUseCase(repository)
        toggleFavoriteUseCase = ToggleCharacterFavoriteUseCase(repository)
        getFavoritesUseCase = GetFavoriteCharactersUseCase(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsCharactersSuccessfully() = runTest(testDispatcher) {
        val viewModel = CharactersViewModel(
            getCharactersUseCase = getCharactersUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            getFavoritesUseCase = getFavoritesUseCase
        )

        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.characters.size)
        assertEquals("Rick Sanchez", state.characters.first().name)
    }

    @Test
    fun init_networkError_setsErrorMessage() = runTest(testDispatcher) {
        repository.charactersResult = Result.Error(DataError.Network.NO_INTERNET)

        val viewModel = CharactersViewModel(
            getCharactersUseCase = getCharactersUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            getFavoritesUseCase = getFavoritesUseCase
        )

        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.characters.isEmpty())
        assertEquals("No internet connection detected across dimensions.", state.errorMessage)
    }

    @Test
    fun onStatusFilterSelect_updatesFilterAndReloads() = runTest(testDispatcher) {
        val viewModel = CharactersViewModel(
            getCharactersUseCase = getCharactersUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            getFavoritesUseCase = getFavoritesUseCase
        )
        advanceUntilIdle()

        viewModel.onAction(CharactersAction.OnStatusFilterSelect(CharacterStatus.DEAD))
        advanceUntilIdle()

        assertEquals(CharacterStatus.DEAD, viewModel.state.value.selectedStatus)
    }

    @Test
    fun onToggleFavorite_updatesFavoriteIdsInState() = runTest(testDispatcher) {
        val viewModel = CharactersViewModel(
            getCharactersUseCase = getCharactersUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            getFavoritesUseCase = getFavoritesUseCase
        )
        advanceUntilIdle()

        viewModel.onAction(CharactersAction.OnToggleFavorite(testCharacter))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.favoriteIds.contains(1))

        viewModel.onAction(CharactersAction.OnToggleFavorite(testCharacter))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.favoriteIds.contains(1))
    }
}
