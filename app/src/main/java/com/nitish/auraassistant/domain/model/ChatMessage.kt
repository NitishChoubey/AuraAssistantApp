package com.nitish.auraassistant.domain.model

data class ChatMessage (
    val id : Long = 0 ,
    val sender: String ,
    val message: String,
    val timestamp: Long = System.currentTimeMillis() ,
    val meta: MessageMeta = MessageMeta()
)
