package com.mruraza.data.entity

import androidx.room.Entity
import com.mruraza.common.model.DeviceContext

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

    fun toDomain(): DeviceContext = DeviceContext(
        deviceType = DeviceContext.DeviceType.valueOf(deviceType.name),
        os = DeviceContext.OSType.valueOf(os.name),
        appVersion = appVersion,
        locale = locale,
        timezone = timezone
    )

    companion object{
        fun fromDomain(deviceContext: DeviceContext): DeviceContextEntity = DeviceContextEntity(
            deviceType = DeviceType.valueOf(deviceContext.deviceType.name),
            os = OSType.valueOf(deviceContext.os.name),
            appVersion = deviceContext.appVersion,
            locale = deviceContext.locale,
            timezone = deviceContext.timezone
        )
    }
}