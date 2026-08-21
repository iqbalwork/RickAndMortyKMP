package com.rickandmorty.app.features.characters.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.usecase.GetCharacterDetailUseCase
import com.rickandmorty.app.features.characters.domain.usecase.IsCharacterFavoriteUseCase
import com.rickandmorty.app.features.characters.domain.usecase.ToggleCharacterFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CharacterDetailViewModel(
    private val characterId: Int,
    private val getCharacterDetailUseCase: GetCharacterDetailUseCase,
    private val toggleFavoriteUseCase: ToggleCharacterFavoriteUseCase,
    private val isFavoriteUseCase: IsCharacterFavoriteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CharacterDetailState())
    val state: StateFlow<CharacterDetailState> = _state.asStateFlow()

    init {
        observeFavoriteStatus()
        loadCharacter()
    }

    fun onAction(action: CharacterDetailAction) {
        when (action) {
            is CharacterDetailAction.OnToggleFavorite -> toggleFavorite()
            is CharacterDetailAction.OnRetry -> loadCharacter()
            is CharacterDetailAction.OnBackClick -> {}
            is CharacterDetailAction.OnEpisodeClick -> {}
        }
    }

    private fun observeFavoriteStatus() {
        isFavoriteUseCase(characterId)
            .onEach { isFav ->
                _state.update { it.copy(isFavorite = isFav) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadCharacter() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getCharacterDetailUseCase(characterId)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            character = result.data,
                            isLoading = false,
                            errorMessage = null
                        )
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

    private fun toggleFavorite() {
        val currentCharacter = _state.value.character ?: return
        viewModelScope.launch {
            toggleFavoriteUseCase(currentCharacter)
        }
    }

    private fun formatErrorMessage(error: DataError.Network): String {
        return when (error) {
            DataError.Network.NO_INTERNET -> "No dimensional connection available."
            DataError.Network.NOT_FOUND -> "Character not located in current reality."
            DataError.Network.REQUEST_TIMEOUT -> "Portal sync timed out."
            DataError.Network.SERVER_ERROR -> "Citadel server anomaly."
            DataError.Network.SERIALIZATION -> "Entity matrix corrupted."
            DataError.Network.UNKNOWN -> "Unknown spatial anomaly occurred."
        }
    }
}
