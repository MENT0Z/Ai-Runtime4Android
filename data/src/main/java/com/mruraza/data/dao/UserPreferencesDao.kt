package com.mruraza.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mruraza.data.entity.UserPreferencesEntity

@Dao
interface UserPreferencesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserPreferences(userPreferences: UserPreferencesEntity)

    @Query("SELECT * FROM user_preferences")
    suspend fun getUserPreferences(): UserPreferencesEntity

    @Query("DELETE FROM user_preferences")
    suspend fun eraseUserPreferences()
}