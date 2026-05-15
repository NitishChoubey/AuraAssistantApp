package com.nitish.auraassistant.data.local.db.entity



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String,
    val age: String,
    val phone: String,
    val personalityTraits: String,   // stored as comma-separated
    val isOnboardingComplete: Boolean,
    val lastSyncedAt: Long = 0L
)