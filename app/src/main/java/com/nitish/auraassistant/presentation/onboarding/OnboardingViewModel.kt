package com.nitish.auraassistant.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitish.auraassistant.domain.model.UserProfile
import com.nitish.auraassistant.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 0,
    val name: String = "",
    val age: String = "",
    val phone: String = "",
    val otpInput: String = "",
    val otpSent: Boolean = false,
    val otpVerified: Boolean = false,
    val selectedTraits: List<String> = emptyList(),
    val nameError: String? = null,
    val ageError: String? = null,
    val phoneError: String? = null,
    val otpError: String? = null,
    val traitsError: String? = null,
    val isComplete: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.getUserProfile().collect { profile ->
                if (profile.name.isNotBlank()) {
                    _uiState.update {
                        it.copy(
                            name = profile.name,
                            age = profile.age,
                            phone = profile.phone,
                            selectedTraits = profile.personalityTraits
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null) }
    fun onAgeChange(value: String) = _uiState.update { it.copy(age = value, ageError = null) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value, phoneError = null, otpSent = false, otpVerified = false) }
    fun onOtpChange(value: String) = _uiState.update { it.copy(otpInput = value, otpError = null) }

    fun sendOtp() {
        val phone = _uiState.value.phone
        if (phone.length != 10) {
            _uiState.update { it.copy(phoneError = "Enter a valid 10-digit phone number") }
            return
        }
        _uiState.update { it.copy(otpSent = true, otpError = null) }
    }

    fun verifyOtp(): Boolean {
        val otp = _uiState.value.otpInput
        return if (otp == "1234") {
            _uiState.update { it.copy(otpVerified = true, otpError = null) }
            true
        } else {
            _uiState.update { it.copy(otpError = "Invalid OTP. Try 1234") }
            false
        }
    }

    fun toggleTrait(trait: String) {
        val current = _uiState.value.selectedTraits.toMutableList()
        if (current.contains(trait)) {
            current.remove(trait)
        } else if (current.size < 3) {
            current.add(trait)
        }
        _uiState.update { it.copy(selectedTraits = current, traitsError = null) }
    }

    fun goToNextStep(): Boolean {
        return when (_uiState.value.currentStep) {
            0 -> {
                _uiState.update { it.copy(currentStep = 1) }
                true
            }
            1 -> {
                if (!validateStep2()) return false
                savePartialProfile()
                _uiState.update { it.copy(currentStep = 2) }
                true
            }
            2 -> {
                if (!validateStep3()) return false
                completeOnboarding()
                true
            }
            else -> false
        }
    }

    fun goToPreviousStep() {
        val current = _uiState.value.currentStep
        if (current > 0) _uiState.update { it.copy(currentStep = current - 1) }
    }

    private fun validateStep2(): Boolean {
        val state = _uiState.value
        var valid = true
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            valid = false
        }
        val ageInt = state.age.toIntOrNull()
        if (ageInt == null || ageInt < 1 || ageInt > 120) {
            _uiState.update { it.copy(ageError = "Enter a valid age (1–120)") }
            valid = false
        }
        if (state.phone.length != 10) {
            _uiState.update { it.copy(phoneError = "Enter a valid 10-digit phone number") }
            valid = false
        }
        if (!state.otpVerified) {
            _uiState.update { it.copy(otpError = "Please verify your OTP first") }
            valid = false
        }
        return valid
    }

    private fun validateStep3(): Boolean {
        return if (_uiState.value.selectedTraits.size < 3) {
            _uiState.update { it.copy(traitsError = "Please select exactly 3 personality traits") }
            false
        } else true
    }

    private fun savePartialProfile() {
        val s = _uiState.value
        viewModelScope.launch {
            userRepository.saveUserProfile(
                UserProfile(name = s.name, age = s.age, phone = s.phone,
                    personalityTraits = s.selectedTraits, isOnboardingComplete = false)
            )
        }
    }

    private fun completeOnboarding() {
        val s = _uiState.value
        viewModelScope.launch {
            userRepository.saveUserProfile(
                UserProfile(name = s.name, age = s.age, phone = s.phone,
                    personalityTraits = s.selectedTraits, isOnboardingComplete = true)
            )
            _uiState.update { it.copy(isComplete = true) }
        }
    }
}

