package com.rickandmorty.app.core.network

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@Serializable
private data class TestCharacter(
    val id: Int,
    val name: String,
    val status: String
)

class HttpClientTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    @Test
    fun safeApiCall_successful200Response_returnsSuccessWithData() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"id":1,"name":"Rick Sanchez","status":"Alive"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClientFactory.create(engine = mockEngine, json = json)

        val result = safeApiCall<TestCharacter> {
            client.get("character/1")
        }

        assertIs<Result.Success<TestCharacter>>(result)
        assertEquals(1, result.data.id)
        assertEquals("Rick Sanchez", result.data.name)
        assertEquals("Alive", result.data.status)
    }

    @Test
    fun safeApiCall_404NotFoundResponse_returnsNotFoundError() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"error":"Character not found"}""",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClientFactory.create(engine = mockEngine, json = json)

        val result = safeApiCall<TestCharacter> {
            client.get("character/9999")
        }

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.NOT_FOUND, result.error)
    }

    @Test
    fun safeApiCall_500ServerErrorResponse_returnsServerError() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }
        val client = HttpClientFactory.create(engine = mockEngine, json = json)

        val result = safeApiCall<TestCharacter> {
            client.get("character/1")
        }

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.SERVER_ERROR, result.error)
    }

    @Test
    fun safeApiCall_malformedJson_returnsSerializationError() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{ "id": "not_an_int", "name": 123 }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClientFactory.create(engine = mockEngine, json = json)

        val result = safeApiCall<TestCharacter> {
            client.get("character/1")
        }

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.SERIALIZATION, result.error)
    }

    @Test
    fun safeApiCall_unresolvedAddressException_returnsNoInternetError() = runTest {
        val mockEngine = MockEngine { _ ->
            throw UnresolvedAddressException()
        }
        val client = HttpClientFactory.create(engine = mockEngine, json = json)

        val result = safeApiCall<TestCharacter> {
            client.get("character/1")
        }

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.NO_INTERNET, result.error)
    }

    @Test
    fun safeApiCall_timeoutException_returnsRequestTimeoutError() = runTest {
        val mockEngine = MockEngine { _ ->
            throw HttpRequestTimeoutException("https://rickandmortyapi.com/api/character/1", 15_000L)
        }
        val client = HttpClientFactory.create(engine = mockEngine, json = json)

        val result = safeApiCall<TestCharacter> {
            client.get("character/1")
        }

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.REQUEST_TIMEOUT, result.error)
    }
}
