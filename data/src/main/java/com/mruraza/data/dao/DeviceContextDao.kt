package com.mruraza.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mruraza.data.entity.DeviceContextEntity

@Dao
interface DeviceContextDao {
    @Query("SELECT * FROM device_context")
    suspend fun getDeviceContext(): DeviceContextEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDeviceContext(deviceContext: DeviceContextEntity)

    @Query("DELETE FROM device_context")
    suspend fun eraseDeviceContext()
}