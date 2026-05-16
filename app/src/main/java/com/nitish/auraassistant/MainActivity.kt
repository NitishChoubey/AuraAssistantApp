package com.nitish.auraassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nitish.auraassistant.presentation.navigation.AppNavigation
import com.nitish.auraassistant.presentation.theme.AuraAssistantTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuraAssistantTheme {
                AppNavigation()
            }
        }
    }
}

