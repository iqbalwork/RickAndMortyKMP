package com.rickandmorty.app.features.episodes.presentation

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.episodes.domain.model.Episode
import com.rickandmorty.app.features.episodes.domain.model.EpisodeFilter
import com.rickandmorty.app.features.episodes.domain.repository.EpisodeRepository
import com.rickandmorty.app.features.episodes.domain.usecase.GetEpisodesUseCase
import com.rickandmorty.app.features.episodes.domain.usecase.GetFavoriteEpisodesUseCase
import com.rickandmorty.app.features.episodes.domain.usecase.ToggleEpisodeFavoriteUseCase
import com.rickandmorty.app.features.episodes.presentation.list.EpisodesAction
import com.rickandmorty.app.features.episodes.presentation.list.EpisodesViewModel
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

private val testEpisode = Episode(
    id = 1,
    name = "Pilot",
    airDate = "December 2, 2013",
    episodeCode = "S01E01",
    characterIds = listOf(1, 2)
)

private class FakeEpisodeRepository : EpisodeRepository {
    var episodesResult: Result<List<Episode>, DataError.Network> = Result.Success(listOf(testEpisode))
    val favorites = MutableStateFlow<Map<Int, Episode>>(emptyMap())

    override suspend fun getEpisodes(page: Int, filter: EpisodeFilter?): Result<List<Episode>, DataError.Network> {
        return episodesResult
    }

    override suspend fun getEpisode(id: Int): Result<Episode, DataError.Network> {
        return Result.Success(testEpisode)
    }

    override suspend fun getMultipleEpisodes(ids: List<Int>): Result<List<Episode>, DataError.Network> {
        return Result.Success(listOf(testEpisode))
    }

    override suspend fun getCharactersForEpisode(characterIds: List<Int>): Result<List<Character>, DataError.Network> {
        return Result.Success(emptyList())
    }

    override fun getFavorites(): Flow<List<Episode>> {
        return favorites.map { it.values.toList() }
    }

    override fun isFavorite(id: Int): Flow<Boolean> {
        return favorites.map { it.containsKey(id) }
    }

    override suspend fun toggleFavorite(episode: Episode) {
        if (favorites.value.containsKey(episode.id)) {
            favorites.update { it - episode.id }
        } else {
            favorites.update { it + (episode.id to episode) }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class EpisodesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeEpisodeRepository
    private lateinit var getEpisodesUseCase: GetEpisodesUseCase
    private lateinit var toggleFavoriteUseCase: ToggleEpisodeFavoriteUseCase
    private lateinit var getFavoritesUseCase: GetFavoriteEpisodesUseCase

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeEpisodeRepository()
        getEpisodesUseCase = GetEpisodesUseCase(repository)
        toggleFavoriteUseCase = ToggleEpisodeFavoriteUseCase(repository)
        getFavoritesUseCase = GetFavoriteEpisodesUseCase(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsEpisodesSuccessfully() = runTest(testDispatcher) {
        val viewModel = EpisodesViewModel(
            getEpisodesUseCase = getEpisodesUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            getFavoritesUseCase = getFavoritesUseCase
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.episodes.size)
        assertEquals("Pilot", state.episodes.first().name)
    }

    @Test
    fun onSeasonSelect_updatesFilterAndReloads() = runTest(testDispatcher) {
        val viewModel = EpisodesViewModel(
            getEpisodesUseCase = getEpisodesUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            getFavoritesUseCase = getFavoritesUseCase
        )
        advanceUntilIdle()

        viewModel.onAction(EpisodesAction.OnSeasonSelect("S01"))
        advanceUntilIdle()

        assertEquals("S01", viewModel.state.value.selectedSeason)
        assertTrue(viewModel.state.value.isFilterActive)
    }

    @Test
    fun onToggleFavorite_updatesFavoriteIds() = runTest(testDispatcher) {
        val viewModel = EpisodesViewModel(
            getEpisodesUseCase = getEpisodesUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            getFavoritesUseCase = getFavoritesUseCase
        )
        advanceUntilIdle()

        viewModel.onAction(EpisodesAction.OnToggleFavorite(testEpisode))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.favoriteIds.contains(1))

        viewModel.onAction(EpisodesAction.OnToggleFavorite(testEpisode))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.favoriteIds.contains(1))
    }
}
