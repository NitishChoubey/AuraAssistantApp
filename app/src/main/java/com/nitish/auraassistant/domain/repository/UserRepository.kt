package com.nitish.auraassistant.domain.repository



import com.nitish.auraassistant.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(): Flow<UserProfile>
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun isOnboardingComplete(): Boolean
}