package com.rickandmorty.app.core.database

import app.cash.turbine.test
import com.rickandmorty.app.core.database.dao.CharacterDao
import com.rickandmorty.app.core.database.entity.CharacterFavoriteEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CharacterDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var characterDao: CharacterDao

    @BeforeTest
    fun setUp() {
        database = getInMemoryDatabase()
        characterDao = database.characterDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    private fun createCharacter(
        id: Int = 1,
        name: String = "Rick Sanchez",
        status: String = "Alive",
        species: String = "Human",
        gender: String = "Male",
        image: String = "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
        originName: String = "Earth (C-137)",
        locationName: String = "Citadel of Ricks",
        createdAt: Long = 1000L
    ) = CharacterFavoriteEntity(
        id = id,
        name = name,
        status = status,
        species = species,
        gender = gender,
        image = image,
        originName = originName,
        locationName = locationName,
        createdAt = createdAt
    )

    @Test
    fun insertAndGetById() = runTest {
        val character = createCharacter(id = 1, name = "Rick Sanchez")
        characterDao.insert(character)

        val result = characterDao.getById(1)
        assertNotNull(result)
        assertEquals(1, result.id)
        assertEquals("Rick Sanchez", result.name)
        assertEquals("Alive", result.status)
        assertEquals("Human", result.species)
        assertEquals("Male", result.gender)
        assertEquals("https://rickandmortyapi.com/api/character/avatar/1.jpeg", result.image)
        assertEquals("Earth (C-137)", result.originName)
        assertEquals("Citadel of Ricks", result.locationName)
        assertEquals(1000L, result.createdAt)
    }

    @Test
    fun isFavoriteFlow() = runTest {
        val character = createCharacter(id = 42, name = "Space Beth")

        characterDao.isFavorite(42).test {
            assertFalse(awaitItem())

            characterDao.insert(character)
            assertTrue(awaitItem())

            characterDao.deleteById(42)
            assertFalse(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllFavoritesFlowOrderedByCreatedAtDesc() = runTest {
        val char1 = createCharacter(id = 1, name = "Rick", createdAt = 1000L)
        val char2 = createCharacter(id = 2, name = "Morty", createdAt = 2000L)

        characterDao.getAllFavorites().test {
            assertEquals(emptyList(), awaitItem())

            characterDao.insert(char1)
            val listAfterChar1 = awaitItem()
            assertEquals(1, listAfterChar1.size)
            assertEquals(1, listAfterChar1.first().id)

            characterDao.insert(char2)
            val listAfterChar2 = awaitItem()
            assertEquals(2, listAfterChar2.size)
            assertEquals(2, listAfterChar2[0].id) // Most recent first
            assertEquals(1, listAfterChar2[1].id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteById() = runTest {
        val character = createCharacter(id = 10)
        characterDao.insert(character)
        assertNotNull(characterDao.getById(10))

        characterDao.deleteById(10)
        assertNull(characterDao.getById(10))
    }

    @Test
    fun deleteEntity() = runTest {
        val character = createCharacter(id = 15)
        characterDao.insert(character)
        assertNotNull(characterDao.getById(15))

        characterDao.delete(character)
        assertNull(characterDao.getById(15))
    }

    @Test
    fun clearAll() = runTest {
        characterDao.insert(createCharacter(id = 1))
        characterDao.insert(createCharacter(id = 2))

        characterDao.clearAll()
        assertNull(characterDao.getById(1))
        assertNull(characterDao.getById(2))
    }
}
