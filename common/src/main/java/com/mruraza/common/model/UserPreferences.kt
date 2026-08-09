package com.mruraza.common.model

data class UserPreferences(
    val language: String = "en",
    val timezone: String? = null,
    val preferredTone: String? = null,
    val responseLength: ResponseLength = ResponseLength.DEFAULT,
    val preferences: Map<String, String> = emptyMap()
){
    enum class ResponseLength {
        SHORT,
        DEFAULT,
        DETAILED
    }
}

