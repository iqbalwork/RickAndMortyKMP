package com.rickandmorty.app.features.locations.presentation.list

import androidx.compose.runtime.Immutable
import com.rickandmorty.app.features.locations.domain.model.Location

@Immutable
data class LocationsState(
    val locations: List<Location> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val endReached: Boolean = false,
    val currentPage: Int = 1,
    val searchQuery: String = "",
    val typeFilter: String? = null,
    val dimensionFilter: String? = null,
    val isFilterSheetVisible: Boolean = false,
    val errorMessage: String? = null
) {
    val isFilterActive: Boolean
        get() = !typeFilter.isNullOrBlank() || !dimensionFilter.isNullOrBlank()

    val isEmpty: Boolean
        get() = !isLoading && locations.isEmpty() && errorMessage == null
}
