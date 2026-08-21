package com.rickandmorty.app.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NavigationTest {

    @Test
    fun bottomBarDestinations_haveUniqueRoutesAndTitles() {
        val destinations = BottomBarDestination.entries
        assertEquals(4, destinations.size)

        val titles = destinations.map { it.title }
        assertEquals(titles.distinct().size, titles.size)

        val routes = destinations.map { it.route }
        assertEquals(routes.distinct().size, routes.size)
    }

    @Test
    fun screenRoutes_instantiation() {
        val characters = Screen.Characters
        val characterDetail = Screen.CharacterDetail(42)
        val locations = Screen.Locations
        val locationDetail = Screen.LocationDetail(1)
        val episodes = Screen.Episodes
        val episodeDetail = Screen.EpisodeDetail(10)
        val favorites = Screen.Favorites

        assertNotNull(characters)
        assertEquals(42, characterDetail.id)
        assertNotNull(locations)
        assertEquals(1, locationDetail.id)
        assertNotNull(episodes)
        assertEquals(10, episodeDetail.id)
        assertNotNull(favorites)
    }
}
