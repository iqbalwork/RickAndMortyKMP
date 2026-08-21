package com.rickandmorty.app.features.locations.presentation.detail

import androidx.compose.runtime.Immutable
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.locations.domain.model.Location

@Immutable
data class LocationDetailState(
    val location: Location? = null,
    val residents: List<Character> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingResidents: Boolean = false,
    val errorMessage: String? = null
)
