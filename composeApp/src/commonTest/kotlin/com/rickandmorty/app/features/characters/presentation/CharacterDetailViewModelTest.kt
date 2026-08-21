package com.rickandmorty.app.features.characters.presentation

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.model.CharacterFilter
import com.rickandmorty.app.features.characters.domain.model.CharacterGender
import com.rickandmorty.app.features.characters.domain.model.CharacterStatus
import com.rickandmorty.app.features.characters.domain.repository.CharacterRepository
import com.rickandmorty.app.features.characters.domain.usecase.GetCharacterDetailUseCase
import com.rickandmorty.app.features.characters.domain.usecase.IsCharacterFavoriteUseCase
import com.rickandmorty.app.features.characters.domain.usecase.ToggleCharacterFavoriteUseCase
import com.rickandmorty.app.features.characters.presentation.detail.CharacterDetailAction
import com.rickandmorty.app.features.characters.presentation.detail.CharacterDetailViewModel
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val detailTestCharacter = Character(
    id = 1,
    name = "Rick Sanchez",
    status = CharacterStatus.ALIVE,
    species = "Human",
    type = "Scientist",
    gender = CharacterGender.MALE,
    originName = "Earth (C-137)",
    locationName = "Citadel of Ricks",
    imageUrl = "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
    episodeIds = listOf(1, 2, 3)
)

private class FakeDetailCharacterRepository : CharacterRepository {
    var characterDetailResult: Result<Character, DataError.Network> = Result.Success(detailTestCharacter)
    val favorites = MutableStateFlow<Map<Int, Character>>(emptyMap())

    override suspend fun getCharacters(page: Int, filter: CharacterFilter?): Result<List<Character>, DataError.Network> {
        return Result.Success(emptyList())
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
class CharacterDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeDetailCharacterRepository
    private lateinit var getCharacterDetailUseCase: GetCharacterDetailUseCase
    private lateinit var toggleFavoriteUseCase: ToggleCharacterFavoriteUseCase
    private lateinit var isFavoriteUseCase: IsCharacterFavoriteUseCase

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeDetailCharacterRepository()
        getCharacterDetailUseCase = GetCharacterDetailUseCase(repository)
        toggleFavoriteUseCase = ToggleCharacterFavoriteUseCase(repository)
        isFavoriteUseCase = IsCharacterFavoriteUseCase(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsCharacterDetailSuccessfully() = runTest(testDispatcher) {
        val viewModel = CharacterDetailViewModel(
            characterId = 1,
            getCharacterDetailUseCase = getCharacterDetailUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            isFavoriteUseCase = isFavoriteUseCase
        )

        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.character)
        assertEquals("Rick Sanchez", state.character?.name)
        assertNull(state.errorMessage)
    }

    @Test
    fun init_characterNotFound_setsErrorMessage() = runTest(testDispatcher) {
        repository.characterDetailResult = Result.Error(DataError.Network.NOT_FOUND)

        val viewModel = CharacterDetailViewModel(
            characterId = 999,
            getCharacterDetailUseCase = getCharacterDetailUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            isFavoriteUseCase = isFavoriteUseCase
        )

        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.character)
        assertEquals("Character not located in current reality.", state.errorMessage)
    }

    @Test
    fun onToggleFavorite_togglesFavoriteStatus() = runTest(testDispatcher) {
        val viewModel = CharacterDetailViewModel(
            characterId = 1,
            getCharacterDetailUseCase = getCharacterDetailUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            isFavoriteUseCase = isFavoriteUseCase
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isFavorite)

        viewModel.onAction(CharacterDetailAction.OnToggleFavorite)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isFavorite)

        viewModel.onAction(CharacterDetailAction.OnToggleFavorite)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isFavorite)
    }
}
