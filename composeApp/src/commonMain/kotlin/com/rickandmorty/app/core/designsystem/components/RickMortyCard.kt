package com.rickandmorty.app.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rickandmorty.app.core.designsystem.theme.BorderSlate
import com.rickandmorty.app.core.designsystem.theme.CardSurface
import com.rickandmorty.app.core.designsystem.theme.TextPrimary

@Composable
fun RickMortyCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = CardSurface,
    contentColor: Color = TextPrimary,
    borderColor: Color = BorderSlate,
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(borderWidth, borderColor),
        content = content
    )
}

@Composable
fun RickMortyCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = CardSurface,
    contentColor: Color = TextPrimary,
    borderColor: Color = BorderSlate,
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(borderWidth, borderColor),
        content = content
    )
}
