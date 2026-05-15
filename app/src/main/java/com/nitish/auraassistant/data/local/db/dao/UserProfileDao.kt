package com.nitish.auraassistant.data.local.db.dao



import androidx.room.*
import com.nitish.auraassistant.data.local.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE lastSyncedAt > :since")
    suspend fun getChangedSince(since: Long): List<UserProfileEntity>
}