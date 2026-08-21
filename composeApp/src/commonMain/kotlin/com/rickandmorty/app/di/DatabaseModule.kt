package com.rickandmorty.app.di

import com.rickandmorty.app.core.database.AppDatabase
import com.rickandmorty.app.core.database.dao.CharacterDao
import com.rickandmorty.app.core.database.dao.EpisodeDao
import org.koin.dsl.module

val databaseModule = module {
    single<CharacterDao> { get<AppDatabase>().characterDao() }
    single<EpisodeDao> { get<AppDatabase>().episodeDao() }
}
