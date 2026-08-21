package com.rickandmorty.app.features.episodes.domain.usecase

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.episodes.domain.model.Episode
import com.rickandmorty.app.features.episodes.domain.repository.EpisodeRepository

class GetEpisodeDetailUseCase(
    private val repository: EpisodeRepository
) {
    suspend fun getEpisode(id: Int): Result<Episode, DataError.Network> {
        return repository.getEpisode(id)
    }

    suspend fun getCharacters(characterIds: List<Int>): Result<List<Character>, DataError.Network> {
        return repository.getCharactersForEpisode(characterIds)
    }
}
