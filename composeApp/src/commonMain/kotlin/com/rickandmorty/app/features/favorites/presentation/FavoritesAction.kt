package com.rickandmorty.app.features.favorites.presentation

import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.episodes.domain.model.Episode

sealed interface FavoritesAction {
    data class OnTabSelect(val tab: FavoritesTab) : FavoritesAction
    data class OnToggleCharacterFavorite(val character: Character) : FavoritesAction
    data class OnToggleEpisodeFavorite(val episode: Episode) : FavoritesAction
    data class OnCharacterClick(val characterId: Int) : FavoritesAction
    data class OnEpisodeClick(val episodeId: Int) : FavoritesAction
}
