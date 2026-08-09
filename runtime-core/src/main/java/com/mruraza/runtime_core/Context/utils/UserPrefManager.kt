package com.mruraza.runtime_core.Context.utils

import com.mruraza.common.model.UserPreferences
import com.mruraza.common.repo.UserPreferenceRepo

import javax.inject.Inject

/**
 * Manages user preferences and delegates persistence to [UserPreferenceRepo].
 *
 * The manager provides the application-facing API for reading, updating,
 * and clearing user preferences while keeping persistence details inside
 * the repository layer.
 */
class UserPreferenceManager @Inject constructor(
    private val userPreferenceRepo: UserPreferenceRepo,
) {

    /**
     * Returns the currently persisted user preferences.
     */
    suspend fun getUserPreferences(): UserPreferences {
        return userPreferenceRepo.getUserPreferences()
    }

    /**
     * Persists the supplied user preferences.
     */
    suspend fun saveUserPreferences(
        userPreferences: UserPreferences,
    ) {
        userPreferenceRepo.insertOrUpdateUserPreferences(userPreferences)
    }

    /**
     * Updates the current user preferences and persists the result.
     *
     * This allows callers to modify only the fields they care about while
     * preserving all other existing preferences.
     */
    suspend fun updateUserPreferences(
        update: UserPreferences.() -> UserPreferences,
    ): UserPreferences {
        val currentPreferences = userPreferenceRepo.getUserPreferences()
        val updatedPreferences = currentPreferences.update()

        userPreferenceRepo.insertOrUpdateUserPreferences(updatedPreferences)

        return updatedPreferences
    }

    /**
     * Updates the user's language.
     */
    suspend fun updateLanguage(
        language: String,
    ): UserPreferences {
        require(language.isNotBlank()) {
            "Language must not be blank."
        }

        return updateUserPreferences {
            copy(language = language)
        }
    }

    /**
     * Updates the user's timezone.
     */
    suspend fun updateTimezone(
        timezone: String?,
    ): UserPreferences {
        return updateUserPreferences {
            copy(timezone = timezone)
        }
    }

    /**
     * Updates the user's preferred response tone.
     */
    suspend fun updatePreferredTone(
        preferredTone: String?,
    ): UserPreferences {
        return updateUserPreferences {
            copy(preferredTone = preferredTone)
        }
    }

    /**
     * Updates the user's preferred response length.
     */
    suspend fun updateResponseLength(
        responseLength: UserPreferences.ResponseLength,
    ): UserPreferences {
        return updateUserPreferences {
            copy(responseLength = responseLength)
        }
    }

    /**
     * Adds or replaces a custom preference.
     */
    suspend fun setPreference(
        key: String,
        value: String,
    ): UserPreferences {
        require(key.isNotBlank()) {
            "Preference key must not be blank."
        }

        return updateUserPreferences {
            copy(
                preferences = preferences.toMutableMap().apply {
                    this[key] = value
                },
            )
        }
    }

    /**
     * Removes a custom preference.
     */
    suspend fun removePreference(
        key: String,
    ): UserPreferences {
        return updateUserPreferences {
            copy(
                preferences = preferences.toMutableMap().apply {
                    remove(key)
                },
            )
        }
    }

    /**
     * Removes all persisted user preferences.
     */
    suspend fun eraseUserPreferences() {
        userPreferenceRepo.eraseUserPreferences()
    }
}