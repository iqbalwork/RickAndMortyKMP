package com.rickandmorty.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rickandmorty.app.core.database.entity.EpisodeFavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM favorite_episodes ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<EpisodeFavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_episodes WHERE id = :id)")
    fun isFavorite(id: Int): Flow<Boolean>

    @Query("SELECT * FROM favorite_episodes WHERE id = :id")
    suspend fun getById(id: Int): EpisodeFavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: EpisodeFavoriteEntity)

    @Query("DELETE FROM favorite_episodes WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Delete
    suspend fun delete(episode: EpisodeFavoriteEntity)

    @Query("DELETE FROM favorite_episodes")
    suspend fun clearAll()
}
