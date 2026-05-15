package com.nitish.auraassistant.data.repository

import com.nitish.auraassistant.data.local.db.dao.ChatMessageDao
import com.nitish.auraassistant.data.local.db.entity.ChatMessageEntity
import com.nitish.auraassistant.domain.model.ChatMessage
import com.nitish.auraassistant.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val dao: ChatMessageDao
) : ChatRepository {

    override fun getMessagesPaged(page: Int): Flow<List<ChatMessage>> {
        return dao.getMessagesPaged(
            limit = 20,
            offset = page * 20
        ).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getAllMessages(): Flow<List<ChatMessage>> =
        dao.getAllMessages().map { it.map { entity -> entity.toDomain() } }

    override suspend fun sendMessage(message: ChatMessage): Long =
        dao.insertMessage(message.toEntity())

    override fun getTotalCount(): Flow<Int> = dao.getTotalCount()

    // ── Mappers ──────────────────────────────────────────
    private fun ChatMessageEntity.toDomain() = ChatMessage(
        id = id,
        sender = sender,
        message = message,
        timestamp = timestamp,
        meta = meta
    )

    private fun ChatMessage.toEntity() = ChatMessageEntity(
        id = id,
        sender = sender,
        message = message,
        timestamp = timestamp,
        meta = meta
    )
}