package com.rickandmorty.app.features.favorites.presentation

import androidx.compose.runtime.Immutable
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.episodes.domain.model.Episode

@Immutable
data class FavoritesState(
    val selectedTab: FavoritesTab = FavoritesTab.CHARACTERS,
    val favoriteCharacters: List<Character> = emptyList(),
    val favoriteEpisodes: List<Episode> = emptyList(),
    val isLoading: Boolean = true
) {
    val currentCount: Int
        get() = when (selectedTab) {
            FavoritesTab.CHARACTERS -> favoriteCharacters.size
            FavoritesTab.EPISODES -> favoriteEpisodes.size
        }

    val isCurrentTabEmpty: Boolean
        get() = !isLoading && currentCount == 0
}
