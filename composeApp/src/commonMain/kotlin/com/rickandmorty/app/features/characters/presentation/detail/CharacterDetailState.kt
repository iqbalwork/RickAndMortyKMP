package com.rickandmorty.app.features.characters.presentation.detail

import androidx.compose.runtime.Immutable
import com.rickandmorty.app.features.characters.domain.model.Character

@Immutable
data class CharacterDetailState(
    val character: Character? = null,
    val isLoading: Boolean = true,
    val isFavorite: Boolean = false,
    val errorMessage: String? = null
)
