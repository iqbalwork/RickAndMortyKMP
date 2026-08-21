package com.rickandmorty.app.features.characters.domain.usecase

import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.repository.CharacterRepository

class ToggleCharacterFavoriteUseCase(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(character: Character) {
        repository.toggleFavorite(character)
    }
}
