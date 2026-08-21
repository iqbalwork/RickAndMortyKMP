package com.rickandmorty.app.features.episodes.domain.usecase

import com.rickandmorty.app.features.episodes.domain.model.Episode
import com.rickandmorty.app.features.episodes.domain.repository.EpisodeRepository

class ToggleEpisodeFavoriteUseCase(
    private val repository: EpisodeRepository
) {
    suspend operator fun invoke(episode: Episode) {
        repository.toggleFavorite(episode)
    }
}
