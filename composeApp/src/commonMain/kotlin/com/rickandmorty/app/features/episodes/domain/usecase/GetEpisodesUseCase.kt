package com.rickandmorty.app.features.episodes.domain.usecase

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.episodes.domain.model.Episode
import com.rickandmorty.app.features.episodes.domain.model.EpisodeFilter
import com.rickandmorty.app.features.episodes.domain.repository.EpisodeRepository

class GetEpisodesUseCase(
    private val repository: EpisodeRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        filter: EpisodeFilter? = null
    ): Result<List<Episode>, DataError.Network> {
        return repository.getEpisodes(page = page, filter = filter)
    }
}
