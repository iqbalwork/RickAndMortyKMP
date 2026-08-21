package com.rickandmorty.app.features.episodes.domain.usecase

import com.rickandmorty.app.features.episodes.domain.model.Episode
import com.rickandmorty.app.features.episodes.domain.repository.EpisodeRepository
import kotlinx.coroutines.flow.Flow

class GetFavoriteEpisodesUseCase(
    private val repository: EpisodeRepository
) {
    operator fun invoke(): Flow<List<Episode>> {
        return repository.getFavorites()
    }
}
