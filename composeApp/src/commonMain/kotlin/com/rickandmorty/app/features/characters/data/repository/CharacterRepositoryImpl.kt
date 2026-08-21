package com.rickandmorty.app.features.characters.data.repository

import com.rickandmorty.app.core.database.dao.CharacterDao
import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.core.util.map
import com.rickandmorty.app.features.characters.data.mapper.toDomain
import com.rickandmorty.app.features.characters.data.mapper.toFavoriteEntity
import com.rickandmorty.app.features.characters.data.remote.CharacterApi
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.model.CharacterFilter
import com.rickandmorty.app.features.characters.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CharacterRepositoryImpl(
    private val api: CharacterApi,
    private val characterDao: CharacterDao
) : CharacterRepository {

    override suspend fun getCharacters(
        page: Int,
        filter: CharacterFilter?
    ): Result<List<Character>, DataError.Network> {
        return api.getCharacters(
            page = page,
            name = filter?.name,
            status = filter?.status?.name?.lowercase(),
            species = filter?.species,
            type = filter?.type,
            gender = filter?.gender?.name?.lowercase()
        ).map { responseDto ->
            responseDto.results.map { it.toDomain() }
        }
    }

    override suspend fun getCharacter(id: Int): Result<Character, DataError.Network> {
        return api.getCharacter(id).map { it.toDomain() }
    }

    override suspend fun getMultipleCharacters(ids: List<Int>): Result<List<Character>, DataError.Network> {
        return api.getMultipleCharacters(ids).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getFavorites(): Flow<List<Character>> {
        return characterDao.getAllFavorites().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun isFavorite(id: Int): Flow<Boolean> {
        return characterDao.isFavorite(id)
    }

    override suspend fun toggleFavorite(character: Character) {
        val existing = characterDao.getById(character.id)
        if (existing != null) {
            characterDao.deleteById(character.id)
        } else {
            characterDao.insert(character.toFavoriteEntity())
        }
    }
}
