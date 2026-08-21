package com.rickandmorty.app.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.rickandmorty.app.core.database.AppDatabase
import com.rickandmorty.app.core.database.AppDatabaseConstructor
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val platformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<AppDatabase> {
        val appDataDir = File(System.getProperty("user.home"), ".rickandmorty")
        if (!appDataDir.exists()) {
            appDataDir.mkdirs()
        }
        val dbFile = File(appDataDir, "rick_morty.db")
        Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
            factory = { AppDatabaseConstructor.initialize() }
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
