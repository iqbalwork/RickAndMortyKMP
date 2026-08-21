package com.rickandmorty.app.features.locations.domain.repository

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.locations.domain.model.Location
import com.rickandmorty.app.features.locations.domain.model.LocationFilter

interface LocationRepository {
    suspend fun getLocations(
        page: Int = 1,
        filter: LocationFilter? = null
    ): Result<List<Location>, DataError.Network>

    suspend fun getLocation(id: Int): Result<Location, DataError.Network>

    suspend fun getMultipleLocations(ids: List<Int>): Result<List<Location>, DataError.Network>

    suspend fun getResidentsForLocation(residentIds: List<Int>): Result<List<Character>, DataError.Network>
}
