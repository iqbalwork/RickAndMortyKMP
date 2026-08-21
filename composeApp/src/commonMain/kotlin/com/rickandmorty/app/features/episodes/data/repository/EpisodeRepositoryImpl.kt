package com.rickandmorty.app.features.episodes.data.repository

import com.rickandmorty.app.core.database.dao.EpisodeDao
import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.core.util.map
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.repository.CharacterRepository
import com.rickandmorty.app.features.episodes.data.mapper.toDomain
import com.rickandmorty.app.features.episodes.data.mapper.toFavoriteEntity
import com.rickandmorty.app.features.episodes.data.remote.EpisodeApi
import com.rickandmorty.app.features.episodes.domain.model.Episode
import com.rickandmorty.app.features.episodes.domain.model.EpisodeFilter
import com.rickandmorty.app.features.episodes.domain.repository.EpisodeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EpisodeRepositoryImpl(
    private val api: EpisodeApi,
    private val episodeDao: EpisodeDao,
    private val characterRepository: CharacterRepository
) : EpisodeRepository {

    override suspend fun getEpisodes(
        page: Int,
        filter: EpisodeFilter?
    ): Result<List<Episode>, DataError.Network> {
        return api.getEpisodes(
            page = page,
            name = filter?.name,
            episode = filter?.episode
        ).map { response ->
            response.results.map { it.toDomain() }
        }
    }

    override suspend fun getEpisode(id: Int): Result<Episode, DataError.Network> {
        return api.getEpisode(id).map { it.toDomain() }
    }

    override suspend fun getMultipleEpisodes(ids: List<Int>): Result<List<Episode>, DataError.Network> {
        return api.getMultipleEpisodes(ids).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getCharactersForEpisode(characterIds: List<Int>): Result<List<Character>, DataError.Network> {
        if (characterIds.isEmpty()) {
            return Result.Success(emptyList())
        }
        return characterRepository.getMultipleCharacters(characterIds)
    }

    override fun getFavorites(): Flow<List<Episode>> {
        return episodeDao.getAllFavorites().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun isFavorite(id: Int): Flow<Boolean> {
        return episodeDao.isFavorite(id)
    }

    override suspend fun toggleFavorite(episode: Episode) {
        val existing = episodeDao.getById(episode.id)
        if (existing != null) {
            episodeDao.deleteById(episode.id)
        } else {
            episodeDao.insert(episode.toFavoriteEntity())
        }
    }
}
