package com.mruraza.runtime_core.Context.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.mruraza.common.model.DeviceContext
import com.mruraza.common.repo.DeviceContextRepo
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.TimeZone
import javax.inject.Inject

/**
 * Responsible for collecting the current device context and keeping it
 * persisted through [DeviceContextRepo].
 *
 * The manager is intentionally the single place where Android-specific
 * device information is mapped into the domain [DeviceContext] model.
 */
class DeviceContextManager @Inject constructor(
    private val deviceContextRepo: DeviceContextRepo,
    @ApplicationContext private val context: Context,
) {

    /**
     * Returns the currently persisted device context.
     *
     * If no context has been persisted yet, the current device context is
     * collected and persisted before being returned.
     */
    suspend fun getDeviceContext(): DeviceContext {
        return deviceContextRepo.getDeviceContext()
    }

    /**
     * Collects the current device information and persists it.
     *
     * This should be called when the application starts and whenever the
     * device/application context may have changed.
     */
    suspend fun collectAndSaveDeviceContext(): DeviceContext {
        val deviceContext = collectDeviceContext()

        deviceContextRepo.insertOrUpdateDeviceContext(deviceContext)

        return deviceContext
    }

    /**
     * Persists the supplied device context.
     */
    suspend fun saveDeviceContext(deviceContext: DeviceContext) {
        deviceContextRepo.insertOrUpdateDeviceContext(deviceContext)
    }

    /**
     * Removes the persisted device context.
     */
    suspend fun eraseDeviceContext() {
        deviceContextRepo.eraseDeviceContext()
    }

    /**
     * Collects device information from the current Android environment.
     */
    private fun collectDeviceContext(): DeviceContext {
        return DeviceContext(
            deviceType = resolveDeviceType(),
            os = DeviceContext.OSType.ANDROID,
            appVersion = resolveAppVersion(),
            locale = resolveLocale(),
            timezone = resolveTimezone(),
        )
    }

    /**
     * Determines the device category from the Android configuration.
     *
     * Android currently runs on phones and tablets for the supported device
     * categories. Other values are retained in the domain model for future
     * platforms and should not be guessed here.
     */
    private fun resolveDeviceType(): DeviceContext.DeviceType {
        val screenLayout = context.resources.configuration.screenLayout
        val screenSize = screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK

        return when (screenSize) {
            Configuration.SCREENLAYOUT_SIZE_LARGE,
            Configuration.SCREENLAYOUT_SIZE_XLARGE -> {
                DeviceContext.DeviceType.TABLET
            }

            else -> {
                DeviceContext.DeviceType.PHONE
            }
        }
    }

    /**
     * Returns the application version.
     *
     * VersionName is preferred because it is the human-readable application
     * version normally used for analytics, diagnostics, and backend context.
     */
    private fun resolveAppVersion(): String? {
        return runCatching {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
                ?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    /**
     * Returns the current application locale using BCP-47 language tags.
     *
     * Examples:
     * - en-US
     * - en-GB
     * - ne-NP
     */
    private fun resolveLocale(): String? {
        return runCatching {
            val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale
            }

            locale.toLanguageTag().takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    /**
     * Returns the current system timezone identifier.
     *
     * Examples:
     * - Asia/Kathmandu
     * - America/New_York
     * - Europe/London
     */
    private fun resolveTimezone(): String? {
        return runCatching {
            TimeZone.getDefault().id.takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}