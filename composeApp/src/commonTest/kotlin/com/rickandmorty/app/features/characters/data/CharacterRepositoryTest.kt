package com.rickandmorty.app.features.characters.data

import app.cash.turbine.test
import com.rickandmorty.app.core.database.dao.CharacterDao
import com.rickandmorty.app.core.database.entity.CharacterFavoriteEntity
import com.rickandmorty.app.core.network.HttpClientFactory
import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.data.remote.CharacterApi
import com.rickandmorty.app.features.characters.data.repository.CharacterRepositoryImpl
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.model.CharacterFilter
import com.rickandmorty.app.features.characters.domain.model.CharacterGender
import com.rickandmorty.app.features.characters.domain.model.CharacterStatus
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private class FakeCharacterDao : CharacterDao {
    private val favorites = MutableStateFlow<Map<Int, CharacterFavoriteEntity>>(emptyMap())

    override fun getAllFavorites(): Flow<List<CharacterFavoriteEntity>> {
        return favorites.map { it.values.toList() }
    }

    override fun isFavorite(id: Int): Flow<Boolean> {
        return favorites.map { it.containsKey(id) }
    }

    override suspend fun getById(id: Int): CharacterFavoriteEntity? {
        return favorites.value[id]
    }

    override suspend fun insert(character: CharacterFavoriteEntity) {
        favorites.update { it + (character.id to character) }
    }

    override suspend fun deleteById(id: Int) {
        favorites.update { it - id }
    }

    override suspend fun delete(character: CharacterFavoriteEntity) {
        favorites.update { it - character.id }
    }

    override suspend fun clearAll() {
        favorites.value = emptyMap()
    }
}

class CharacterRepositoryTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    @Test
    fun getCharacters_successfulResponse_returnsMappedCharacters() = runTest {
        val mockResponse = """
            {
                "info": { "count": 1, "pages": 1, "next": null, "prev": null },
                "results": [
                    {
                        "id": 1,
                        "name": "Rick Sanchez",
                        "status": "Alive",
                        "species": "Human",
                        "type": "",
                        "gender": "Male",
                        "origin": { "name": "Earth (C-137)", "url": "" },
                        "location": { "name": "Citadel of Ricks", "url": "" },
                        "image": "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
                        "episode": ["https://rickandmortyapi.com/api/episode/1"],
                        "url": "https://rickandmortyapi.com/api/character/1",
                        "created": "2017-11-04T18:48:46.250Z"
                    }
                ]
            }
        """.trimIndent()

        val mockEngine = MockEngine { _ ->
            respond(
                content = mockResponse,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClientFactory.create(mockEngine, json)
        val api = CharacterApi(client)
        val dao = FakeCharacterDao()
        val repository = CharacterRepositoryImpl(api, dao)

        val result = repository.getCharacters(
            page = 1,
            filter = CharacterFilter(name = "Rick", status = CharacterStatus.ALIVE)
        )

        assertIs<Result.Success<List<Character>>>(result)
        assertEquals(1, result.data.size)
        assertEquals(1, result.data.first().id)
        assertEquals("Rick Sanchez", result.data.first().name)
        assertEquals(CharacterStatus.ALIVE, result.data.first().status)
    }

    @Test
    fun getCharacter_notFound_returnsNotFoundError() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"error": "Character not found"}""",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClientFactory.create(mockEngine, json)
        val api = CharacterApi(client)
        val dao = FakeCharacterDao()
        val repository = CharacterRepositoryImpl(api, dao)

        val result = repository.getCharacter(99999)

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.NOT_FOUND, result.error)
    }

    @Test
    fun toggleFavorite_togglesFavoriteStatusInDao() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = "{}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClientFactory.create(mockEngine, json)
        val api = CharacterApi(client)
        val dao = FakeCharacterDao()
        val repository = CharacterRepositoryImpl(api, dao)

        val character = Character(
            id = 1,
            name = "Rick Sanchez",
            status = CharacterStatus.ALIVE,
            species = "Human",
            type = "",
            gender = CharacterGender.MALE,
            originName = "Earth (C-137)",
            locationName = "Citadel of Ricks",
            imageUrl = "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
            episodeIds = listOf(1)
        )

        repository.isFavorite(1).test {
            assertEquals(false, awaitItem())

            repository.toggleFavorite(character)
            assertEquals(true, awaitItem())

            repository.toggleFavorite(character)
            assertEquals(false, awaitItem())
        }
    }
}
