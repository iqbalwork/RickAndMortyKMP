package com.rickandmorty.app.features.episodes.data

import app.cash.turbine.test
import com.rickandmorty.app.core.database.dao.EpisodeDao
import com.rickandmorty.app.core.database.entity.EpisodeFavoriteEntity
import com.rickandmorty.app.core.network.HttpClientFactory
import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.model.CharacterFilter
import com.rickandmorty.app.features.characters.domain.model.CharacterGender
import com.rickandmorty.app.features.characters.domain.model.CharacterStatus
import com.rickandmorty.app.features.characters.domain.repository.CharacterRepository
import com.rickandmorty.app.features.episodes.data.remote.EpisodeApi
import com.rickandmorty.app.features.episodes.data.repository.EpisodeRepositoryImpl
import com.rickandmorty.app.features.episodes.domain.model.Episode
import com.rickandmorty.app.features.episodes.domain.model.EpisodeFilter
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private class FakeEpisodeDao : EpisodeDao {
    private val favorites = MutableStateFlow<Map<Int, EpisodeFavoriteEntity>>(emptyMap())

    override fun getAllFavorites(): Flow<List<EpisodeFavoriteEntity>> {
        return favorites.map { it.values.toList() }
    }

    override fun isFavorite(id: Int): Flow<Boolean> {
        return favorites.map { it.containsKey(id) }
    }

    override suspend fun getById(id: Int): EpisodeFavoriteEntity? {
        return favorites.value[id]
    }

    override suspend fun insert(episode: EpisodeFavoriteEntity) {
        favorites.update { it + (episode.id to episode) }
    }

    override suspend fun deleteById(id: Int) {
        favorites.update { it - id }
    }

    override suspend fun delete(episode: EpisodeFavoriteEntity) {
        favorites.update { it - episode.id }
    }

    override suspend fun clearAll() {
        favorites.value = emptyMap()
    }
}

private class FakeCharacterRepoForEpisode : CharacterRepository {
    var sampleCharacter = Character(
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

    override suspend fun getCharacters(page: Int, filter: CharacterFilter?): Result<List<Character>, DataError.Network> =
        Result.Success(listOf(sampleCharacter))

    override suspend fun getCharacter(id: Int): Result<Character, DataError.Network> =
        Result.Success(sampleCharacter)

    override suspend fun getMultipleCharacters(ids: List<Int>): Result<List<Character>, DataError.Network> =
        Result.Success(listOf(sampleCharacter))

    override fun getFavorites(): Flow<List<Character>> = emptyFlow()
    override fun isFavorite(id: Int): Flow<Boolean> = emptyFlow()
    override suspend fun toggleFavorite(character: Character) {}
}

class EpisodeRepositoryTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    @Test
    fun getEpisodes_successfulResponse_returnsMappedEpisodes() = runTest {
        val mockResponse = """
            {
                "info": { "count": 1, "pages": 1, "next": null, "prev": null },
                "results": [
                    {
                        "id": 1,
                        "name": "Pilot",
                        "air_date": "December 2, 2013",
                        "episode": "S01E01",
                        "characters": ["https://rickandmortyapi.com/api/character/1"],
                        "url": "https://rickandmortyapi.com/api/episode/1",
                        "created": "2017-11-10T12:56:33.798Z"
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
        val api = EpisodeApi(client)
        val dao = FakeEpisodeDao()
        val characterRepo = FakeCharacterRepoForEpisode()
        val repository = EpisodeRepositoryImpl(api, dao, characterRepo)

        val result = repository.getEpisodes(page = 1, filter = EpisodeFilter(episode = "S01"))

        assertIs<Result.Success<List<Episode>>>(result)
        assertEquals(1, result.data.size)
        assertEquals("Pilot", result.data.first().name)
        assertEquals("S01E01", result.data.first().episodeCode)
    }

    @Test
    fun toggleFavorite_togglesFavoriteStatusInDao() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(content = "{}", status = HttpStatusCode.OK)
        }
        val client = HttpClientFactory.create(mockEngine, json)
        val api = EpisodeApi(client)
        val dao = FakeEpisodeDao()
        val characterRepo = FakeCharacterRepoForEpisode()
        val repository = EpisodeRepositoryImpl(api, dao, characterRepo)

        val episode = Episode(
            id = 1,
            name = "Pilot",
            airDate = "December 2, 2013",
            episodeCode = "S01E01",
            characterIds = listOf(1)
        )

        repository.isFavorite(1).test {
            assertEquals(false, awaitItem())

            repository.toggleFavorite(episode)
            assertEquals(true, awaitItem())

            repository.toggleFavorite(episode)
            assertEquals(false, awaitItem())
        }
    }
}
