package com.mruraza.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mruraza.common.model.UserPreferences

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
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

    fun toDomain(): UserPreferences = UserPreferences(
        id = id,
        language = language,
        timezone = timezone,
        preferredTone = preferredTone,
        responseLength = UserPreferences.ResponseLength.valueOf(responseLength.name),
        preferences = preferences
    )

    companion object{
        fun fromDomain(userPreferences: UserPreferences): UserPreferencesEntity = UserPreferencesEntity(
            id = userPreferences.id,
            language = userPreferences.language,
            timezone = userPreferences.timezone,
            preferredTone = userPreferences.preferredTone,
            responseLength = ResponseLength.valueOf(userPreferences.responseLength.name),
            preferences = userPreferences.preferences
        )
    }
}

