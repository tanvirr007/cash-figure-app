package app.cash.tanvir.info.domain.repository

import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.data.local.preferences.AppTheme
import kotlinx.coroutines.flow.Flow

/**
 * Interface for settings repository operations.
 */
interface SettingsRepository {
    fun getTheme(): Flow<AppTheme>
    fun getLanguage(): Flow<AppLanguage>
    fun getDisabledDenominations(): Flow<Set<Int>>
    fun getBiometricEnabled(): Flow<Boolean>
    fun getScreenshotBlockEnabled(): Flow<Boolean>
    fun getHapticFeedbackEnabled(): Flow<Boolean>
    fun getHapticFeedbackIntensity(): Flow<Float>
    suspend fun setTheme(theme: AppTheme)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setDenominationEnabled(denomination: Int, enabled: Boolean)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setScreenshotBlockEnabled(enabled: Boolean)
    suspend fun setHapticFeedbackEnabled(enabled: Boolean)
    suspend fun setHapticFeedbackIntensity(intensity: Float)
    suspend fun resetAllData()
    suspend fun restoreSettings(theme: AppTheme, language: AppLanguage, disabledDenominations: Set<Int>)
}
