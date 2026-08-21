package com.rickandmorty.app.features.locations.data

import com.rickandmorty.app.features.locations.data.mapper.toDomain
import com.rickandmorty.app.features.locations.data.model.LocationDto
import kotlin.test.Test
import kotlin.test.assertEquals

class LocationMapperTest {

    @Test
    fun locationDto_toDomain_mapsAllFieldsCorrectly() {
        val dto = LocationDto(
            id = 1,
            name = "Earth (C-137)",
            type = "Planet",
            dimension = "Dimension C-137",
            residents = listOf(
                "https://rickandmortyapi.com/api/character/1",
                "https://rickandmortyapi.com/api/character/2",
                "https://rickandmortyapi.com/api/character/38"
            ),
            url = "https://rickandmortyapi.com/api/location/1",
            created = "2017-11-10T12:42:04.162Z"
        )

        val domain = dto.toDomain()

        assertEquals(1, domain.id)
        assertEquals("Earth (C-137)", domain.name)
        assertEquals("Planet", domain.type)
        assertEquals("Dimension C-137", domain.dimension)
        assertEquals(listOf(1, 2, 38), domain.residentIds)
        assertEquals(3, domain.residentCount)
    }
}
