package com.mruraza.data.entity

import androidx.room.Entity

@Entity(tableName = "device_context")
data class DeviceContextEntity(
    val deviceType: DeviceType = DeviceType.PHONE,
    val os: OSType = OSType.ANDROID,
    val appVersion: String? = null,
    val locale: String? = null,
    val timezone: String? = null
) {

    enum class DeviceType {
        PHONE,
        TABLET,
        DESKTOP,
        WEB,
        OTHER
    }

    enum class OSType {
        ANDROID,
        IOS,
        WINDOWS,
        MAC,
        LINUX,
        OTHER
    }
}