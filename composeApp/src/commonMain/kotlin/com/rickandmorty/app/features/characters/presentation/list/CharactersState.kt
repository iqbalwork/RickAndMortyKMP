package com.rickandmorty.app.features.characters.presentation.list

import androidx.compose.runtime.Immutable
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.model.CharacterGender
import com.rickandmorty.app.features.characters.domain.model.CharacterStatus

@Immutable
data class CharactersState(
    val characters: List<Character> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val endReached: Boolean = false,
    val currentPage: Int = 1,
    val searchQuery: String = "",
    val selectedStatus: CharacterStatus? = null,
    val selectedGender: CharacterGender? = null,
    val speciesFilter: String? = null,
    val typeFilter: String? = null,
    val isFilterSheetVisible: Boolean = false,
    val errorMessage: String? = null,
    val favoriteIds: Set<Int> = emptySet()
) {
    val isFilterActive: Boolean
        get() = selectedStatus != null || selectedGender != null || !speciesFilter.isNullOrBlank() || !typeFilter.isNullOrBlank()

    val isEmpty: Boolean
        get() = !isLoading && characters.isEmpty() && errorMessage == null
}
