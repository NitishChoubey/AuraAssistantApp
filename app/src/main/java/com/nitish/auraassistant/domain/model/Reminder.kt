package com.nitish.auraassistant.domain.model

data class Reminder (

    val id: Long = 0 ,
    val title: String ,
    val triggerAt: Long ,
    val isCompleted: Boolean = false ,
    val lastSyncedAt: Long = 0L
)
