package com.rickandmorty.app.features.episodes.domain.usecase

import com.rickandmorty.app.features.episodes.domain.repository.EpisodeRepository
import kotlinx.coroutines.flow.Flow

class IsEpisodeFavoriteUseCase(
    private val repository: EpisodeRepository
) {
    operator fun invoke(id: Int): Flow<Boolean> {
        return repository.isFavorite(id)
    }
}
