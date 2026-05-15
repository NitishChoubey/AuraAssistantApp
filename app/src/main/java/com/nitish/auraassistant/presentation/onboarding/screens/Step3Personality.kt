package com.nitish.auraassistant.presentation.onboarding.screens

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nitish.auraassistant.presentation.onboarding.OnboardingUiState
import com.nitish.auraassistant.presentation.theme.AuraAccent
import com.nitish.auraassistant.presentation.theme.AuraAccentBright
import com.nitish.auraassistant.presentation.theme.AuraDeepBg
import com.nitish.auraassistant.presentation.theme.AuraError
import com.nitish.auraassistant.presentation.theme.AuraGlow
import com.nitish.auraassistant.presentation.theme.AuraOnSurface
import com.nitish.auraassistant.presentation.theme.AuraOnSurfaceDim
import com.nitish.auraassistant.presentation.theme.AuraSurfaceVariant

private val allTraits = listOf(
    "Creative", "Analytical", "Empathetic", "Adventurous", "Calm",
    "Ambitious", "Humorous", "Introverted", "Extroverted", "Curious",
    "Organized", "Spontaneous", "Caring", "Logical", "Artistic",
    "Strategic", "Mindful", "Energetic", "Thoughtful", "Optimistic"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step3Personality(
    state: OnboardingUiState,
    onToggleTrait: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraDeepBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(
            text = "Your Personality",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = AuraOnSurface, fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Pick 3 traits that best describe you",
            style = MaterialTheme.typography.bodyMedium.copy(color = AuraOnSurfaceDim)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.selectedTraits.size} / 3 selected",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = if (state.selectedTraits.size == 3) AuraAccentBright else AuraOnSurfaceDim
                )
            )
            if (state.selectedTraits.size == 3) {
                Text(
                    text = "✓ Perfect!",
                    style = MaterialTheme.typography.labelMedium.copy(color = AuraAccentBright)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AuraSurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(state.selectedTraits.size / 3f)
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(listOf(AuraGlow, AuraAccentBright)),
                        RoundedCornerShape(2.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            allTraits.forEach { trait ->
                TraitChip(
                    label = trait,
                    selected = state.selectedTraits.contains(trait),
                    disabled = !state.selectedTraits.contains(trait) && state.selectedTraits.size >= 3,
                    onClick = { onToggleTrait(trait) }
                )
            }
        }

        if (state.traitsError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.traitsError,
                color = AuraError,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.selectedTraits.isNotEmpty()) {
            Text(
                text = "Your selected traits:",
                style = MaterialTheme.typography.labelMedium.copy(color = AuraOnSurfaceDim)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.selectedTraits.forEach { trait ->
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(AuraGlow.copy(0.4f), AuraAccent.copy(0.4f))),
                                RoundedCornerShape(20.dp)
                            )
                            .border(1.dp, AuraAccent, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = trait,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = AuraAccentBright, fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TraitChip(
    label: String,
    selected: Boolean,
    disabled: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) AuraAccent.copy(0.2f) else AuraSurfaceVariant,
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) AuraAccent else AuraOnSurfaceDim.copy(alpha = 0.2f),
        label = "chipBorder"
    )
    val textColor by animateColorAsState(
        targetValue = when {
            selected -> AuraAccentBright
            disabled -> AuraOnSurfaceDim.copy(alpha = 0.35f)
            else -> AuraOnSurface
        },
        label = "chipText"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chipScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .background(bgColor, RoundedCornerShape(24.dp))
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = !disabled || selected) { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = if (selected) "✓ $label" else label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = textColor, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 13.sp
            )
        )
    }
}

