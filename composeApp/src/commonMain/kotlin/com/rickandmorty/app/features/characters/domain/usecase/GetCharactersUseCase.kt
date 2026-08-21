package com.rickandmorty.app.features.characters.domain.usecase

import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.model.CharacterFilter
import com.rickandmorty.app.features.characters.domain.repository.CharacterRepository

class GetCharactersUseCase(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        filter: CharacterFilter? = null
    ): Result<List<Character>, DataError.Network> {
        return repository.getCharacters(page = page, filter = filter)
    }
}
