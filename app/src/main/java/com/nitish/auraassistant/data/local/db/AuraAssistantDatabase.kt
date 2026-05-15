package com.nitish.auraassistant.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nitish.auraassistant.data.local.db.converter.MessageMetaConverter
import com.nitish.auraassistant.data.local.db.dao.ChatMessageDao
import com.nitish.auraassistant.data.local.db.dao.ReminderDao
import com.nitish.auraassistant.data.local.db.dao.UserProfileDao
import com.nitish.auraassistant.data.local.db.entity.ChatMessageEntity
import com.nitish.auraassistant.data.local.db.entity.ReminderEntity
import com.nitish.auraassistant.data.local.db.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        ChatMessageEntity::class,
        ReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(MessageMetaConverter::class)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun reminderDao(): ReminderDao
}