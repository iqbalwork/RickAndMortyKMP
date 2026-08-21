package com.rickandmorty.app.features.characters.domain.usecase

import com.rickandmorty.app.features.characters.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow

class IsCharacterFavoriteUseCase(
    private val repository: CharacterRepository
) {
    operator fun invoke(id: Int): Flow<Boolean> {
        return repository.isFavorite(id)
    }
}
