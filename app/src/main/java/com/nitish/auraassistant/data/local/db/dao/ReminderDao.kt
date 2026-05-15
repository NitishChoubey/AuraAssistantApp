package com.nitish.auraassistant.data.local.db.dao



import androidx.room.*
import com.nitish.auraassistant.data.local.db.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY triggerAt ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE lastSyncedAt > :since")
    suspend fun getChangedSince(since: Long): List<ReminderEntity>
}