package com.nitish.auraassistant.data.local.db.entity



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val triggerAt: Long,
    val isCompleted: Boolean,
    val lastSyncedAt: Long = 0L
)