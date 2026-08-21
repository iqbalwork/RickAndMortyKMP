package com.rickandmorty.app.di

import com.rickandmorty.app.core.network.HttpClientFactory
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        }
    }
    single { HttpClientFactory.create(engine = get(), json = get()) }
}
