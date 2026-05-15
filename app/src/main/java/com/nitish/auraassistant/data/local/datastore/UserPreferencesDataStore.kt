package com.nitish.auraassistant.data.local.datastore



import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.nitish.auraassistant.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataStore(private val context: Context) {

    companion object {
        val KEY_NAME = stringPreferencesKey("name")
        val KEY_AGE = stringPreferencesKey("age")
        val KEY_PHONE = stringPreferencesKey("phone")
        val KEY_TRAITS = stringPreferencesKey("personality_traits")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_complete")
    }

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { prefs ->
            UserProfile(
                name = prefs[KEY_NAME] ?: "",
                age = prefs[KEY_AGE] ?: "",
                phone = prefs[KEY_PHONE] ?: "",
                personalityTraits = prefs[KEY_TRAITS]
                    ?.split(",")
                    ?.filter { it.isNotBlank() }
                    ?: emptyList(),
                isOnboardingComplete = prefs[KEY_ONBOARDING_DONE] ?: false
            )
        }

    suspend fun saveUserProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NAME] = profile.name
            prefs[KEY_AGE] = profile.age
            prefs[KEY_PHONE] = profile.phone
            prefs[KEY_TRAITS] = profile.personalityTraits.joinToString(",")
            prefs[KEY_ONBOARDING_DONE] = profile.isOnboardingComplete
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}