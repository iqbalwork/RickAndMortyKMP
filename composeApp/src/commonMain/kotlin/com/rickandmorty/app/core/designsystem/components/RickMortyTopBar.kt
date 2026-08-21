package com.rickandmorty.app.core.designsystem.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RickMortyTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    centered: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        navigationIconContentColor = MaterialTheme.colorScheme.primary,
        actionIconContentColor = MaterialTheme.colorScheme.primary
    ),
    showBottomDivider: Boolean = true,
    dividerColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
) {
    RickMortyTopBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        centered = centered,
        scrollBehavior = scrollBehavior,
        colors = colors,
        showBottomDivider = showBottomDivider,
        dividerColor = dividerColor
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RickMortyTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    centered: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        navigationIconContentColor = MaterialTheme.colorScheme.primary,
        actionIconContentColor = MaterialTheme.colorScheme.primary
    ),
    showBottomDivider: Boolean = true,
    dividerColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
) {
    val bottomDividerModifier = if (showBottomDivider) {
        Modifier.drawBehind {
            val strokeWidth = 1.dp.toPx()
            drawLine(
                color = dividerColor,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = strokeWidth
            )
        }
    } else Modifier

    if (centered) {
        CenterAlignedTopAppBar(
            title = title,
            modifier = modifier.then(bottomDividerModifier),
            navigationIcon = { navigationIcon?.invoke() },
            actions = actions,
            colors = colors,
            scrollBehavior = scrollBehavior
        )
    } else {
        TopAppBar(
            title = title,
            modifier = modifier.then(bottomDividerModifier),
            navigationIcon = { navigationIcon?.invoke() },
            actions = actions,
            colors = colors,
            scrollBehavior = scrollBehavior
        )
    }
}
