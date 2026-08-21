package com.rickandmorty.app.features.characters.domain.model

data class Character(
    val id: Int,
    val name: String,
    val status: CharacterStatus,
    val species: String,
    val type: String,
    val gender: CharacterGender,
    val originName: String,
    val locationName: String,
    val imageUrl: String,
    val episodeIds: List<Int>
)
