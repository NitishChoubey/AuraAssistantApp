package com.nitish.auraassistant.data.local.db.entity



import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nitish.auraassistant.domain.model.MessageMeta

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val message: String,
    val timestamp: Long,
    val meta: MessageMeta,          // TypeConverter handles this
    val lastSyncedAt: Long = 0L
)