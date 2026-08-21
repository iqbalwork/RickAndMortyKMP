package com.rickandmorty.app.di

import com.rickandmorty.app.features.characters.di.characterModule
import com.rickandmorty.app.features.episodes.di.episodeModule
import com.rickandmorty.app.features.favorites.di.favoritesModule
import com.rickandmorty.app.features.locations.di.locationModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class AppModuleTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun featureModules_canBeConfigured() {
        val testNetworkModule = module {
            single { Json { ignoreUnknownKeys = true } }
            single { HttpClient(MockEngine { respond("ok") }) }
        }

        val app = startKoin {
            modules(
                testNetworkModule,
                characterModule,
                locationModule,
                episodeModule,
                favoritesModule
            )
        }

        assertNotNull(app)
    }
}
