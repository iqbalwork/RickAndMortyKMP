package com.rickandmorty.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object Characters : Screen

    @Serializable
    data class CharacterDetail(val id: Int) : Screen

    @Serializable
    data object Locations : Screen

    @Serializable
    data class LocationDetail(val id: Int) : Screen

    @Serializable
    data object Episodes : Screen

    @Serializable
    data class EpisodeDetail(val id: Int) : Screen

    @Serializable
    data object Favorites : Screen
}

enum class BottomBarDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Screen
) {
    CHARACTERS(
        title = "Characters",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        route = Screen.Characters
    ),
    LOCATIONS(
        title = "Locations",
        selectedIcon = Icons.Filled.Public,
        unselectedIcon = Icons.Outlined.Public,
        route = Screen.Locations
    ),
    EPISODES(
        title = "Episodes",
        selectedIcon = Icons.Filled.Movie,
        unselectedIcon = Icons.Outlined.Movie,
        route = Screen.Episodes
    ),
    FAVORITES(
        title = "Favorites",
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder,
        route = Screen.Favorites
    )
}
