package com.nitish.auraassistant.statemachine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val PROCESSING_TIMEOUT_MS = 8_000L

class MessageStateMachine(
    private val scope: CoroutineScope,
    private val responseDelayMs: Long = 1_500L
) {

    private val _state = MutableStateFlow<MessageState>(MessageState.Idle)
    val state: StateFlow<MessageState> = _state.asStateFlow()

    private var pipelineJob: Job? = null

    fun onTyping(draft: String) {
        if (_state.value is MessageState.Idle || _state.value is MessageState.Typing) {
            _state.value = MessageState.Typing(draft)
        }
    }

    fun sendMessage(text: String) {
        val current = _state.value
        if (current is MessageState.Processing || current is MessageState.Responding) {
            pipelineJob?.cancel()
        }
        pipelineJob = scope.launch { runPipeline(text) }
    }

    fun retry() {
        val error = _state.value as? MessageState.Error ?: return
        pipelineJob?.cancel()
        pipelineJob = scope.launch { runPipeline(error.text) }
    }

    fun reset() {
        pipelineJob?.cancel()
        _state.value = MessageState.Idle
    }

    private suspend fun runPipeline(text: String) {
        _state.value = MessageState.Validating(text)
        delay(200)

        if (text.isBlank()) {
            _state.value = MessageState.Error(text, "Message cannot be empty")
            return
        }

        _state.value = MessageState.Processing(text)
        try {
            withTimeout(PROCESSING_TIMEOUT_MS) {
                val response = simulateAiResponse(text)
                _state.value = MessageState.Responding(text, response)
                delay(800)
                _state.value = MessageState.Idle
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            _state.value = MessageState.Error(text, "Request timed out. Please retry.")
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.value = MessageState.Error(text, e.message ?: "Unknown error")
        }
    }

    private suspend fun simulateAiResponse(text: String): String {
        delay(responseDelayMs)
        return "Aura response to: \"$text\""
    }
}
