package com.rickandmorty.app.features.favorites.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.usecase.GetFavoriteCharactersUseCase
import com.rickandmorty.app.features.characters.domain.usecase.ToggleCharacterFavoriteUseCase
import com.rickandmorty.app.features.episodes.domain.model.Episode
import com.rickandmorty.app.features.episodes.domain.usecase.GetFavoriteEpisodesUseCase
import com.rickandmorty.app.features.episodes.domain.usecase.ToggleEpisodeFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val getFavoriteCharactersUseCase: GetFavoriteCharactersUseCase,
    private val toggleCharacterFavoriteUseCase: ToggleCharacterFavoriteUseCase,
    private val getFavoriteEpisodesUseCase: GetFavoriteEpisodesUseCase,
    private val toggleEpisodeFavoriteUseCase: ToggleEpisodeFavoriteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state.asStateFlow()

    init {
        observeFavorites()
    }

    fun onAction(action: FavoritesAction) {
        when (action) {
            is FavoritesAction.OnTabSelect -> {
                _state.update { it.copy(selectedTab = action.tab) }
            }
            is FavoritesAction.OnToggleCharacterFavorite -> {
                toggleCharacterFavorite(action.character)
            }
            is FavoritesAction.OnToggleEpisodeFavorite -> {
                toggleEpisodeFavorite(action.episode)
            }
            is FavoritesAction.OnCharacterClick -> {}
            is FavoritesAction.OnEpisodeClick -> {}
        }
    }

    private fun observeFavorites() {
        combine(
            getFavoriteCharactersUseCase(),
            getFavoriteEpisodesUseCase()
        ) { characters, episodes ->
            _state.update {
                it.copy(
                    favoriteCharacters = characters,
                    favoriteEpisodes = episodes,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun toggleCharacterFavorite(character: Character) {
        viewModelScope.launch {
            toggleCharacterFavoriteUseCase(character)
        }
    }

    private fun toggleEpisodeFavorite(episode: Episode) {
        viewModelScope.launch {
            toggleEpisodeFavoriteUseCase(episode)
        }
    }
}
