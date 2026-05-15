package com.nitish.auraassistant.domain.repository



import com.nitish.auraassistant.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessagesPaged(page: Int): Flow<List<ChatMessage>>
    fun getAllMessages(): Flow<List<ChatMessage>>
    suspend fun sendMessage(message: ChatMessage): Long
    fun getTotalCount(): Flow<Int>
}