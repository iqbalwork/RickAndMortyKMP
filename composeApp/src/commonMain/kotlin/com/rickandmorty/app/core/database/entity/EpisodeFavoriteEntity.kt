package com.rickandmorty.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_episodes")
data class EpisodeFavoriteEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val airDate: String,
    val episodeCode: String,
    val createdAt: Long
)
