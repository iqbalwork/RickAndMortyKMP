package com.rickandmorty.app.features.characters.domain.model

enum class CharacterGender(val displayName: String) {
    FEMALE("Female"),
    MALE("Male"),
    GENDERLESS("Genderless"),
    UNKNOWN("Unknown");

    companion object {
        fun fromString(gender: String?): CharacterGender {
            return when (gender?.trim()?.lowercase()) {
                "female" -> FEMALE
                "male" -> MALE
                "genderless" -> GENDERLESS
                else -> UNKNOWN
            }
        }
    }
}
