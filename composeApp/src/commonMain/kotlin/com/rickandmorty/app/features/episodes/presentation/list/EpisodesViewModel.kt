package com.rickandmorty.app.features.episodes.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.episodes.domain.model.Episode
import com.rickandmorty.app.features.episodes.domain.model.EpisodeFilter
import com.rickandmorty.app.features.episodes.domain.usecase.GetEpisodesUseCase
import com.rickandmorty.app.features.episodes.domain.usecase.GetFavoriteEpisodesUseCase
import com.rickandmorty.app.features.episodes.domain.usecase.ToggleEpisodeFavoriteUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class EpisodesViewModel(
    private val getEpisodesUseCase: GetEpisodesUseCase,
    private val toggleFavoriteUseCase: ToggleEpisodeFavoriteUseCase,
    private val getFavoritesUseCase: GetFavoriteEpisodesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EpisodesState())
    val state: StateFlow<EpisodesState> = _state.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")
    private var loadJob: Job? = null

    init {
        observeFavorites()
        observeSearch()
        loadInitialEpisodes()
    }

    fun onAction(action: EpisodesAction) {
        when (action) {
            is EpisodesAction.OnSearchQueryChange -> {
                _state.update { it.copy(searchQuery = action.query) }
                searchQueryFlow.value = action.query
            }
            is EpisodesAction.OnSeasonSelect -> {
                val newSeason = if (_state.value.selectedSeason == action.season) null else action.season
                _state.update { it.copy(selectedSeason = newSeason) }
                loadInitialEpisodes()
            }
            is EpisodesAction.OnClearFilters -> {
                _state.update { it.copy(selectedSeason = null) }
                loadInitialEpisodes()
            }
            is EpisodesAction.OnOpenFilterSheet -> {
                _state.update { it.copy(isFilterSheetVisible = true) }
            }
            is EpisodesAction.OnDismissFilterSheet -> {
                _state.update { it.copy(isFilterSheetVisible = false) }
            }
            is EpisodesAction.OnLoadNextPage -> {
                loadNextPage()
            }
            is EpisodesAction.OnRefresh -> {
                refresh()
            }
            is EpisodesAction.OnToggleFavorite -> {
                toggleFavorite(action.episode)
            }
            is EpisodesAction.OnRetry -> {
                loadInitialEpisodes()
            }
            is EpisodesAction.OnEpisodeClick -> {
                // Handled in navigation callback
            }
        }
    }

    private fun observeFavorites() {
        getFavoritesUseCase()
            .onEach { favorites ->
                val favIds = favorites.map { it.id }.toSet()
                _state.update { it.copy(favoriteIds = favIds) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSearch() {
        searchQueryFlow
            .debounce(400L)
            .distinctUntilChanged()
            .onEach {
                loadInitialEpisodes()
            }
            .launchIn(viewModelScope)
    }

    private fun currentFilter(): EpisodeFilter? {
        val currentState = _state.value
        val name = currentState.searchQuery.takeIf { it.isNotBlank() }
        val episodeCode = currentState.selectedSeason?.takeIf { it.isNotBlank() }

        return if (name != null || episodeCode != null) {
            EpisodeFilter(
                name = name,
                episode = episodeCode
            )
        } else {
            null
        }
    }

    private fun loadInitialEpisodes() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    currentPage = 1,
                    endReached = false
                )
            }

            when (val result = getEpisodesUseCase(page = 1, filter = currentFilter())) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            episodes = result.data,
                            isLoading = false,
                            errorMessage = null,
                            endReached = result.data.isEmpty()
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            episodes = emptyList(),
                            isLoading = false,
                            errorMessage = formatErrorMessage(result.error)
                        )
                    }
                }
            }
        }
    }

    private fun loadNextPage() {
        val currentState = _state.value
        if (currentState.isLoading || currentState.isLoadingMore || currentState.endReached) {
            return
        }

        viewModelScope.launch {
            val nextPage = currentState.currentPage + 1
            _state.update { it.copy(isLoadingMore = true) }

            when (val result = getEpisodesUseCase(page = nextPage, filter = currentFilter())) {
                is Result.Success -> {
                    val newEpisodes = result.data
                    _state.update {
                        it.copy(
                            episodes = it.episodes + newEpisodes,
                            currentPage = nextPage,
                            isLoadingMore = false,
                            endReached = newEpisodes.isEmpty()
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoadingMore = false,
                            endReached = true
                        )
                    }
                }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, errorMessage = null) }
            when (val result = getEpisodesUseCase(page = 1, filter = currentFilter())) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            episodes = result.data,
                            isRefreshing = false,
                            currentPage = 1,
                            endReached = result.data.isEmpty(),
                            errorMessage = null
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            errorMessage = formatErrorMessage(result.error)
                        )
                    }
                }
            }
        }
    }

    private fun toggleFavorite(episode: Episode) {
        viewModelScope.launch {
            toggleFavoriteUseCase(episode)
        }
    }

    private fun formatErrorMessage(error: DataError.Network): String {
        return when (error) {
            DataError.Network.NO_INTERNET -> "No dimensional connection available."
            DataError.Network.NOT_FOUND -> "No broadcast transmissions found matching query."
            DataError.Network.REQUEST_TIMEOUT -> "Interdimensional broadcast sync timed out."
            DataError.Network.SERVER_ERROR -> "Citadel TV network anomaly."
            DataError.Network.SERIALIZATION -> "Broadcast transmission corrupted."
            DataError.Network.UNKNOWN -> "Unknown transmission anomaly occurred."
        }
    }
}
