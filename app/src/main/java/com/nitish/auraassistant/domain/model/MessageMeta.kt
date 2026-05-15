package com.nitish.auraassistant.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MessageMeta (
    val isRead: Boolean = false ,
    val reactionEmoji: String? = null ,
    val retryCount:  Int = 0 ,
    val modelVersion: String = "aura-v1"
)
