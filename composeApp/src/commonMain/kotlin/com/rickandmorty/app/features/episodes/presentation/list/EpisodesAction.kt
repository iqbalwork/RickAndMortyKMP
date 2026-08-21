package com.rickandmorty.app.features.episodes.presentation.list

import com.rickandmorty.app.features.episodes.domain.model.Episode

sealed interface EpisodesAction {
    data class OnSearchQueryChange(val query: String) : EpisodesAction
    data class OnSeasonSelect(val season: String?) : EpisodesAction
    data object OnClearFilters : EpisodesAction
    data object OnOpenFilterSheet : EpisodesAction
    data object OnDismissFilterSheet : EpisodesAction
    data object OnLoadNextPage : EpisodesAction
    data object OnRefresh : EpisodesAction
    data class OnToggleFavorite(val episode: Episode) : EpisodesAction
    data class OnEpisodeClick(val episodeId: Int) : EpisodesAction
    data object OnRetry : EpisodesAction
}
