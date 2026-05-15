package com.nitish.auraassistant.presentation.onboarding.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nitish.auraassistant.presentation.onboarding.OnboardingViewModel
import com.nitish.auraassistant.presentation.theme.AuraAccent
import com.nitish.auraassistant.presentation.theme.AuraAccentBright
import com.nitish.auraassistant.presentation.theme.AuraDeepBg
import com.nitish.auraassistant.presentation.theme.AuraGlow
import com.nitish.auraassistant.presentation.theme.AuraOnSurfaceDim
import com.nitish.auraassistant.presentation.theme.AuraSurfaceVariant
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.currentStep) {
        pagerState.animateScrollToPage(state.currentStep)
    }

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onComplete()
    }

    BackHandler(enabled = state.currentStep > 0) {
        viewModel.goToPreviousStep()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraDeepBg)
            .navigationBarsPadding()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> Step1ValueProps()
                1 -> Step2UserInfo(
                    state = state,
                    onNameChange = viewModel::onNameChange,
                    onAgeChange = viewModel::onAgeChange,
                    onPhoneChange = viewModel::onPhoneChange,
                    onOtpChange = viewModel::onOtpChange,
                    onSendOtp = viewModel::sendOtp,
                    onVerifyOtp = { viewModel.verifyOtp() }
                )
                2 -> Step3Personality(
                    state = state,
                    onToggleTrait = viewModel::toggleTrait
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuraDeepBg)
                .padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StepIndicator(currentStep = state.currentStep, totalSteps = 3)
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.currentStep > 0) {
                    TextButton(
                        onClick = { viewModel.goToPreviousStep() }
                    ) {
                        Text(
                            "Back",
                            color = AuraOnSurfaceDim,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                Button(
                    onClick = { viewModel.goToNextStep() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AuraAccent),
                    modifier = Modifier.height(52.dp).weight(1f).padding(start = 16.dp)
                ) {
                    Text(
                        text = if (state.currentStep == 2) "Get Started ✦" else "Continue →",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuraDeepBg
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            val isPast = index < currentStep
            val width by animateDpAsState(
                targetValue = if (isActive) 32.dp else 8.dp,
                animationSpec = tween(300),
                label = "indicatorWidth"
            )
            val color by animateColorAsState(
                targetValue = when {
                    isActive -> AuraAccent
                    isPast -> AuraAccentBright.copy(alpha = 0.5f)
                    else -> AuraSurfaceVariant
                },
                label = "indicatorColor"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

