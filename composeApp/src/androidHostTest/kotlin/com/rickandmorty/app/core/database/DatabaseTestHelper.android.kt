package com.rickandmorty.app.core.database

import android.content.Context
import android.content.ContextWrapper
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual fun getInMemoryDatabase(): AppDatabase {
    val dummyContext = object : ContextWrapper(null) {
        override fun getApplicationContext(): Context = this
        override fun getSystemService(name: String): Any? = null
        override fun getSystemServiceName(serviceClass: Class<*>): String? = null
    }
    return Room.inMemoryDatabaseBuilder<AppDatabase>(
        context = dummyContext,
        factory = { AppDatabaseConstructor.initialize() }
    )
        .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
        .setDriver(BundledSQLiteDriver())
        .build()
}
