package com.mruraza.common.model

data class DeviceContext(
    val deviceType: DeviceType,
    val os: String? = null,
    val appVersion: String? = null,
    val locale: String? = null,
    val timezone: String? = null
)

enum class DeviceType {
    PHONE,
    TABLET,
    DESKTOP,
    WEB,
    OTHER
}
