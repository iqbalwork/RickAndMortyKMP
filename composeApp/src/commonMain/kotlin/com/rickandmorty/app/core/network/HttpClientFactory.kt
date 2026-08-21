package com.rickandmorty.app.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
    fun create(engine: HttpClientEngine, json: Json): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(json)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000L
                connectTimeoutMillis = 10_000L
                socketTimeoutMillis = 15_000L
            }
            install(Logging) {
                level = LogLevel.INFO
                logger = Logger.DEFAULT
            }
            defaultRequest {
                url("https://rickandmortyapi.com/api/")
                contentType(ContentType.Application.Json)
            }
        }
    }
}
