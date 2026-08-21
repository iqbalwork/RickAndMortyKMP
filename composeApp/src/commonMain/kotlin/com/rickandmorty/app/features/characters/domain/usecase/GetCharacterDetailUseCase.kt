package com.rickandmorty.app.features.characters.domain.usecase

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.repository.CharacterRepository

class GetCharacterDetailUseCase(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(id: Int): Result<Character, DataError.Network> {
        return repository.getCharacter(id)
    }
}
