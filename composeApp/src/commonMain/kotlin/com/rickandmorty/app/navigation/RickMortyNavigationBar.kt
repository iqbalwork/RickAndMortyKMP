package com.rickandmorty.app.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rickandmorty.app.core.designsystem.theme.BorderSlate
import com.rickandmorty.app.core.designsystem.theme.DarkVoid
import com.rickandmorty.app.core.designsystem.theme.ElectricCyan
import com.rickandmorty.app.core.designsystem.theme.PortalGreen
import com.rickandmorty.app.core.designsystem.theme.SpaceBlack
import com.rickandmorty.app.core.designsystem.theme.TextMuted

@Composable
fun RickMortyNavigationBar(
    currentDestination: BottomBarDestination?,
    onNavigateToDestination: (BottomBarDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(BorderSlate.copy(alpha = 0.6f), Color.Transparent)
                ),
                shape = androidx.compose.ui.graphics.RectangleShape
            ),
        containerColor = DarkVoid,
        tonalElevation = 0.dp
    ) {
        BottomBarDestination.entries.forEach { destination ->
            val isSelected = destination == currentDestination
            RickMortyNavItem(
                destination = destination,
                isSelected = isSelected,
                onClick = { onNavigateToDestination(destination) }
            )
        }
    }
}

@Composable
private fun RowScope.RickMortyNavItem(
    destination: BottomBarDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "nav_item_scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) PortalGreen else TextMuted,
        label = "nav_icon_color"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) PortalGreen else TextMuted,
        label = "nav_text_color"
    )

    NavigationBarItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.scale(scale)
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        PortalGreen.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
                Icon(
                    imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                    contentDescription = destination.title,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        label = {
            Text(
                text = destination.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = PortalGreen,
            unselectedIconColor = TextMuted,
            selectedTextColor = PortalGreen,
            unselectedTextColor = TextMuted,
            indicatorColor = Color.Transparent
        )
    )
}
