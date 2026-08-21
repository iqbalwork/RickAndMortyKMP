package com.rickandmorty.app.features.locations.domain.usecase

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.locations.domain.model.Location
import com.rickandmorty.app.features.locations.domain.repository.LocationRepository

class GetLocationDetailUseCase(
    private val repository: LocationRepository
) {
    suspend fun getLocation(id: Int): Result<Location, DataError.Network> {
        return repository.getLocation(id)
    }

    suspend fun getResidents(residentIds: List<Int>): Result<List<Character>, DataError.Network> {
        return repository.getResidentsForLocation(residentIds)
    }
}
