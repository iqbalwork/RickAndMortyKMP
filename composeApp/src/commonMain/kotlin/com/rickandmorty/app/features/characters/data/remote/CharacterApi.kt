package com.rickandmorty.app.features.characters.data.remote

import com.rickandmorty.app.core.network.safeApiCall
import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.core.util.map
import com.rickandmorty.app.features.characters.data.model.CharacterDto
import com.rickandmorty.app.features.characters.data.model.CharacterResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class CharacterApi(
    private val httpClient: HttpClient
) {
    suspend fun getCharacters(
        page: Int = 1,
        name: String? = null,
        status: String? = null,
        species: String? = null,
        type: String? = null,
        gender: String? = null
    ): Result<CharacterResponseDto, DataError.Network> {
        return safeApiCall {
            httpClient.get("character") {
                parameter("page", page)
                name?.takeIf { it.isNotBlank() }?.let { parameter("name", it) }
                status?.takeIf { it.isNotBlank() }?.let { parameter("status", it) }
                species?.takeIf { it.isNotBlank() }?.let { parameter("species", it) }
                type?.takeIf { it.isNotBlank() }?.let { parameter("type", it) }
                gender?.takeIf { it.isNotBlank() }?.let { parameter("gender", it) }
            }
        }
    }

    suspend fun getCharacter(id: Int): Result<CharacterDto, DataError.Network> {
        return safeApiCall {
            httpClient.get("character/$id")
        }
    }

    suspend fun getMultipleCharacters(ids: List<Int>): Result<List<CharacterDto>, DataError.Network> {
        if (ids.isEmpty()) {
            return Result.Success(emptyList())
        }
        if (ids.size == 1) {
            return getCharacter(ids.first()).map { listOf(it) }
        }
        return safeApiCall {
            httpClient.get("character/${ids.joinToString(",")}")
        }
    }
}
