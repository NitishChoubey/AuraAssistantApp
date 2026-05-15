package com.nitish.auraassistant.domain.repository



import com.nitish.auraassistant.data.local.datastore.UserPreferencesDataStore
import com.nitish.auraassistant.domain.model.UserProfile
import com.nitish.auraassistant.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore
) : UserRepository {

    override fun getUserProfile(): Flow<UserProfile> =
        dataStore.userProfileFlow

    override suspend fun saveUserProfile(profile: UserProfile) =
        dataStore.saveUserProfile(profile)

    override suspend fun isOnboardingComplete(): Boolean =
        dataStore.userProfileFlow.first().isOnboardingComplete
}