package com.rickandmorty.app.features.characters.presentation.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rickandmorty.app.core.designsystem.components.ErrorRetryView
import com.rickandmorty.app.core.designsystem.components.LoadingView
import com.rickandmorty.app.core.designsystem.components.PortalLoader
import com.rickandmorty.app.core.designsystem.components.PortalSearchBar
import com.rickandmorty.app.core.designsystem.components.RickMortyTopBar
import com.rickandmorty.app.core.designsystem.theme.CyberYellow
import com.rickandmorty.app.core.designsystem.theme.ElectricCyan
import com.rickandmorty.app.core.designsystem.theme.PortalGreen
import com.rickandmorty.app.core.designsystem.theme.SpaceBlack
import com.rickandmorty.app.core.designsystem.theme.TextMuted
import com.rickandmorty.app.core.designsystem.theme.TextPrimary
import com.rickandmorty.app.core.designsystem.theme.TextSecondary
import com.rickandmorty.app.features.characters.presentation.list.components.CharacterCard
import com.rickandmorty.app.features.characters.presentation.list.components.CharacterFilterBottomSheet

@Composable
fun CharactersScreen(
    viewModel: CharactersViewModel,
    onCharacterClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CharactersScreenContent(
        state = state,
        onAction = viewModel::onAction,
        onCharacterClick = onCharacterClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharactersScreenContent(
    state: CharactersState,
    onAction: (CharactersAction) -> Unit,
    onCharacterClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    val pullToRefreshState = rememberPullToRefreshState()

    // Pagination trigger when nearing end of grid
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !state.isLoading && !state.isLoadingMore && !state.endReached) {
            onAction(CharactersAction.OnLoadNextPage)
        }
    }

    Scaffold(
        topBar = {
            RickMortyTopBar(
                title = "Characters",
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
            // Search Bar with Filter Icon Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                PortalSearchBar(
                    query = state.searchQuery,
                    onQueryChange = { onAction(CharactersAction.OnSearchQueryChange(it)) },
                    placeholder = "Search by name across dimensions...",
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onAction(CharactersAction.OnSearchQueryChange("")) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            IconButton(
                                onClick = { onAction(CharactersAction.OnOpenFilterSheet) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (state.isFilterActive) {
                                            Badge(
                                                containerColor = CyberYellow,
                                                modifier = Modifier.size(8.dp)
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Filter characters",
                                        tint = if (state.isFilterActive) PortalGreen else ElectricCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    state.isLoading && state.characters.isEmpty() -> {
                        LoadingView(
                            message = "Summoning Characters...",
                            subMessage = "Scanning multiverse coordinate registry"
                        )
                    }

                    state.errorMessage != null && state.characters.isEmpty() -> {
                        ErrorRetryView(
                            message = state.errorMessage,
                            onRetry = { onAction(CharactersAction.OnRetry) }
                        )
                    }

                    state.isEmpty -> {
                        EmptyCharactersView(
                            isFilterActive = state.isFilterActive,
                            onClearFilters = { onAction(CharactersAction.OnClearFilters) }
                        )
                    }

                    else -> {
                        PullToRefreshBox(
                            isRefreshing = state.isRefreshing,
                            onRefresh = { onAction(CharactersAction.OnRefresh) },
                            state = pullToRefreshState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 160.dp),
                                state = gridState,
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    items = state.characters,
                                    key = { it.id }
                                ) { character ->
                                    val isFav = state.favoriteIds.contains(character.id)
                                    CharacterCard(
                                        character = character,
                                        isFavorite = isFav,
                                        onClick = { onCharacterClick(character.id) },
                                        onToggleFavorite = {
                                            onAction(CharactersAction.OnToggleFavorite(character))
                                        }
                                    )
                                }

                                if (state.isLoadingMore) {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            PortalLoader(size = 48.dp)
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

    // Filter BottomSheet
    if (state.isFilterSheetVisible) {
        CharacterFilterBottomSheet(
            selectedStatus = state.selectedStatus,
            selectedGender = state.selectedGender,
            onApply = { status, gender ->
                onAction(CharactersAction.OnStatusFilterSelect(status))
                onAction(CharactersAction.OnGenderFilterSelect(gender))
                onAction(CharactersAction.OnDismissFilterSheet)
            },
            onReset = {
                onAction(CharactersAction.OnClearFilters)
                onAction(CharactersAction.OnDismissFilterSheet)
            },
            onDismiss = { onAction(CharactersAction.OnDismissFilterSheet) }
        )
    }
}

@Composable
private fun EmptyCharactersView(
    isFilterActive: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(PortalGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = PortalGreen,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Lifeforms Found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Try adjusting your search query or interdimensional filter parameters.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            if (isFilterActive) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onClearFilters,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PortalGreen,
                        contentColor = SpaceBlack
                    )
                ) {
                    Text(
                        text = "Reset All Filters",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
