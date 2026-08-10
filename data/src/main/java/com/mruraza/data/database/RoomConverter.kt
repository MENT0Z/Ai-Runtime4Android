package com.mruraza.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mruraza.data.entity.AgentSessionEntity
import com.mruraza.data.entity.ConversationTurnEntity
import com.mruraza.data.entity.DeviceContextEntity
import com.mruraza.data.entity.UserPreferencesEntity

class RoomConverters {

    @TypeConverter
    fun fromMap(value: Map<String, String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toMap(value: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun responseLengthToString(value: UserPreferencesEntity.ResponseLength): String {
        return value.name
    }

    @TypeConverter
    fun stringToResponseLength(value: String): UserPreferencesEntity.ResponseLength {
        return UserPreferencesEntity.ResponseLength.valueOf(value)
    }

    @TypeConverter
    fun deviceTypeToString(value: DeviceContextEntity.DeviceType): String {
        return value.name
    }

    @TypeConverter
    fun stringToDeviceType(value: String): DeviceContextEntity.DeviceType {
        return DeviceContextEntity.DeviceType.valueOf(value)
    }

    @TypeConverter
    fun osTypeToString(value: DeviceContextEntity.OSType): String {
        return value.name
    }

    @TypeConverter
    fun stringToOsType(value: String): DeviceContextEntity.OSType {
        return DeviceContextEntity.OSType.valueOf(value)
    }

    @TypeConverter
    fun conversationRoleToString(value: ConversationTurnEntity.ConversationRole): String {
        return value.name
    }

    @TypeConverter
    fun stringToConversationRole(value: String): ConversationTurnEntity.ConversationRole {
        return ConversationTurnEntity.ConversationRole.valueOf(value)
    }

    @TypeConverter
    fun sessionStatusToString(value: AgentSessionEntity.SessionStatus): String {
        return value.name
    }

    @TypeConverter
    fun stringToSessionStatus(value: String): AgentSessionEntity.SessionStatus {
        return AgentSessionEntity.SessionStatus.valueOf(value)
    }
}