@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.rickandmorty.app.features.characters.data.mapper

import com.rickandmorty.app.core.database.entity.CharacterFavoriteEntity
import com.rickandmorty.app.features.characters.data.model.CharacterDto
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.model.CharacterGender
import com.rickandmorty.app.features.characters.domain.model.CharacterStatus
import kotlin.time.Clock

fun CharacterDto.toDomain(): Character {
    return Character(
        id = id,
        name = name,
        status = CharacterStatus.fromString(status),
        species = species,
        type = type,
        gender = CharacterGender.fromString(gender),
        originName = origin.name,
        locationName = location.name,
        imageUrl = image,
        episodeIds = episode.mapNotNull { it.trimEnd('/').substringAfterLast('/').toIntOrNull() }
    )
}

fun CharacterFavoriteEntity.toDomain(): Character {
    return Character(
        id = id,
        name = name,
        status = CharacterStatus.fromString(status),
        species = species,
        type = "",
        gender = CharacterGender.fromString(gender),
        originName = originName,
        locationName = locationName,
        imageUrl = image,
        episodeIds = emptyList()
    )
}

fun CharacterDto.toFavoriteEntity(
    createdAt: Long = Clock.System.now().toEpochMilliseconds()
): CharacterFavoriteEntity {
    return CharacterFavoriteEntity(
        id = id,
        name = name,
        status = status,
        species = species,
        gender = gender,
        image = image,
        originName = origin.name,
        locationName = location.name,
        createdAt = createdAt
    )
}

fun Character.toFavoriteEntity(
    createdAt: Long = Clock.System.now().toEpochMilliseconds()
): CharacterFavoriteEntity {
    return CharacterFavoriteEntity(
        id = id,
        name = name,
        status = status.displayName,
        species = species,
        gender = gender.displayName,
        image = imageUrl,
        originName = originName,
        locationName = locationName,
        createdAt = createdAt
    )
}

