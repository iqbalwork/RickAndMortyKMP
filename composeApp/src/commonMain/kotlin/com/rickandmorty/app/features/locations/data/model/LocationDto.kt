package com.rickandmorty.app.features.locations.data.model

import com.rickandmorty.app.features.characters.data.model.PageInfoDto
import kotlinx.serialization.Serializable

@Serializable
data class LocationResponseDto(
    val info: PageInfoDto? = null,
    val results: List<LocationDto> = emptyList()
)

@Serializable
data class LocationDto(
    val id: Int,
    val name: String,
    val type: String = "",
    val dimension: String = "",
    val residents: List<String> = emptyList(),
    val url: String = "",
    val created: String = ""
)
