package com.nitish.auraassistant.statemachine

sealed class MessageState {
    object Idle : MessageState()
    data class Typing(val draft: String) : MessageState()
    data class Validating(val text: String) : MessageState()
    data class Processing(val text: String) : MessageState()
    data class Responding(val text: String, val response: String) : MessageState()
    data class Error(val text: String, val reason: String) : MessageState()
}
