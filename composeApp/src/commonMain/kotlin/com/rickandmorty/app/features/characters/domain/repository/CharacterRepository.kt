package com.rickandmorty.app.features.characters.domain.repository

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.model.CharacterFilter
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    suspend fun getCharacters(
        page: Int = 1,
        filter: CharacterFilter? = null
    ): Result<List<Character>, DataError.Network>

    suspend fun getCharacter(id: Int): Result<Character, DataError.Network>

    suspend fun getMultipleCharacters(ids: List<Int>): Result<List<Character>, DataError.Network>

    fun getFavorites(): Flow<List<Character>>

    fun isFavorite(id: Int): Flow<Boolean>

    suspend fun toggleFavorite(character: Character)
}
