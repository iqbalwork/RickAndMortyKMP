package com.rickandmorty.app.features.locations.data.repository

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.core.util.map
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.repository.CharacterRepository
import com.rickandmorty.app.features.locations.data.mapper.toDomain
import com.rickandmorty.app.features.locations.data.remote.LocationApi
import com.rickandmorty.app.features.locations.domain.model.Location
import com.rickandmorty.app.features.locations.domain.model.LocationFilter
import com.rickandmorty.app.features.locations.domain.repository.LocationRepository

class LocationRepositoryImpl(
    private val api: LocationApi,
    private val characterRepository: CharacterRepository
) : LocationRepository {

    override suspend fun getLocations(
        page: Int,
        filter: LocationFilter?
    ): Result<List<Location>, DataError.Network> {
        return api.getLocations(
            page = page,
            name = filter?.name,
            type = filter?.type,
            dimension = filter?.dimension
        ).map { response ->
            response.results.map { it.toDomain() }
        }
    }

    override suspend fun getLocation(id: Int): Result<Location, DataError.Network> {
        return api.getLocation(id).map { it.toDomain() }
    }

    override suspend fun getMultipleLocations(ids: List<Int>): Result<List<Location>, DataError.Network> {
        return api.getMultipleLocations(ids).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getResidentsForLocation(residentIds: List<Int>): Result<List<Character>, DataError.Network> {
        if (residentIds.isEmpty()) {
            return Result.Success(emptyList())
        }
        return characterRepository.getMultipleCharacters(residentIds)
    }
}
