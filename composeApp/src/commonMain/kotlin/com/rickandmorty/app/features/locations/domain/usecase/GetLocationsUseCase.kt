package com.rickandmorty.app.features.locations.domain.usecase

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.locations.domain.model.Location
import com.rickandmorty.app.features.locations.domain.model.LocationFilter
import com.rickandmorty.app.features.locations.domain.repository.LocationRepository

class GetLocationsUseCase(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        filter: LocationFilter? = null
    ): Result<List<Location>, DataError.Network> {
        return repository.getLocations(page = page, filter = filter)
    }
}
