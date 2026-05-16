package com.nitish.auraassistant.presentation.navigation

import androidx.lifecycle.ViewModel
import com.nitish.auraassistant.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    val userRepository: UserRepository
) : ViewModel()
