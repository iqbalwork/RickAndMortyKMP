package com.rickandmorty.app.features.episodes.presentation.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.rickandmorty.app.core.designsystem.components.ErrorRetryView
import com.rickandmorty.app.core.designsystem.components.LoadingView
import com.rickandmorty.app.core.designsystem.components.PortalLoader
import com.rickandmorty.app.core.designsystem.components.RickMortyCard
import com.rickandmorty.app.core.designsystem.components.RickMortyTopBar
import com.rickandmorty.app.core.designsystem.components.StatusBadge
import com.rickandmorty.app.core.designsystem.theme.BorderSlate
import com.rickandmorty.app.core.designsystem.theme.CardSurface
import com.rickandmorty.app.core.designsystem.theme.DarkVoid
import com.rickandmorty.app.core.designsystem.theme.ElectricCyan
import com.rickandmorty.app.core.designsystem.theme.PortalGreen
import com.rickandmorty.app.core.designsystem.theme.SpaceBlack
import com.rickandmorty.app.core.designsystem.theme.StatusDead
import com.rickandmorty.app.core.designsystem.theme.TextMuted
import com.rickandmorty.app.core.designsystem.theme.TextPrimary
import com.rickandmorty.app.core.designsystem.theme.TextSecondary
import com.rickandmorty.app.features.characters.domain.model.Character

@Composable
fun EpisodeDetailScreen(
    viewModel: EpisodeDetailViewModel,
    onBackClick: () -> Unit,
    onCharacterClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    EpisodeDetailScreenContent(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        onCharacterClick = onCharacterClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeDetailScreenContent(
    state: EpisodeDetailState,
    onAction: (EpisodeDetailAction) -> Unit,
    onBackClick: () -> Unit,
    onCharacterClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val heartColor by animateColorAsState(
        targetValue = if (state.isFavorite) StatusDead else TextSecondary,
        label = "episode_detail_heart_color"
    )

    val heartScale by animateFloatAsState(
        targetValue = if (state.isFavorite) 1.25f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "episode_detail_heart_scale"
    )

    Scaffold(
        topBar = {
            RickMortyTopBar(
                title = state.episode?.episodeCode ?: "Episode Transmission",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PortalGreen
                        )
                    }
                },
                actions = {
                    if (state.episode != null) {
                        IconButton(
                            onClick = { onAction(EpisodeDetailAction.OnToggleFavorite) }
                        ) {
                            Icon(
                                imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (state.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = heartColor,
                                modifier = Modifier.scale(heartScale)
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> {
                    LoadingView(
                        message = "Decoding Transmission Stream...",
                        subMessage = "Synchronizing interdimensional audio/video feed"
                    )
                }

                state.errorMessage != null && state.episode == null -> {
                    ErrorRetryView(
                        message = state.errorMessage,
                        onRetry = { onAction(EpisodeDetailAction.OnRetry) }
                    )
                }

                state.episode != null -> {
                    val episode = state.episode

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Hero Header
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EpisodeHeroHeader(
                                episodeCode = episode.episodeCode,
                                name = episode.name,
                                airDate = episode.airDate,
                                formattedSeason = episode.formattedSeasonEpisode,
                                characterCount = episode.characterIds.size
                            )
                        }

                        // Cast Section Title
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                                Text(
                                    text = "CAST APPEARANCES (${episode.characterIds.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan,
                                    letterSpacing = 1.2.sp
                                )
                            }
                        }

                        if (state.isLoadingCharacters) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PortalLoader(size = 48.dp)
                                }
                            }
                        } else if (state.characters.isEmpty() && episode.characterIds.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No recorded characters in this broadcast.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            items(
                                items = state.characters,
                                key = { it.id }
                            ) { character ->
                                CastGridCard(
                                    character = character,
                                    onClick = { onCharacterClick(character.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeHeroHeader(
    episodeCode: String,
    name: String,
    airDate: String,
    formattedSeason: String,
    characterCount: Int,
    modifier: Modifier = Modifier
) {
    RickMortyCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        containerColor = CardSurface,
        borderColor = PortalGreen.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TV Glowing Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PortalGreen.copy(alpha = 0.35f),
                                ElectricCyan.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.5.dp, PortalGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = PortalGreen,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Episode Code Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PortalGreen.copy(alpha = 0.15f))
                    .border(1.dp, PortalGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = episodeCode,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PortalGreen
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = formattedSeason,
                style = MaterialTheme.typography.bodyMedium,
                color = ElectricCyan,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Two pills: Air Date & Cast
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkVoid)
                        .border(1.dp, BorderSlate, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = airDate.ifBlank { "Unknown Date" },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkVoid)
                        .border(1.dp, BorderSlate, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = PortalGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$characterCount Characters",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CastGridCard(
    character: Character,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RickMortyCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        containerColor = CardSurface,
        borderColor = BorderSlate
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(SpaceBlack)
            ) {
                AsyncImage(
                    model = character.imageUrl,
                    contentDescription = character.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    StatusBadge(
                        status = character.status.displayName,
                        animated = false
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${character.species} • ${character.gender.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
