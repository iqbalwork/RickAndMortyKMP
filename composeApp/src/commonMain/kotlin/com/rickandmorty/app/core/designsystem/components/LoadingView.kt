package com.rickandmorty.app.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rickandmorty.app.core.designsystem.theme.CyberYellow
import com.rickandmorty.app.core.designsystem.theme.ElectricCyan
import com.rickandmorty.app.core.designsystem.theme.PortalGreen
import com.rickandmorty.app.core.designsystem.theme.PortalGreenDark
import com.rickandmorty.app.core.designsystem.theme.TextPrimary
import com.rickandmorty.app.core.designsystem.theme.TextSecondary

@Composable
fun PortalLoader(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "portal_loader_anim")

    // Rotation animation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "portal_rotation"
    )

    // Reverse rotation for counter-spinning swirl
    val reverseRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "portal_reverse_rotation"
    )

    // Pulse animation for core glow
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "portal_pulse"
    )

    val portalColors = listOf(
        PortalGreen,
        ElectricCyan,
        PortalGreenDark,
        CyberYellow,
        PortalGreen
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.minDimension / 2f) * 0.85f

            // 1. Center pulsing portal glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PortalGreen.copy(alpha = 0.45f * (pulseScale / 1.15f)),
                        ElectricCyan.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * pulseScale
                ),
                radius = radius * pulseScale,
                center = center
            )

            // 2. Outer swirling neon ring
            rotate(degrees = rotation, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = portalColors,
                        center = center
                    ),
                    radius = radius,
                    center = center,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

            // 3. Inner counter-rotating ring
            rotate(degrees = reverseRotation, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            ElectricCyan,
                            PortalGreen,
                            Color.Transparent,
                            PortalGreen
                        ),
                        center = center
                    ),
                    radius = radius * 0.65f,
                    center = center,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

            // 4. Center singularity spark
            drawCircle(
                color = PortalGreen,
                radius = 3.dp.toPx() * pulseScale,
                center = center
            )
        }
    }
}

@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    message: String = "Opening Interdimensional Portal...",
    subMessage: String? = "Calibrating dimensional coordinates..."
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            PortalLoader(size = 96.dp)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            if (subMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
