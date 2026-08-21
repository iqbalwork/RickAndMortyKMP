@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.rickandmorty.app.features.episodes.data.mapper

import com.rickandmorty.app.core.database.entity.EpisodeFavoriteEntity
import com.rickandmorty.app.features.episodes.data.model.EpisodeDto
import com.rickandmorty.app.features.episodes.domain.model.Episode
import kotlin.time.Clock

fun EpisodeDto.toDomain(): Episode {
    return Episode(
        id = id,
        name = name,
        airDate = airDate,
        episodeCode = episode,
        characterIds = characters.mapNotNull { it.trimEnd('/').substringAfterLast('/').toIntOrNull() }
    )
}

fun EpisodeFavoriteEntity.toDomain(): Episode {
    return Episode(
        id = id,
        name = name,
        airDate = airDate,
        episodeCode = episodeCode,
        characterIds = emptyList()
    )
}

fun EpisodeDto.toFavoriteEntity(
    createdAt: Long = Clock.System.now().toEpochMilliseconds()
): EpisodeFavoriteEntity {
    return EpisodeFavoriteEntity(
        id = id,
        name = name,
        airDate = airDate,
        episodeCode = episode,
        createdAt = createdAt
    )
}

fun Episode.toFavoriteEntity(
    createdAt: Long = Clock.System.now().toEpochMilliseconds()
): EpisodeFavoriteEntity {
    return EpisodeFavoriteEntity(
        id = id,
        name = name,
        airDate = airDate,
        episodeCode = episodeCode,
        createdAt = createdAt
    )
}
