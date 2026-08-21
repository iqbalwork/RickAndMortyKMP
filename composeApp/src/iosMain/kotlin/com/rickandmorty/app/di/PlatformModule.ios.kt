package com.rickandmorty.app.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.rickandmorty.app.core.database.AppDatabase
import com.rickandmorty.app.core.database.AppDatabaseConstructor
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSHomeDirectory

actual val platformModule: Module = module {
    single<HttpClientEngine> {
        Darwin.create {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }
    }
    single<AppDatabase> {
        val dbFilePath = NSHomeDirectory() + "/rick_morty.db"
        Room.databaseBuilder<AppDatabase>(
            name = dbFilePath,
            factory = { AppDatabaseConstructor.initialize() }
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
