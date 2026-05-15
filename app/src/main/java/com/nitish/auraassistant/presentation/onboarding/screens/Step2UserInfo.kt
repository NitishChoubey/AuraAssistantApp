package com.nitish.auraassistant.presentation.onboarding.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nitish.auraassistant.presentation.onboarding.OnboardingUiState
import com.nitish.auraassistant.presentation.theme.AuraAccent
import com.nitish.auraassistant.presentation.theme.AuraAccentBright
import com.nitish.auraassistant.presentation.theme.AuraDeepBg
import com.nitish.auraassistant.presentation.theme.AuraError
import com.nitish.auraassistant.presentation.theme.AuraInputBg
import com.nitish.auraassistant.presentation.theme.AuraOnSurface
import com.nitish.auraassistant.presentation.theme.AuraOnSurfaceDim
import com.nitish.auraassistant.presentation.theme.AuraSurface
import com.nitish.auraassistant.presentation.theme.AuraSuccess

@Composable
fun Step2UserInfo(
    state: OnboardingUiState,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraDeepBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Tell us about you",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = AuraOnSurface, fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "We'll personalise Aura just for you",
            style = MaterialTheme.typography.bodyMedium.copy(color = AuraOnSurfaceDim)
        )
        Spacer(modifier = Modifier.height(36.dp))

        AuraTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = "Your Name",
            placeholder = "e.g. Nitish",
            error = state.nameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(16.dp))

        AuraTextField(
            value = state.age,
            onValueChange = onAgeChange,
            label = "Your Age",
            placeholder = "e.g. 24",
            error = state.ageError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AuraTextField(
                modifier = Modifier.weight(1f),
                value = state.phone,
                onValueChange = onPhoneChange,
                label = "Phone Number",
                placeholder = "10-digit number",
                error = state.phoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                trailingIcon = if (state.otpVerified) ({
                    Icon(Icons.Default.CheckCircle, null, tint = AuraSuccess)
                }) else null
            )
            Button(
                onClick = onSendOtp,
                modifier = Modifier.padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.otpSent) AuraSurface else AuraAccent,
                    contentColor = if (state.otpSent) AuraAccentBright else AuraDeepBg
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (state.otpSent) "Resend" else "Send OTP",
                    fontSize = 13.sp
                )
            }
        }

        AnimatedVisibility(
            visible = state.otpSent && !state.otpVerified,
            enter = expandVertically() + fadeIn()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AuraTextField(
                        modifier = Modifier.weight(1f),
                        value = state.otpInput,
                        onValueChange = onOtpChange,
                        label = "Enter OTP",
                        placeholder = "- - - -",
                        error = state.otpError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done)
                    )
                    Button(
                        onClick = onVerifyOtp,
                        modifier = Modifier.padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AuraAccent, contentColor = AuraDeepBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verify", fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Hint: OTP is 1234",
                    style = MaterialTheme.typography.labelSmall.copy(color = AuraOnSurfaceDim),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        AnimatedVisibility(visible = state.otpVerified) {
            Row(
                modifier = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = AuraSuccess)
                Text(
                    "Phone number verified!",
                    style = MaterialTheme.typography.bodySmall.copy(color = AuraSuccess)
                )
            }
        }
    }
}

@Composable
fun AuraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder, color = AuraOnSurfaceDim) },
            isError = error != null,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AuraAccent,
                unfocusedBorderColor = AuraOnSurfaceDim.copy(alpha = 0.3f),
                errorBorderColor = AuraError,
                focusedLabelColor = AuraAccentBright,
                unfocusedLabelColor = AuraOnSurfaceDim,
                cursorColor = AuraAccent,
                focusedTextColor = AuraOnSurface,
                unfocusedTextColor = AuraOnSurface,
                focusedContainerColor = AuraInputBg,
                unfocusedContainerColor = AuraInputBg,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (error != null) {
            Text(
                text = error,
                color = AuraError,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

