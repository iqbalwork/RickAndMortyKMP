package com.rickandmorty.app.features.locations.data.mapper

import com.rickandmorty.app.features.locations.data.model.LocationDto
import com.rickandmorty.app.features.locations.domain.model.Location

fun LocationDto.toDomain(): Location {
    return Location(
        id = id,
        name = name,
        type = type,
        dimension = dimension,
        residentIds = residents.mapNotNull { it.trimEnd('/').substringAfterLast('/').toIntOrNull() }
    )
}
