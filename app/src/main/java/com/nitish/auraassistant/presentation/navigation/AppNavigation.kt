package com.nitish.auraassistant.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nitish.auraassistant.domain.repository.UserRepository
import com.nitish.auraassistant.presentation.home.screens.HomeScreen
import com.nitish.auraassistant.presentation.onboarding.screens.OnboardingScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
}

@Composable
fun AppNavigation(
    userRepository: UserRepository = hiltViewModel<NavViewModel>().userRepository
) {
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val isComplete = userRepository.isOnboardingComplete()
        startDestination = if (isComplete) Screen.Home.route else Screen.Onboarding.route
    }

    startDestination?.let { start ->
        NavHost(navController = navController, startDestination = start) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen()
            }
        }
    }
}