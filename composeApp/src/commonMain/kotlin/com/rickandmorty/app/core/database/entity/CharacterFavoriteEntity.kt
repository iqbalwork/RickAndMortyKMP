package com.rickandmorty.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_characters")
data class CharacterFavoriteEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val image: String,
    val originName: String,
    val locationName: String,
    val createdAt: Long
)
