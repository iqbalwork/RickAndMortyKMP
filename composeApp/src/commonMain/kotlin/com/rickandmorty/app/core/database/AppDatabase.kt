package com.rickandmorty.app.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.rickandmorty.app.core.database.dao.CharacterDao
import com.rickandmorty.app.core.database.dao.EpisodeDao
import com.rickandmorty.app.core.database.entity.CharacterFavoriteEntity
import com.rickandmorty.app.core.database.entity.EpisodeFavoriteEntity

@Database(
    entities = [
        CharacterFavoriteEntity::class,
        EpisodeFavoriteEntity::class
    ],
    version = 1
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun episodeDao(): EpisodeDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
