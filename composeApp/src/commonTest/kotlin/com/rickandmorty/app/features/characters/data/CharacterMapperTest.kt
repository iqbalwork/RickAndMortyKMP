package com.rickandmorty.app.features.characters.data

import com.rickandmorty.app.core.database.entity.CharacterFavoriteEntity
import com.rickandmorty.app.features.characters.data.mapper.toDomain
import com.rickandmorty.app.features.characters.data.mapper.toFavoriteEntity
import com.rickandmorty.app.features.characters.data.model.CharacterDto
import com.rickandmorty.app.features.characters.data.model.LocationOriginDto
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.model.CharacterGender
import com.rickandmorty.app.features.characters.domain.model.CharacterStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CharacterMapperTest {

    @Test
    fun characterDto_toDomain_mapsAllFieldsCorrectly() {
        val dto = CharacterDto(
            id = 1,
            name = "Rick Sanchez",
            status = "Alive",
            species = "Human",
            type = "Genius Scientist",
            gender = "Male",
            origin = LocationOriginDto(name = "Earth (C-137)", url = "https://rickandmortyapi.com/api/location/1"),
            location = LocationOriginDto(name = "Citadel of Ricks", url = "https://rickandmortyapi.com/api/location/3"),
            image = "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
            episode = listOf(
                "https://rickandmortyapi.com/api/episode/1",
                "https://rickandmortyapi.com/api/episode/2",
                "https://rickandmortyapi.com/api/episode/3"
            ),
            url = "https://rickandmortyapi.com/api/character/1",
            created = "2017-11-04T18:48:46.250Z"
        )

        val domain = dto.toDomain()

        assertEquals(1, domain.id)
        assertEquals("Rick Sanchez", domain.name)
        assertEquals(CharacterStatus.ALIVE, domain.status)
        assertEquals("Human", domain.species)
        assertEquals("Genius Scientist", domain.type)
        assertEquals(CharacterGender.MALE, domain.gender)
        assertEquals("Earth (C-137)", domain.originName)
        assertEquals("Citadel of Ricks", domain.locationName)
        assertEquals("https://rickandmortyapi.com/api/character/avatar/1.jpeg", domain.imageUrl)
        assertEquals(listOf(1, 2, 3), domain.episodeIds)
    }

    @Test
    fun characterFavoriteEntity_toDomain_mapsFieldsCorrectly() {
        val entity = CharacterFavoriteEntity(
            id = 2,
            name = "Morty Smith",
            status = "Alive",
            species = "Human",
            gender = "Male",
            image = "https://rickandmortyapi.com/api/character/avatar/2.jpeg",
            originName = "Earth (C-137)",
            locationName = "Earth (Replacement Dimension)",
            createdAt = 123456789L
        )

        val domain = entity.toDomain()

        assertEquals(2, domain.id)
        assertEquals("Morty Smith", domain.name)
        assertEquals(CharacterStatus.ALIVE, domain.status)
        assertEquals("Human", domain.species)
        assertEquals(CharacterGender.MALE, domain.gender)
        assertEquals("Earth (C-137)", domain.originName)
        assertEquals("Earth (Replacement Dimension)", domain.locationName)
        assertEquals("https://rickandmortyapi.com/api/character/avatar/2.jpeg", domain.imageUrl)
        assertTrue(domain.episodeIds.isEmpty())
    }

    @Test
    fun character_toFavoriteEntity_mapsFieldsCorrectly() {
        val domain = Character(
            id = 3,
            name = "Summer Smith",
            status = CharacterStatus.ALIVE,
            species = "Human",
            type = "",
            gender = CharacterGender.FEMALE,
            originName = "Earth (Replacement Dimension)",
            locationName = "Earth (Replacement Dimension)",
            imageUrl = "https://rickandmortyapi.com/api/character/avatar/3.jpeg",
            episodeIds = listOf(6, 7, 8)
        )

        val entity = domain.toFavoriteEntity(createdAt = 999999L)

        assertEquals(3, entity.id)
        assertEquals("Summer Smith", entity.name)
        assertEquals("Alive", entity.status)
        assertEquals("Human", entity.species)
        assertEquals("Female", entity.gender)
        assertEquals("https://rickandmortyapi.com/api/character/avatar/3.jpeg", entity.image)
        assertEquals("Earth (Replacement Dimension)", entity.originName)
        assertEquals("Earth (Replacement Dimension)", entity.locationName)
        assertEquals(999999L, entity.createdAt)
    }
}
