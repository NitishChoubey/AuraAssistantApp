package com.nitish.auraassistant.data.local.db.converter



import androidx.room.TypeConverter
import com.nitish.auraassistant.domain.model.MessageMeta
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MessageMetaConverter {

    @TypeConverter
    fun fromMessageMeta(meta: MessageMeta): String {
        return Json.encodeToString(meta)
    }

    @TypeConverter
    fun toMessageMeta(json: String): MessageMeta {
        return Json.decodeFromString(json)
    }
}