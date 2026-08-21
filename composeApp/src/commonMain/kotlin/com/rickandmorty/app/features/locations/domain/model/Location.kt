package com.rickandmorty.app.features.locations.domain.model

data class Location(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    val residentIds: List<Int>
) {
    val residentCount: Int
        get() = residentIds.size
}
