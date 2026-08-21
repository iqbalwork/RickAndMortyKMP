package com.rickandmorty.app.features.characters.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterResponseDto(
    val info: PageInfoDto? = null,
    val results: List<CharacterDto> = emptyList()
)

@Serializable
data class PageInfoDto(
    val count: Int = 0,
    val pages: Int = 0,
    val next: String? = null,
    val prev: String? = null
)

@Serializable
data class CharacterDto(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String = "",
    val gender: String,
    val origin: LocationOriginDto,
    val location: LocationOriginDto,
    val image: String,
    val episode: List<String> = emptyList(),
    val url: String = "",
    val created: String = ""
)

@Serializable
data class LocationOriginDto(
    val name: String,
    val url: String = ""
)
