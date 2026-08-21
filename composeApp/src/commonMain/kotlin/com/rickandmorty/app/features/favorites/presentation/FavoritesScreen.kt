package com.rickandmorty.app.features.favorites.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rickandmorty.app.core.designsystem.components.LoadingView
import com.rickandmorty.app.core.designsystem.components.RickMortyTopBar
import com.rickandmorty.app.core.designsystem.theme.PortalGreen
import com.rickandmorty.app.core.designsystem.theme.StatusDead
import com.rickandmorty.app.core.designsystem.theme.TextMuted
import com.rickandmorty.app.core.designsystem.theme.TextPrimary
import com.rickandmorty.app.features.characters.presentation.list.components.CharacterCard
import com.rickandmorty.app.features.episodes.presentation.list.components.EpisodeCard
import com.rickandmorty.app.features.favorites.presentation.components.FavoritesSegmentedControl

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onCharacterClick: (Int) -> Unit,
    onEpisodeClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    FavoritesScreenContent(
        state = state,
        onAction = viewModel::onAction,
        onCharacterClick = onCharacterClick,
        onEpisodeClick = onEpisodeClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreenContent(
    state: FavoritesState,
    onAction: (FavoritesAction) -> Unit,
    onCharacterClick: (Int) -> Unit,
    onEpisodeClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            RickMortyTopBar(
                title = "Favorites Vault",
                centered = false
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Segmented Tab Selector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                FavoritesSegmentedControl(
                    selectedTab = state.selectedTab,
                    onTabSelect = { onAction(FavoritesAction.OnTabSelect(it)) },
                    charactersCount = state.favoriteCharacters.size,
                    episodesCount = state.favoriteEpisodes.size
                )
            }

            // Main Content Area with Animated Transition
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    state.isLoading -> {
                        LoadingView(
                            message = "Opening Vault Storage...",
                            subMessage = "Retrieving bookmarked biometric & transmission records"
                        )
                    }

                    state.isCurrentTabEmpty -> {
                        EmptyFavoritesView(tab = state.selectedTab)
                    }

                    else -> {
                        Crossfade(targetState = state.selectedTab, label = "tab_crossfade") { tab ->
                            when (tab) {
                                FavoritesTab.CHARACTERS -> {
                                    LazyVerticalGrid(
                                        columns = GridCells.Adaptive(minSize = 160.dp),
                                        contentPadding = PaddingValues(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(
                                            items = state.favoriteCharacters,
                                            key = { it.id }
                                        ) { character ->
                                            CharacterCard(
                                                character = character,
                                                isFavorite = true,
                                                onClick = { onCharacterClick(character.id) },
                                                onToggleFavorite = {
                                                    onAction(FavoritesAction.OnToggleCharacterFavorite(character))
                                                }
                                            )
                                        }
                                    }
                                }

                                FavoritesTab.EPISODES -> {
                                    LazyColumn(
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(
                                            items = state.favoriteEpisodes,
                                            key = { it.id }
                                        ) { episode ->
                                            EpisodeCard(
                                                episode = episode,
                                                isFavorite = true,
                                                onClick = { onEpisodeClick(episode.id) },
                                                onToggleFavorite = {
                                                    onAction(FavoritesAction.OnToggleEpisodeFavorite(episode))
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesView(
    tab: FavoritesTab,
    modifier: Modifier = Modifier
) {
    val (title, description) = when (tab) {
        FavoritesTab.CHARACTERS -> Pair(
            "No Favorite Characters",
            "Bookmark lifeforms across the multiverse by tapping the heart icon on any character card."
        )
        FavoritesTab.EPISODES -> Pair(
            "No Favorite Episodes",
            "Save interdimensional cable transmissions by tapping the heart icon on any episode."
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(StatusDead.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = StatusDead,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
