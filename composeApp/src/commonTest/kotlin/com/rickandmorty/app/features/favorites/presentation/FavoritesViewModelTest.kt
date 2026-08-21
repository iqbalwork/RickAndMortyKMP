package com.rickandmorty.app.features.favorites.presentation

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.model.CharacterFilter
import com.rickandmorty.app.features.characters.domain.model.CharacterGender
import com.rickandmorty.app.features.characters.domain.model.CharacterStatus
import com.rickandmorty.app.features.characters.domain.repository.CharacterRepository
import com.rickandmorty.app.features.characters.domain.usecase.GetFavoriteCharactersUseCase
import com.rickandmorty.app.features.characters.domain.usecase.ToggleCharacterFavoriteUseCase
import com.rickandmorty.app.features.episodes.domain.model.Episode
import com.rickandmorty.app.features.episodes.domain.model.EpisodeFilter
import com.rickandmorty.app.features.episodes.domain.repository.EpisodeRepository
import com.rickandmorty.app.features.episodes.domain.usecase.GetFavoriteEpisodesUseCase
import com.rickandmorty.app.features.episodes.domain.usecase.ToggleEpisodeFavoriteUseCase
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

private val favTestCharacter = Character(
    id = 1,
    name = "Rick Sanchez",
    status = CharacterStatus.ALIVE,
    species = "Human",
    type = "",
    gender = CharacterGender.MALE,
    originName = "Earth (C-137)",
    locationName = "Citadel of Ricks",
    imageUrl = "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
    episodeIds = listOf(1)
)

private val favTestEpisode = Episode(
    id = 1,
    name = "Pilot",
    airDate = "December 2, 2013",
    episodeCode = "S01E01",
    characterIds = listOf(1)
)

private class FakeCharacterRepoForFav : CharacterRepository {
    val favorites = MutableStateFlow<Map<Int, Character>>(mapOf(1 to favTestCharacter))

    override suspend fun getCharacters(page: Int, filter: CharacterFilter?): Result<List<Character>, DataError.Network> =
        Result.Success(emptyList())

    override suspend fun getCharacter(id: Int): Result<Character, DataError.Network> =
        Result.Success(favTestCharacter)

    override suspend fun getMultipleCharacters(ids: List<Int>): Result<List<Character>, DataError.Network> =
        Result.Success(emptyList())

    override fun getFavorites(): Flow<List<Character>> =
        favorites.map { it.values.toList() }

    override fun isFavorite(id: Int): Flow<Boolean> =
        favorites.map { it.containsKey(id) }

    override suspend fun toggleFavorite(character: Character) {
        if (favorites.value.containsKey(character.id)) {
            favorites.update { it - character.id }
        } else {
            favorites.update { it + (character.id to character) }
        }
    }
}

private class FakeEpisodeRepoForFav : EpisodeRepository {
    val favorites = MutableStateFlow<Map<Int, Episode>>(mapOf(1 to favTestEpisode))

    override suspend fun getEpisodes(page: Int, filter: EpisodeFilter?): Result<List<Episode>, DataError.Network> =
        Result.Success(emptyList())

    override suspend fun getEpisode(id: Int): Result<Episode, DataError.Network> =
        Result.Success(favTestEpisode)

    override suspend fun getMultipleEpisodes(ids: List<Int>): Result<List<Episode>, DataError.Network> =
        Result.Success(emptyList())

    override suspend fun getCharactersForEpisode(characterIds: List<Int>): Result<List<Character>, DataError.Network> =
        Result.Success(emptyList())

    override fun getFavorites(): Flow<List<Episode>> =
        favorites.map { it.values.toList() }

    override fun isFavorite(id: Int): Flow<Boolean> =
        favorites.map { it.containsKey(id) }

    override suspend fun toggleFavorite(episode: Episode) {
        if (favorites.value.containsKey(episode.id)) {
            favorites.update { it - episode.id }
        } else {
            favorites.update { it + (episode.id to episode) }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var characterRepo: FakeCharacterRepoForFav
    private lateinit var episodeRepo: FakeEpisodeRepoForFav
    private lateinit var getFavoriteCharactersUseCase: GetFavoriteCharactersUseCase
    private lateinit var toggleCharacterFavoriteUseCase: ToggleCharacterFavoriteUseCase
    private lateinit var getFavoriteEpisodesUseCase: GetFavoriteEpisodesUseCase
    private lateinit var toggleEpisodeFavoriteUseCase: ToggleEpisodeFavoriteUseCase

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        characterRepo = FakeCharacterRepoForFav()
        episodeRepo = FakeEpisodeRepoForFav()
        getFavoriteCharactersUseCase = GetFavoriteCharactersUseCase(characterRepo)
        toggleCharacterFavoriteUseCase = ToggleCharacterFavoriteUseCase(characterRepo)
        getFavoriteEpisodesUseCase = GetFavoriteEpisodesUseCase(episodeRepo)
        toggleEpisodeFavoriteUseCase = ToggleEpisodeFavoriteUseCase(episodeRepo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_observesBothCharactersAndEpisodes() = runTest(testDispatcher) {
        val viewModel = FavoritesViewModel(
            getFavoriteCharactersUseCase,
            toggleCharacterFavoriteUseCase,
            getFavoriteEpisodesUseCase,
            toggleEpisodeFavoriteUseCase
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.favoriteCharacters.size)
        assertEquals(1, state.favoriteEpisodes.size)
        assertEquals("Rick Sanchez", state.favoriteCharacters.first().name)
        assertEquals("Pilot", state.favoriteEpisodes.first().name)
    }

    @Test
    fun onTabSelect_changesActiveTab() = runTest(testDispatcher) {
        val viewModel = FavoritesViewModel(
            getFavoriteCharactersUseCase,
            toggleCharacterFavoriteUseCase,
            getFavoriteEpisodesUseCase,
            toggleEpisodeFavoriteUseCase
        )
        advanceUntilIdle()

        viewModel.onAction(FavoritesAction.OnTabSelect(FavoritesTab.EPISODES))
        advanceUntilIdle()

        assertEquals(FavoritesTab.EPISODES, viewModel.state.value.selectedTab)
    }

    @Test
    fun onToggleCharacterFavorite_removesCharacter() = runTest(testDispatcher) {
        val viewModel = FavoritesViewModel(
            getFavoriteCharactersUseCase,
            toggleCharacterFavoriteUseCase,
            getFavoriteEpisodesUseCase,
            toggleEpisodeFavoriteUseCase
        )
        advanceUntilIdle()

        viewModel.onAction(FavoritesAction.OnToggleCharacterFavorite(favTestCharacter))
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.favoriteCharacters.size)
    }
}
