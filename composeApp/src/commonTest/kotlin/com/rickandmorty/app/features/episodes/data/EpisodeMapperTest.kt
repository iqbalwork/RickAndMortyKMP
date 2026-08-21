package com.rickandmorty.app.features.episodes.data

import com.rickandmorty.app.core.database.entity.EpisodeFavoriteEntity
import com.rickandmorty.app.features.episodes.data.mapper.toDomain
import com.rickandmorty.app.features.episodes.data.mapper.toFavoriteEntity
import com.rickandmorty.app.features.episodes.data.model.EpisodeDto
import com.rickandmorty.app.features.episodes.domain.model.Episode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EpisodeMapperTest {

    @Test
    fun episodeDto_toDomain_mapsAllFieldsAndParsesSeason() {
        val dto = EpisodeDto(
            id = 1,
            name = "Pilot",
            airDate = "December 2, 2013",
            episode = "S01E01",
            characters = listOf(
                "https://rickandmortyapi.com/api/character/1",
                "https://rickandmortyapi.com/api/character/2"
            ),
            url = "https://rickandmortyapi.com/api/episode/1",
            created = "2017-11-10T12:56:33.798Z"
        )

        val domain = dto.toDomain()

        assertEquals(1, domain.id)
        assertEquals("Pilot", domain.name)
        assertEquals("December 2, 2013", domain.airDate)
        assertEquals("S01E01", domain.episodeCode)
        assertEquals(listOf(1, 2), domain.characterIds)
        assertEquals(1, domain.seasonNumber)
        assertEquals(1, domain.episodeNumber)
        assertEquals("Season 1 • Episode 1", domain.formattedSeasonEpisode)
    }

    @Test
    fun episodeFavoriteEntity_toDomain_mapsFieldsCorrectly() {
        val entity = EpisodeFavoriteEntity(
            id = 28,
            name = "The Ricklantis Mixup",
            airDate = "September 10, 2017",
            episodeCode = "S03E07",
            createdAt = 123456789L
        )

        val domain = entity.toDomain()

        assertEquals(28, domain.id)
        assertEquals("The Ricklantis Mixup", domain.name)
        assertEquals("September 10, 2017", domain.airDate)
        assertEquals("S03E07", domain.episodeCode)
        assertEquals(3, domain.seasonNumber)
        assertEquals(7, domain.episodeNumber)
        assertTrue(domain.characterIds.isEmpty())
    }

    @Test
    fun episode_toFavoriteEntity_mapsFieldsCorrectly() {
        val domain = Episode(
            id = 28,
            name = "The Ricklantis Mixup",
            airDate = "September 10, 2017",
            episodeCode = "S03E07",
            characterIds = listOf(1, 2, 3)
        )

        val entity = domain.toFavoriteEntity(createdAt = 888888L)

        assertEquals(28, entity.id)
        assertEquals("The Ricklantis Mixup", entity.name)
        assertEquals("September 10, 2017", entity.airDate)
        assertEquals("S03E07", entity.episodeCode)
        assertEquals(888888L, entity.createdAt)
    }
}
