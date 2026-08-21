package com.rickandmorty.app.features.episodes.data.remote

import com.rickandmorty.app.core.network.safeApiCall
import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.core.util.map
import com.rickandmorty.app.features.episodes.data.model.EpisodeDto
import com.rickandmorty.app.features.episodes.data.model.EpisodeResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class EpisodeApi(
    private val httpClient: HttpClient
) {
    suspend fun getEpisodes(
        page: Int = 1,
        name: String? = null,
        episode: String? = null
    ): Result<EpisodeResponseDto, DataError.Network> {
        return safeApiCall {
            httpClient.get("episode") {
                parameter("page", page)
                name?.takeIf { it.isNotBlank() }?.let { parameter("name", it) }
                episode?.takeIf { it.isNotBlank() }?.let { parameter("episode", it) }
            }
        }
    }

    suspend fun getEpisode(id: Int): Result<EpisodeDto, DataError.Network> {
        return safeApiCall {
            httpClient.get("episode/$id")
        }
    }

    suspend fun getMultipleEpisodes(ids: List<Int>): Result<List<EpisodeDto>, DataError.Network> {
        if (ids.isEmpty()) {
            return Result.Success(emptyList())
        }
        if (ids.size == 1) {
            return getEpisode(ids.first()).map { listOf(it) }
        }
        return safeApiCall {
            httpClient.get("episode/${ids.joinToString(",")}")
        }
    }
}
