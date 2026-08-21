package com.rickandmorty.app.features.episodes.domain.repository

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.episodes.domain.model.Episode
import com.rickandmorty.app.features.episodes.domain.model.EpisodeFilter
import kotlinx.coroutines.flow.Flow

interface EpisodeRepository {
    suspend fun getEpisodes(
        page: Int = 1,
        filter: EpisodeFilter? = null
    ): Result<List<Episode>, DataError.Network>

    suspend fun getEpisode(id: Int): Result<Episode, DataError.Network>

    suspend fun getMultipleEpisodes(ids: List<Int>): Result<List<Episode>, DataError.Network>

    suspend fun getCharactersForEpisode(characterIds: List<Int>): Result<List<Character>, DataError.Network>

    fun getFavorites(): Flow<List<Episode>>

    fun isFavorite(id: Int): Flow<Boolean>

    suspend fun toggleFavorite(episode: Episode)
}
