package com.rickandmorty.app.features.episodes.presentation.detail

sealed interface EpisodeDetailAction {
    data object OnBackClick : EpisodeDetailAction
    data object OnToggleFavorite : EpisodeDetailAction
    data object OnRetry : EpisodeDetailAction
    data class OnCharacterClick(val characterId: Int) : EpisodeDetailAction
}
