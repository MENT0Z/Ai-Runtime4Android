package com.mruraza.common.repo

import com.mruraza.common.model.DeviceContext

interface DeviceContextRepo {
    suspend fun getDeviceContext(): DeviceContext
    suspend fun insertOrUpdateDeviceContext(deviceContext: DeviceContext)
    suspend fun eraseDeviceContext()
}