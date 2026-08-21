package com.rickandmorty.app.features.locations.data.remote

import com.rickandmorty.app.core.network.safeApiCall
import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.core.util.map
import com.rickandmorty.app.features.locations.data.model.LocationDto
import com.rickandmorty.app.features.locations.data.model.LocationResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class LocationApi(
    private val httpClient: HttpClient
) {
    suspend fun getLocations(
        page: Int = 1,
        name: String? = null,
        type: String? = null,
        dimension: String? = null
    ): Result<LocationResponseDto, DataError.Network> {
        return safeApiCall {
            httpClient.get("location") {
                parameter("page", page)
                name?.takeIf { it.isNotBlank() }?.let { parameter("name", it) }
                type?.takeIf { it.isNotBlank() }?.let { parameter("type", it) }
                dimension?.takeIf { it.isNotBlank() }?.let { parameter("dimension", it) }
            }
        }
    }

    suspend fun getLocation(id: Int): Result<LocationDto, DataError.Network> {
        return safeApiCall {
            httpClient.get("location/$id")
        }
    }

    suspend fun getMultipleLocations(ids: List<Int>): Result<List<LocationDto>, DataError.Network> {
        if (ids.isEmpty()) {
            return Result.Success(emptyList())
        }
        if (ids.size == 1) {
            return getLocation(ids.first()).map { listOf(it) }
        }
        return safeApiCall {
            httpClient.get("location/${ids.joinToString(",")}")
        }
    }
}
