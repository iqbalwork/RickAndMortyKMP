package com.rickandmorty.app.features.episodes.data.model

import com.rickandmorty.app.features.characters.data.model.PageInfoDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EpisodeResponseDto(
    val info: PageInfoDto? = null,
    val results: List<EpisodeDto> = emptyList()
)

@Serializable
data class EpisodeDto(
    val id: Int,
    val name: String,
    @SerialName("air_date")
    val airDate: String = "",
    val episode: String = "",
    val characters: List<String> = emptyList(),
    val url: String = "",
    val created: String = ""
)
