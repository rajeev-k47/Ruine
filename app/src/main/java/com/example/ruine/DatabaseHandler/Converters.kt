package com.example.ruine.DatabaseHandler

import androidx.room.TypeConverter
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromJSONArray(value: JSONArray?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toJSONArray(value: String?): JSONArray? {
        return value?.let { JSONArray(it) }
    }
}