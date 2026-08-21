package com.rickandmorty.app.features.characters.domain.usecase

import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow

class GetFavoriteCharactersUseCase(
    private val repository: CharacterRepository
) {
    operator fun invoke(): Flow<List<Character>> {
        return repository.getFavorites()
    }
}
