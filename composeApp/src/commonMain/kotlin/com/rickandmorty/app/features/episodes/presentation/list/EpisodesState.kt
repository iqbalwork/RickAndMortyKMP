package com.rickandmorty.app.features.episodes.presentation.list

import androidx.compose.runtime.Immutable
import com.rickandmorty.app.features.episodes.domain.model.Episode

@Immutable
data class EpisodesState(
    val episodes: List<Episode> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val endReached: Boolean = false,
    val currentPage: Int = 1,
    val searchQuery: String = "",
    val selectedSeason: String? = null,
    val isFilterSheetVisible: Boolean = false,
    val errorMessage: String? = null,
    val favoriteIds: Set<Int> = emptySet()
) {
    val isFilterActive: Boolean
        get() = !selectedSeason.isNullOrBlank()

    val isEmpty: Boolean
        get() = !isLoading && episodes.isEmpty() && errorMessage == null
}
