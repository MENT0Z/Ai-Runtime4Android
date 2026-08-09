package com.mruraza.common.repo

import com.mruraza.common.model.UserPreferences

interface UserPreferenceRepo {
    suspend fun insertOrUpdateUserPreferences(userPreferences: UserPreferences)
    suspend fun getUserPreferences(): UserPreferences
    suspend fun eraseUserPreferences()
}