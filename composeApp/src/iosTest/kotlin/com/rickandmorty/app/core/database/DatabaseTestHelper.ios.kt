package com.rickandmorty.app.core.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual fun getInMemoryDatabase(): AppDatabase {
    return Room.inMemoryDatabaseBuilder<AppDatabase>()
        .setDriver(BundledSQLiteDriver())
        .build()
}
