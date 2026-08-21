package com.rickandmorty.app.features.characters.presentation.list

import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.model.CharacterGender
import com.rickandmorty.app.features.characters.domain.model.CharacterStatus

sealed interface CharactersAction {
    data class OnSearchQueryChange(val query: String) : CharactersAction
    data class OnStatusFilterSelect(val status: CharacterStatus?) : CharactersAction
    data class OnGenderFilterSelect(val gender: CharacterGender?) : CharactersAction
    data class OnSpeciesFilterChange(val species: String?) : CharactersAction
    data class OnTypeFilterChange(val type: String?) : CharactersAction
    data object OnClearFilters : CharactersAction
    data object OnOpenFilterSheet : CharactersAction
    data object OnDismissFilterSheet : CharactersAction
    data object OnLoadNextPage : CharactersAction
    data object OnRefresh : CharactersAction
    data class OnToggleFavorite(val character: Character) : CharactersAction
    data class OnCharacterClick(val characterId: Int) : CharactersAction
    data object OnRetry : CharactersAction
}
