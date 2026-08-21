package com.rickandmorty.app.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rickandmorty.app.core.designsystem.theme.StatusAlive
import com.rickandmorty.app.core.designsystem.theme.StatusDead
import com.rickandmorty.app.core.designsystem.theme.StatusUnknown
import com.rickandmorty.app.core.designsystem.theme.TextPrimary

enum class CharacterStatus(val label: String, val color: Color) {
    ALIVE("Alive", StatusAlive),
    DEAD("Dead", StatusDead),
    UNKNOWN("Unknown", StatusUnknown);

    companion object {
        fun fromString(status: String?): CharacterStatus {
            return when (status?.trim()?.lowercase()) {
                "alive" -> ALIVE
                "dead" -> DEAD
                else -> UNKNOWN
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    animated: Boolean = true
) {
    val parsedStatus = CharacterStatus.fromString(status)
    StatusBadge(
        status = parsedStatus,
        modifier = modifier,
        animated = animated
    )
}

@Composable
fun StatusBadge(
    status: CharacterStatus,
    modifier: Modifier = Modifier,
    animated: Boolean = true
) {
    val statusColor = status.color

    val haloAlpha by if (animated && status == CharacterStatus.ALIVE) {
        val infiniteTransition = rememberInfiniteTransition(label = "status_glow")
        infiniteTransition.animateFloat(
            initialValue = 0.25f,
            targetValue = 0.65f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "halo_alpha"
        )
    } else {
        rememberUpdatedState(0.35f)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(statusColor.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = statusColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glowing dot
        Box(
            modifier = Modifier.size(10.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow halo
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = haloAlpha))
            )
            // Inner core dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
