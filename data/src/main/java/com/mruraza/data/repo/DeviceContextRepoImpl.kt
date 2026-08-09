package com.mruraza.data.repo

import com.mruraza.common.model.DeviceContext
import com.mruraza.common.repo.DeviceContextRepo
import com.mruraza.data.dao.DeviceContextDao
import com.mruraza.data.entity.DeviceContextEntity
import javax.inject.Inject

class DeviceContextRepoImpl @Inject constructor(
    private val deviceContextDao: DeviceContextDao
) : DeviceContextRepo {
    override suspend fun getDeviceContext(): DeviceContext {
        return deviceContextDao.getDeviceContext().toDomain()
    }

    override suspend fun insertOrUpdateDeviceContext(deviceContext: DeviceContext) {
        deviceContextDao.insertOrUpdateDeviceContext(DeviceContextEntity.fromDomain(deviceContext))
    }

    override suspend fun eraseDeviceContext() {
        deviceContextDao.eraseDeviceContext()
    }

}