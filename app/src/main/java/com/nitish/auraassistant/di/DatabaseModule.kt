package com.nitish.auraassistant.di

import android.content.Context
import androidx.room.Room
import com.nitish.auraassistant.data.local.db.AuraAssistantDatabase
import com.nitish.auraassistant.data.local.db.dao.ChatMessageDao
import com.nitish.auraassistant.data.local.db.dao.ReminderDao
import com.nitish.auraassistant.data.local.db.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAuraDatabase(@ApplicationContext context: Context): AuraDatabase {
        return Room.databaseBuilder(
            context,
            AuraDatabase::class.java,
            "aura_database"
        ).build()
    }

    @Provides
    fun provideUserProfileDao(db: AuraDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    fun provideChatMessageDao(db: AuraDatabase): ChatMessageDao = db.chatMessageDao()

    @Provides
    fun provideReminderDao(db: AuraDatabase): ReminderDao = db.reminderDao()
}