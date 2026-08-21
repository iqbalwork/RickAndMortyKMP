package com.rickandmorty.app.features.locations.data

import com.rickandmorty.app.core.network.HttpClientFactory
import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.model.CharacterFilter
import com.rickandmorty.app.features.characters.domain.model.CharacterGender
import com.rickandmorty.app.features.characters.domain.model.CharacterStatus
import com.rickandmorty.app.features.characters.domain.repository.CharacterRepository
import com.rickandmorty.app.features.locations.data.remote.LocationApi
import com.rickandmorty.app.features.locations.data.repository.LocationRepositoryImpl
import com.rickandmorty.app.features.locations.domain.model.Location
import com.rickandmorty.app.features.locations.domain.model.LocationFilter
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private class FakeCharacterRepoForLocation : CharacterRepository {
    var charactersToReturn = listOf(
        Character(
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
    )

    override suspend fun getCharacters(page: Int, filter: CharacterFilter?): Result<List<Character>, DataError.Network> =
        Result.Success(charactersToReturn)

    override suspend fun getCharacter(id: Int): Result<Character, DataError.Network> =
        Result.Success(charactersToReturn.first())

    override suspend fun getMultipleCharacters(ids: List<Int>): Result<List<Character>, DataError.Network> =
        Result.Success(charactersToReturn.filter { ids.contains(it.id) })

    override fun getFavorites(): Flow<List<Character>> = emptyFlow()
    override fun isFavorite(id: Int): Flow<Boolean> = emptyFlow()
    override suspend fun toggleFavorite(character: Character) {}
}

class LocationRepositoryTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    @Test
    fun getLocations_successfulResponse_returnsMappedLocations() = runTest {
        val mockResponse = """
            {
                "info": { "count": 1, "pages": 1, "next": null, "prev": null },
                "results": [
                    {
                        "id": 1,
                        "name": "Earth (C-137)",
                        "type": "Planet",
                        "dimension": "Dimension C-137",
                        "residents": ["https://rickandmortyapi.com/api/character/1"],
                        "url": "https://rickandmortyapi.com/api/location/1",
                        "created": "2017-11-10T12:42:04.162Z"
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
        val api = LocationApi(client)
        val characterRepo = FakeCharacterRepoForLocation()
        val repository = LocationRepositoryImpl(api, characterRepo)

        val result = repository.getLocations(page = 1, filter = LocationFilter(type = "Planet"))

        assertIs<Result.Success<List<Location>>>(result)
        assertEquals(1, result.data.size)
        assertEquals("Earth (C-137)", result.data.first().name)
        assertEquals("Planet", result.data.first().type)
        assertEquals(listOf(1), result.data.first().residentIds)
    }

    @Test
    fun getResidentsForLocation_delegatesToCharacterRepo() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(content = "{}", status = HttpStatusCode.OK)
        }
        val client = HttpClientFactory.create(mockEngine, json)
        val api = LocationApi(client)
        val characterRepo = FakeCharacterRepoForLocation()
        val repository = LocationRepositoryImpl(api, characterRepo)

        val result = repository.getResidentsForLocation(listOf(1))

        assertIs<Result.Success<List<Character>>>(result)
        assertEquals(1, result.data.size)
        assertEquals("Rick Sanchez", result.data.first().name)
    }
}
