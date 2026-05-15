package com.nitish.auraassistant.presentation.home.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.KeyboardVoice
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nitish.auraassistant.data.sync.SyncStatus
import com.nitish.auraassistant.presentation.home.HomeViewModel
import com.nitish.auraassistant.presentation.theme.AuraAccent
import com.nitish.auraassistant.presentation.theme.AuraAccentBright
import com.nitish.auraassistant.presentation.theme.AuraDeepBg
import com.nitish.auraassistant.presentation.theme.AuraError
import com.nitish.auraassistant.presentation.theme.AuraGlow
import com.nitish.auraassistant.presentation.theme.AuraOnSurface
import com.nitish.auraassistant.presentation.theme.AuraOnSurfaceDim
import com.nitish.auraassistant.presentation.theme.AuraSurface
import com.nitish.auraassistant.presentation.theme.AuraSurfaceVariant
import com.nitish.auraassistant.presentation.theme.AuraSuccess
import com.nitish.auraassistant.statemachine.MessageState

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val msgState by viewModel.messageState.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    var dragOffset by remember { mutableFloatStateOf(0f) }
    val scrollThreshold = -120f
    val isScrolledUp = dragOffset < scrollThreshold

    val circleAlpha by animateFloatAsState(
        targetValue = if (isScrolledUp) 0f else 1f,
        animationSpec = tween(400),
        label = "circleAlpha"
    )
    val circleScale by animateFloatAsState(
        targetValue = if (isScrolledUp) 0.7f else 1f,
        animationSpec = tween(400),
        label = "circleScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraDeepBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(userName = uiState.userName, syncStatus = syncStatus)

            Box(modifier = Modifier.weight(1f)) {
                AnimatedVisibility(
                    visible = isScrolledUp,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    ChatHistoryList(
                        messages = uiState.messages,
                        isLoadingMore = uiState.isLoadingMore,
                        hasMorePages = uiState.hasMorePages,
                        onLoadMore = viewModel::loadNextPage,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                AnimatedVisibility(
                    visible = !isScrolledUp,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragEnd = { if (dragOffset >= scrollThreshold) dragOffset = 0f },
                                    onDragCancel = { dragOffset = 0f }
                                ) { _, dragAmount ->
                                    dragOffset = (dragOffset + dragAmount).coerceIn(-300f, 0f)
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .alpha(circleAlpha)
                                .scale(circleScale),
                            contentAlignment = Alignment.Center
                        ) {
                            AuraCircle(state = uiState.auraCircleState, size = 260.dp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        StateBanner(msgState = msgState, onRetry = viewModel::retryLastMessage)

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "↑ Swipe up for chat history",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AuraOnSurfaceDim.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                if (isScrolledUp) {
                    TextButton(
                        onClick = { dragOffset = 0f },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    ) {
                        Text("↓ Back to Aura", color = AuraAccentBright, fontSize = 12.sp)
                    }
                }
            }

            BottomInputBar(
                inputText = uiState.inputText,
                isKeyboardVisible = uiState.isKeyboardVisible,
                isListening = uiState.auraCircleState is AuraCircleState.Listening,
                onInputChange = viewModel::onInputChange,
                onSend = viewModel::sendMessage,
                onToggleKeyboard = viewModel::toggleKeyboard,
                onToggleMic = {
                    if (uiState.auraCircleState is AuraCircleState.Listening) viewModel.stopListening()
                    else viewModel.startListening()
                }
            )
        }
    }
}

@Composable
private fun TopBar(userName: String, syncStatus: SyncStatus) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "✦ Aura",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = AuraAccentBright, fontWeight = FontWeight.Bold
                )
            )
            if (userName.isNotBlank()) {
                Text(
                    "Hey, $userName",
                    style = MaterialTheme.typography.bodySmall.copy(color = AuraOnSurfaceDim)
                )
            }
        }

        SyncStatusBadge(status = syncStatus)
    }
}

@Composable
private fun SyncStatusBadge(status: SyncStatus) {
    val (label, color) = when (status) {
        is SyncStatus.Syncing -> "Syncing…" to AuraAccentBright
        is SyncStatus.Success -> "Synced" to AuraSuccess
        is SyncStatus.Error -> "Sync error" to AuraError
        else -> return
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = color))
    }
}

@Composable
private fun StateBanner(msgState: MessageState, onRetry: () -> Unit) {
    AnimatedContent(
        targetState = msgState,
        transitionSpec = {
            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
        },
        label = "stateBanner"
    ) { state ->
        when (state) {
            is MessageState.Idle -> Box(modifier = Modifier.height(40.dp))
            is MessageState.Typing -> StateChip("Typing…", AuraOnSurfaceDim)
            is MessageState.Validating -> StateChip("Validating…", AuraAccentBright)
            is MessageState.Processing -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    StateChip("Processing…", AuraAccent)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.width(140.dp).clip(CircleShape),
                        color = AuraAccent,
                        trackColor = AuraSurfaceVariant
                    )
                }
            }
            is MessageState.Responding -> StateChip("Responding…", AuraSuccess)
            is MessageState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    StateChip("⚠ ${state.reason}", AuraError)
                    TextButton(onClick = onRetry) {
                        Text("Retry", color = AuraAccentBright, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StateChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium.copy(color = color))
    }
}

@Composable
private fun BottomInputBar(
    inputText: String,
    isKeyboardVisible: Boolean,
    isListening: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onToggleKeyboard: () -> Unit,
    onToggleMic: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AuraSurface)
            .imePadding()
    ) {
        AnimatedVisibility(
            visible = isKeyboardVisible,
            enter = slideInVertically(tween(300)) { it } + fadeIn(tween(300)),
            exit = slideOutVertically(tween(300)) { it } + fadeOut(tween(300))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    placeholder = { Text("Ask Aura anything…", color = AuraOnSurfaceDim) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraAccent,
                        unfocusedBorderColor = AuraSurfaceVariant,
                        focusedTextColor = AuraOnSurface,
                        unfocusedTextColor = AuraOnSurface,
                        cursorColor = AuraAccent,
                        focusedContainerColor = AuraInputBg,
                        unfocusedContainerColor = AuraInputBg
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.radialGradient(listOf(AuraAccent, AuraGlow)),
                            CircleShape
                        )
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = AuraDeepBg)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleKeyboard,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (isKeyboardVisible) AuraAccent.copy(0.2f) else AuraSurfaceVariant,
                        CircleShape
                    )
                    .border(
                        1.dp,
                        if (isKeyboardVisible) AuraAccent else AuraSurfaceVariant,
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Keyboard",
                    tint = if (isKeyboardVisible) AuraAccent else AuraOnSurfaceDim
                )
            }

            IconButton(
                onClick = onToggleMic,
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.radialGradient(
                            if (isListening) listOf(AuraError.copy(0.7f), AuraError.copy(0.3f))
                            else listOf(AuraAccent, AuraGlow)
                        ),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.KeyboardVoice,
                    contentDescription = if (isListening) "Stop" else "Mic",
                    tint = if (isListening) AuraDeepBg else AuraDeepBg,
                    modifier = Modifier.size(30.dp)
                )
            }

            Box(modifier = Modifier.size(56.dp))
        }
    }
}

