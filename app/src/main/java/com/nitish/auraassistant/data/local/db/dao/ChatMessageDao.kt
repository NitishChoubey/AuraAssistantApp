package com.nitish.auraassistant.data.local.db.dao



import androidx.room.*
import com.nitish.auraassistant.data.local.db.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    // Pagination — load 20 at a time, offset by page
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getMessagesPaged(limit: Int = 20, offset: Int = 0): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Delete
    suspend fun deleteMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages WHERE lastSyncedAt > :since")
    suspend fun getChangedSince(since: Long): List<ChatMessageEntity>

    @Query("SELECT COUNT(*) FROM chat_messages")
    fun getTotalCount(): Flow<Int>
}