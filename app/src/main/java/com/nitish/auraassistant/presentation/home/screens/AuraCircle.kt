package com.nitish.auraassistant.presentation.home.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nitish.auraassistant.presentation.theme.AuraAccent
import com.nitish.auraassistant.presentation.theme.AuraAccentBright
import com.nitish.auraassistant.presentation.theme.AuraDeepBg
import com.nitish.auraassistant.presentation.theme.AuraGlow
import kotlin.math.sin

sealed class AuraCircleState {
    object Idle : AuraCircleState()
    data class Listening(val amplitude: Float = 0f) : AuraCircleState()
}

@Composable
fun AuraCircle(
    state: AuraCircleState,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aura")

    val breathingPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "breathingPhase"
    )

    val rotationPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationPhase"
    )

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val baseRadius = this.size.minDimension * 0.35f

        when (state) {
            is AuraCircleState.Idle -> drawIdleState(center, baseRadius, breathingPhase, rotationPhase)
            is AuraCircleState.Listening -> drawListeningState(center, baseRadius, state.amplitude, breathingPhase, rotationPhase)
        }
    }
}

private fun DrawScope.drawIdleState(
    center: Offset,
    baseRadius: Float,
    breathingPhase: Float,
    rotationPhase: Float
) {
    val breathScale = 1f + 0.07f * sin(breathingPhase)
    val radius = baseRadius * breathScale

    for (i in 5 downTo 1) {
        val glowAlpha = (0.04f * i) * breathScale
        val glowRadius = radius * (1f + 0.18f * i)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(AuraGlow.copy(alpha = glowAlpha), Color.Transparent),
                center = center,
                radius = glowRadius
            ),
            radius = glowRadius,
            center = center
        )
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(AuraAccentBright.copy(alpha = 0.15f), AuraGlow.copy(alpha = 0.35f), Color.Transparent),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(AuraAccentBright, AuraAccent, AuraGlow),
            center = center,
            radius = radius * 0.55f
        ),
        radius = radius * 0.55f,
        center = center
    )

    val ringAlpha = 0.3f + 0.15f * sin(breathingPhase)
    drawCircle(
        color = AuraAccentBright.copy(alpha = ringAlpha),
        radius = radius,
        center = center,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
    )

    val orbRadius = radius * 0.12f
    val orbDist = radius * 0.75f
    for (i in 0..2) {
        val angle = rotationPhase + i * (2 * Math.PI / 3).toFloat()
        val orbCenter = Offset(
            center.x + orbDist * kotlin.math.cos(angle),
            center.y + orbDist * kotlin.math.sin(angle)
        )
        drawCircle(
            color = AuraAccentBright.copy(alpha = 0.6f),
            radius = orbRadius,
            center = orbCenter
        )
    }
}

private fun DrawScope.drawListeningState(
    center: Offset,
    baseRadius: Float,
    amplitude: Float,
    breathingPhase: Float,
    rotationPhase: Float
) {
    val clampedAmp = amplitude.coerceIn(0f, 1f)
    val ampScale = 1f + 0.25f * clampedAmp
    val radius = baseRadius * ampScale

    for (i in 6 downTo 1) {
        val glowAlpha = (0.06f + 0.04f * clampedAmp) * i
        val glowRadius = radius * (1f + 0.22f * i)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(AuraAccent.copy(alpha = glowAlpha.coerceAtMost(0.4f)), Color.Transparent),
                center = center,
                radius = glowRadius
            ),
            radius = glowRadius,
            center = center
        )
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                AuraAccentBright.copy(alpha = 0.2f + 0.3f * clampedAmp),
                AuraGlow.copy(alpha = 0.4f),
                Color.Transparent
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.9f), AuraAccentBright, AuraAccent),
            center = center,
            radius = radius * 0.55f
        ),
        radius = radius * 0.55f,
        center = center
    )

    val ringAlpha = 0.5f + 0.4f * clampedAmp
    drawCircle(
        color = AuraAccentBright.copy(alpha = ringAlpha),
        radius = radius,
        center = center,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f + 2f * clampedAmp)
    )

    val waveCount = 3
    for (w in 0 until waveCount) {
        val waveAlpha = (0.15f + 0.2f * clampedAmp) * (1f - w * 0.25f)
        val waveRadius = radius * (1.1f + w * 0.12f + 0.08f * sin(breathingPhase + w))
        drawCircle(
            color = AuraAccent.copy(alpha = waveAlpha),
            radius = waveRadius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
        )
    }
}

