package com.rickandmorty.app.features.episodes.presentation

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.model.CharacterGender
import com.rickandmorty.app.features.characters.domain.model.CharacterStatus
import com.rickandmorty.app.features.episodes.domain.model.Episode
import com.rickandmorty.app.features.episodes.domain.model.EpisodeFilter
import com.rickandmorty.app.features.episodes.domain.repository.EpisodeRepository
import com.rickandmorty.app.features.episodes.domain.usecase.GetEpisodeDetailUseCase
import com.rickandmorty.app.features.episodes.domain.usecase.IsEpisodeFavoriteUseCase
import com.rickandmorty.app.features.episodes.domain.usecase.ToggleEpisodeFavoriteUseCase
import com.rickandmorty.app.features.episodes.presentation.detail.EpisodeDetailAction
import com.rickandmorty.app.features.episodes.presentation.detail.EpisodeDetailViewModel
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

private val detailTestEpisode = Episode(
    id = 1,
    name = "Pilot",
    airDate = "December 2, 2013",
    episodeCode = "S01E01",
    characterIds = listOf(1)
)

private val castCharacter = Character(
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

private class FakeDetailEpisodeRepository : EpisodeRepository {
    var episodeResult: Result<Episode, DataError.Network> = Result.Success(detailTestEpisode)
    var charactersResult: Result<List<Character>, DataError.Network> = Result.Success(listOf(castCharacter))
    val favorites = MutableStateFlow<Map<Int, Episode>>(emptyMap())

    override suspend fun getEpisodes(page: Int, filter: EpisodeFilter?): Result<List<Episode>, DataError.Network> =
        Result.Success(emptyList())

    override suspend fun getEpisode(id: Int): Result<Episode, DataError.Network> =
        episodeResult

    override suspend fun getMultipleEpisodes(ids: List<Int>): Result<List<Episode>, DataError.Network> =
        Result.Success(emptyList())

    override suspend fun getCharactersForEpisode(characterIds: List<Int>): Result<List<Character>, DataError.Network> =
        charactersResult

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
class EpisodeDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeDetailEpisodeRepository
    private lateinit var getEpisodeDetailUseCase: GetEpisodeDetailUseCase
    private lateinit var toggleFavoriteUseCase: ToggleEpisodeFavoriteUseCase
    private lateinit var isFavoriteUseCase: IsEpisodeFavoriteUseCase

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeDetailEpisodeRepository()
        getEpisodeDetailUseCase = GetEpisodeDetailUseCase(repository)
        toggleFavoriteUseCase = ToggleEpisodeFavoriteUseCase(repository)
        isFavoriteUseCase = IsEpisodeFavoriteUseCase(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsEpisodeAndCharactersSuccessfully() = runTest(testDispatcher) {
        val viewModel = EpisodeDetailViewModel(
            episodeId = 1,
            getEpisodeDetailUseCase = getEpisodeDetailUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            isFavoriteUseCase = isFavoriteUseCase
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingCharacters)
        assertNotNull(state.episode)
        assertEquals("Pilot", state.episode?.name)
        assertEquals(1, state.characters.size)
        assertEquals("Rick Sanchez", state.characters.first().name)
        assertNull(state.errorMessage)
    }

    @Test
    fun onToggleFavorite_togglesFavoriteStatus() = runTest(testDispatcher) {
        val viewModel = EpisodeDetailViewModel(
            episodeId = 1,
            getEpisodeDetailUseCase = getEpisodeDetailUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            isFavoriteUseCase = isFavoriteUseCase
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isFavorite)

        viewModel.onAction(EpisodeDetailAction.OnToggleFavorite)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isFavorite)

        viewModel.onAction(EpisodeDetailAction.OnToggleFavorite)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isFavorite)
    }
}
