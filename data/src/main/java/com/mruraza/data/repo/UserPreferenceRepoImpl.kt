package com.mruraza.data.repo

import com.mruraza.common.model.UserPreferences
import com.mruraza.common.repo.UserPreferenceRepo
import com.mruraza.data.dao.UserPreferencesDao
import com.mruraza.data.entity.UserPreferencesEntity
import javax.inject.Inject

class UserPreferenceRepoImpl @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao
) : UserPreferenceRepo {
    override suspend fun insertOrUpdateUserPreferences(userPreferences: UserPreferences) {
        userPreferencesDao.insertOrUpdateUserPreferences(
            UserPreferencesEntity.fromDomain(
                userPreferences
            )
        )
    }

    override suspend fun getUserPreferences(): UserPreferences {
        return userPreferencesDao.getUserPreferences().toDomain()
    }

    override suspend fun eraseUserPreferences() {
        userPreferencesDao.eraseUserPreferences()
    }

}