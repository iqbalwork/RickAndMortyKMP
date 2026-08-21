package com.rickandmorty.app.features.locations.presentation.list

sealed interface LocationsAction {
    data class OnSearchQueryChange(val query: String) : LocationsAction
    data class OnTypeFilterSelect(val type: String?) : LocationsAction
    data class OnDimensionFilterSelect(val dimension: String?) : LocationsAction
    data object OnClearFilters : LocationsAction
    data object OnOpenFilterSheet : LocationsAction
    data object OnDismissFilterSheet : LocationsAction
    data object OnLoadNextPage : LocationsAction
    data object OnRefresh : LocationsAction
    data class OnLocationClick(val locationId: Int) : LocationsAction
    data object OnRetry : LocationsAction
}
