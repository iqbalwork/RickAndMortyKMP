package com.rickandmorty.app.features.episodes.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.episodes.domain.usecase.GetEpisodeDetailUseCase
import com.rickandmorty.app.features.episodes.domain.usecase.IsEpisodeFavoriteUseCase
import com.rickandmorty.app.features.episodes.domain.usecase.ToggleEpisodeFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EpisodeDetailViewModel(
    private val episodeId: Int,
    private val getEpisodeDetailUseCase: GetEpisodeDetailUseCase,
    private val toggleFavoriteUseCase: ToggleEpisodeFavoriteUseCase,
    private val isFavoriteUseCase: IsEpisodeFavoriteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EpisodeDetailState())
    val state: StateFlow<EpisodeDetailState> = _state.asStateFlow()

    init {
        observeFavoriteStatus()
        loadEpisode()
    }

    fun onAction(action: EpisodeDetailAction) {
        when (action) {
            is EpisodeDetailAction.OnToggleFavorite -> toggleFavorite()
            is EpisodeDetailAction.OnRetry -> loadEpisode()
            is EpisodeDetailAction.OnBackClick -> {}
            is EpisodeDetailAction.OnCharacterClick -> {}
        }
    }

    private fun observeFavoriteStatus() {
        isFavoriteUseCase(episodeId)
            .onEach { isFav ->
                _state.update { it.copy(isFavorite = isFav) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadEpisode() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getEpisodeDetailUseCase.getEpisode(episodeId)) {
                is Result.Success -> {
                    val episode = result.data
                    _state.update {
                        it.copy(
                            episode = episode,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    if (episode.characterIds.isNotEmpty()) {
                        loadCharacters(episode.characterIds)
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = formatErrorMessage(result.error)
                        )
                    }
                }
            }
        }
    }

    private fun loadCharacters(characterIds: List<Int>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingCharacters = true) }
            when (val result = getEpisodeDetailUseCase.getCharacters(characterIds)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            characters = result.data,
                            isLoadingCharacters = false
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoadingCharacters = false) }
                }
            }
        }
    }

    private fun toggleFavorite() {
        val currentEpisode = _state.value.episode ?: return
        viewModelScope.launch {
            toggleFavoriteUseCase(currentEpisode)
        }
    }

    private fun formatErrorMessage(error: DataError.Network): String {
        return when (error) {
            DataError.Network.NO_INTERNET -> "No dimensional connection available."
            DataError.Network.NOT_FOUND -> "Episode broadcast not found."
            DataError.Network.REQUEST_TIMEOUT -> "Signal acquisition timed out."
            DataError.Network.SERVER_ERROR -> "Broadcast station anomaly."
            DataError.Network.SERIALIZATION -> "Signal decoding error."
            DataError.Network.UNKNOWN -> "Unknown signal anomaly."
        }
    }
}
