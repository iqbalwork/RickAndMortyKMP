package com.rickandmorty.app.features.locations.presentation.detail

sealed interface LocationDetailAction {
    data object OnBackClick : LocationDetailAction
    data object OnRetry : LocationDetailAction
    data class OnResidentClick(val characterId: Int) : LocationDetailAction
}
