package com.nitish.auraassistant.presentation.onboarding.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nitish.auraassistant.presentation.theme.AuraAccent
import com.nitish.auraassistant.presentation.theme.AuraAccentBright
import com.nitish.auraassistant.presentation.theme.AuraDeepBg
import com.nitish.auraassistant.presentation.theme.AuraGlow
import com.nitish.auraassistant.presentation.theme.AuraOnSurface
import com.nitish.auraassistant.presentation.theme.AuraOnSurfaceDim
import kotlinx.coroutines.delay

private val valueProps = listOf(
    Triple("Your AI Companion", "Always by your side, understanding your thoughts and feelings in real time.", "\uD83E\uDD16"),
    Triple("Voice-First Experience", "Just speak naturally. Aura listens and responds like a true companion.", "\uD83C\uDFA4"),
    Triple("Smart & Private", "All your conversations are stored locally. Your data, your control.", "\uD83D\uDD12"),
    Triple("Learns From You", "Aura adapts to your personality, preferences, and communication style.", "\u2728"),
)

@Composable
fun Step1ValueProps() {
    var visibleIndex by remember { mutableIntStateOf(0) }
    var visibleFlags by remember { mutableStateOf(List(valueProps.size) { false }) }

    LaunchedEffect(Unit) {
        for (i in valueProps.indices) {
            delay(if (i == 0) 300L else 900L)
            visibleFlags = visibleFlags.toMutableList().also { it[i] = true }
            visibleIndex = i
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraDeepBg),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .scale(pulseScale)
                .alpha(glowAlpha)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(listOf(AuraGlow.copy(alpha = 0.3f), AuraDeepBg)),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "✦ AURA",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 6.sp, color = AuraAccentBright, fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your personal AI assistant",
                style = MaterialTheme.typography.bodyMedium.copy(color = AuraOnSurfaceDim),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(56.dp))

            valueProps.forEachIndexed { idx, (title, desc, emoji) ->
                AnimatedVisibility(
                    visible = visibleFlags[idx],
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 }
                ) {
                    ValuePropCard(emoji = emoji, title = title, description = desc)
                }
                if (idx < valueProps.size - 1) Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ValuePropCard(emoji: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    Brush.radialGradient(listOf(AuraAccent.copy(0.25f), AuraDeepBg)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 22.sp)
        }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = AuraOnSurface, fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(color = AuraOnSurfaceDim),
                lineHeight = 18.sp
            )
        }
    }
}

