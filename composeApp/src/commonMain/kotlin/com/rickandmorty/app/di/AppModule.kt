package com.rickandmorty.app.di

import com.rickandmorty.app.features.characters.di.characterModule
import com.rickandmorty.app.features.episodes.di.episodeModule
import com.rickandmorty.app.features.favorites.di.favoritesModule
import com.rickandmorty.app.features.locations.di.locationModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

val appModules: List<Module> = listOf(
    platformModule,
    networkModule,
    databaseModule,
    characterModule,
    locationModule,
    episodeModule,
    favoritesModule
)

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = initializeKoin(appDeclaration)

