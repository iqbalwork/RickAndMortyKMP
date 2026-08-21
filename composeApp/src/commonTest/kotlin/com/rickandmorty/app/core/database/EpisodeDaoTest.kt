package com.rickandmorty.app.core.database

import app.cash.turbine.test
import com.rickandmorty.app.core.database.dao.EpisodeDao
import com.rickandmorty.app.core.database.entity.EpisodeFavoriteEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EpisodeDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var episodeDao: EpisodeDao

    @BeforeTest
    fun setUp() {
        database = getInMemoryDatabase()
        episodeDao = database.episodeDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    private fun createEpisode(
        id: Int = 1,
        name: String = "Pilot",
        airDate: String = "December 2, 2013",
        episodeCode: String = "S01E01",
        createdAt: Long = 1000L
    ) = EpisodeFavoriteEntity(
        id = id,
        name = name,
        airDate = airDate,
        episodeCode = episodeCode,
        createdAt = createdAt
    )

    @Test
    fun insertAndGetById() = runTest {
        val episode = createEpisode(id = 1, name = "Pilot", episodeCode = "S01E01")
        episodeDao.insert(episode)

        val result = episodeDao.getById(1)
        assertNotNull(result)
        assertEquals(1, result.id)
        assertEquals("Pilot", result.name)
        assertEquals("December 2, 2013", result.airDate)
        assertEquals("S01E01", result.episodeCode)
        assertEquals(1000L, result.createdAt)
    }

    @Test
    fun isFavoriteFlow() = runTest {
        val episode = createEpisode(id = 10, name = "Close Rick-counters of the Rick Kind")

        episodeDao.isFavorite(10).test {
            assertFalse(awaitItem())

            episodeDao.insert(episode)
            assertTrue(awaitItem())

            episodeDao.deleteById(10)
            assertFalse(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllFavoritesFlowOrderedByCreatedAtDesc() = runTest {
        val ep1 = createEpisode(id = 1, name = "Pilot", createdAt = 1000L)
        val ep2 = createEpisode(id = 2, name = "Lawnmower Dog", createdAt = 2000L)

        episodeDao.getAllFavorites().test {
            assertEquals(emptyList(), awaitItem())

            episodeDao.insert(ep1)
            val listAfterEp1 = awaitItem()
            assertEquals(1, listAfterEp1.size)
            assertEquals(1, listAfterEp1.first().id)

            episodeDao.insert(ep2)
            val listAfterEp2 = awaitItem()
            assertEquals(2, listAfterEp2.size)
            assertEquals(2, listAfterEp2[0].id) // Most recent first
            assertEquals(1, listAfterEp2[1].id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteById() = runTest {
        val episode = createEpisode(id = 5)
        episodeDao.insert(episode)
        assertNotNull(episodeDao.getById(5))

        episodeDao.deleteById(5)
        assertNull(episodeDao.getById(5))
    }

    @Test
    fun deleteEntity() = runTest {
        val episode = createEpisode(id = 8)
        episodeDao.insert(episode)
        assertNotNull(episodeDao.getById(8))

        episodeDao.delete(episode)
        assertNull(episodeDao.getById(8))
    }

    @Test
    fun clearAll() = runTest {
        episodeDao.insert(createEpisode(id = 1))
        episodeDao.insert(createEpisode(id = 2))

        episodeDao.clearAll()
        assertNull(episodeDao.getById(1))
        assertNull(episodeDao.getById(2))
    }
}
