package com.rickandmorty.app.features.characters.domain.model

data class CharacterFilter(
    val name: String? = null,
    val status: CharacterStatus? = null,
    val gender: CharacterGender? = null,
    val species: String? = null,
    val type: String? = null
)
