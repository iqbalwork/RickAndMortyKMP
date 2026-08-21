package com.rickandmorty.app.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.rickandmorty.app.features.characters.presentation.detail.CharacterDetailScreen
import com.rickandmorty.app.features.characters.presentation.detail.CharacterDetailViewModel
import com.rickandmorty.app.features.characters.presentation.list.CharactersScreen
import com.rickandmorty.app.features.characters.presentation.list.CharactersViewModel
import com.rickandmorty.app.features.episodes.presentation.detail.EpisodeDetailScreen
import com.rickandmorty.app.features.episodes.presentation.detail.EpisodeDetailViewModel
import com.rickandmorty.app.features.episodes.presentation.list.EpisodesScreen
import com.rickandmorty.app.features.episodes.presentation.list.EpisodesViewModel
import com.rickandmorty.app.features.favorites.presentation.FavoritesScreen
import com.rickandmorty.app.features.favorites.presentation.FavoritesViewModel
import com.rickandmorty.app.features.locations.presentation.detail.LocationDetailScreen
import com.rickandmorty.app.features.locations.presentation.detail.LocationDetailViewModel
import com.rickandmorty.app.features.locations.presentation.list.LocationsScreen
import com.rickandmorty.app.features.locations.presentation.list.LocationsViewModel
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalSerializationApi::class)
private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<Screen>()
        }
    }
}

@Composable
fun RickMortyNavHost(
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(navConfig, Screen.Characters)
    val currentScreen = backStack.lastOrNull()

    val currentDestination = when (currentScreen) {
        is Screen.Characters -> BottomBarDestination.CHARACTERS
        is Screen.Locations -> BottomBarDestination.LOCATIONS
        is Screen.Episodes -> BottomBarDestination.EPISODES
        is Screen.Favorites -> BottomBarDestination.FAVORITES
        else -> null
    }

    val isTopLevelDestination = currentDestination != null

    Scaffold(
        bottomBar = {
            if (isTopLevelDestination) {
                RickMortyNavigationBar(
                    currentDestination = currentDestination,
                    onNavigateToDestination = { destination ->
                        if (currentScreen != destination.route) {
                            backStack.clear()
                            backStack.add(destination.route)
                        }
                    }
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            entryProvider = remember {
                entryProvider {
                    entry<Screen.Characters> {
                        val viewModel = koinViewModel<CharactersViewModel>()
                        CharactersScreen(
                            viewModel = viewModel,
                            onCharacterClick = { characterId ->
                                backStack.add(Screen.CharacterDetail(characterId))
                            }
                        )
                    }

                    entry<Screen.CharacterDetail> { key ->
                        val viewModel = koinViewModel<CharacterDetailViewModel>(
                            key = "character_detail_${key.id}",
                            parameters = { parametersOf(key.id) }
                        )
                        CharacterDetailScreen(
                            viewModel = viewModel,
                            onBackClick = { if (backStack.size > 1) backStack.removeLast() },
                            onEpisodeClick = { episodeId ->
                                backStack.add(Screen.EpisodeDetail(episodeId))
                            }
                        )
                    }

                    entry<Screen.Locations> {
                        val viewModel = koinViewModel<LocationsViewModel>()
                        LocationsScreen(
                            viewModel = viewModel,
                            onLocationClick = { locationId ->
                                backStack.add(Screen.LocationDetail(locationId))
                            }
                        )
                    }

                    entry<Screen.LocationDetail> { key ->
                        val viewModel = koinViewModel<LocationDetailViewModel>(
                            key = "location_detail_${key.id}",
                            parameters = { parametersOf(key.id) }
                        )
                        LocationDetailScreen(
                            viewModel = viewModel,
                            onBackClick = { if (backStack.size > 1) backStack.removeLast() },
                            onResidentClick = { residentId ->
                                backStack.add(Screen.CharacterDetail(residentId))
                            }
                        )
                    }

                    entry<Screen.Episodes> {
                        val viewModel = koinViewModel<EpisodesViewModel>()
                        EpisodesScreen(
                            viewModel = viewModel,
                            onEpisodeClick = { episodeId ->
                                backStack.add(Screen.EpisodeDetail(episodeId))
                            }
                        )
                    }

                    entry<Screen.EpisodeDetail> { key ->
                        val viewModel = koinViewModel<EpisodeDetailViewModel>(
                            key = "episode_detail_${key.id}",
                            parameters = { parametersOf(key.id) }
                        )
                        EpisodeDetailScreen(
                            viewModel = viewModel,
                            onBackClick = { if (backStack.size > 1) backStack.removeLast() },
                            onCharacterClick = { characterId ->
                                backStack.add(Screen.CharacterDetail(characterId))
                            }
                        )
                    }

                    entry<Screen.Favorites> {
                        val viewModel = koinViewModel<FavoritesViewModel>()
                        FavoritesScreen(
                            viewModel = viewModel,
                            onCharacterClick = { characterId ->
                                backStack.add(Screen.CharacterDetail(characterId))
                            },
                            onEpisodeClick = { episodeId ->
                                backStack.add(Screen.EpisodeDetail(episodeId))
                            }
                        )
                    }
                }
            }
        )
    }
}
