package com.rickandmorty.app.features.characters.domain.model

enum class CharacterStatus(val displayName: String) {
    ALIVE("Alive"),
    DEAD("Dead"),
    UNKNOWN("Unknown");

    companion object {
        fun fromString(status: String?): CharacterStatus {
            return when (status?.trim()?.lowercase()) {
                "alive" -> ALIVE
                "dead" -> DEAD
                else -> UNKNOWN
            }
        }
    }
}
