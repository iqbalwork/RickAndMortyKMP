package com.rickandmorty.app.features.episodes.presentation.detail

import androidx.compose.runtime.Immutable
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.episodes.domain.model.Episode

@Immutable
data class EpisodeDetailState(
    val episode: Episode? = null,
    val characters: List<Character> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val isLoadingCharacters: Boolean = false,
    val errorMessage: String? = null
)
