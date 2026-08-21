package com.rickandmorty.app.core.network

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.ContentConvertException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

suspend inline fun <reified T> safeApiCall(
    crossinline block: suspend () -> HttpResponse
): Result<T, DataError.Network> {
    val response = try {
        block()
    } catch (e: UnresolvedAddressException) {
        return Result.Error(DataError.Network.NO_INTERNET)
    } catch (e: HttpRequestTimeoutException) {
        return Result.Error(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: ConnectTimeoutException) {
        return Result.Error(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: SocketTimeoutException) {
        return Result.Error(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: SerializationException) {
        return Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: ContentConvertException) {
        return Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: NoTransformationFoundException) {
        return Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        return Result.Error(DataError.Network.UNKNOWN)
    }

    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): Result<T, DataError.Network> {
    return when (response.status.value) {
        in 200..299 -> {
            try {
                val data = response.body<T>()
                Result.Success(data)
            } catch (e: SerializationException) {
                Result.Error(DataError.Network.SERIALIZATION)
            } catch (e: ContentConvertException) {
                Result.Error(DataError.Network.SERIALIZATION)
            } catch (e: NoTransformationFoundException) {
                Result.Error(DataError.Network.SERIALIZATION)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.Error(DataError.Network.UNKNOWN)
            }
        }
        404 -> Result.Error(DataError.Network.NOT_FOUND)
        408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
        in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
        else -> Result.Error(DataError.Network.UNKNOWN)
    }
}
