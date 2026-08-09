package com.mruraza.data.entity

import androidx.room.Entity

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    val language: String = "en",
    val timezone: String? = null,
    val preferredTone: String? = null,
    val responseLength: ResponseLength = ResponseLength.DEFAULT,
    val preferences: Map<String, String> = emptyMap()
) {
    enum class ResponseLength {
        SHORT,
        DEFAULT,
        DETAILED
    }
}

