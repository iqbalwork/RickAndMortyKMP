package com.rickandmorty.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rickandmorty.app.core.database.entity.CharacterFavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM favorite_characters ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<CharacterFavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_characters WHERE id = :id)")
    fun isFavorite(id: Int): Flow<Boolean>

    @Query("SELECT * FROM favorite_characters WHERE id = :id")
    suspend fun getById(id: Int): CharacterFavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(character: CharacterFavoriteEntity)

    @Query("DELETE FROM favorite_characters WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Delete
    suspend fun delete(character: CharacterFavoriteEntity)

    @Query("DELETE FROM favorite_characters")
    suspend fun clearAll()
}
