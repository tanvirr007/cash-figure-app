package app.cash.tanvir.info.domain.repository

import app.cash.tanvir.info.data.local.preferences.AppFont
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.data.local.preferences.AppTheme
import kotlinx.coroutines.flow.Flow

/**
 * Interface for settings repository operations.
 */
interface SettingsRepository {
    fun getTheme(): Flow<AppTheme>
    fun getLanguage(): Flow<AppLanguage>
    fun getFont(): Flow<AppFont>
    fun getDisabledDenominations(): Flow<Set<Int>>
    fun getBiometricEnabled(): Flow<Boolean>
    fun getScreenshotBlockEnabled(): Flow<Boolean>
    fun getHapticFeedbackEnabled(): Flow<Boolean>
    fun getHapticFeedbackIntensity(): Flow<Float>
    fun getKeepScreenOnEnabled(): Flow<Boolean>
    fun getDynamicColorEnabled(): Flow<Boolean>
    fun getLastSuccessfulCheck(): Flow<Long?>
    fun getHiddenNoteSuggestions(): Flow<Set<String>>
    suspend fun hideNoteSuggestion(suggestion: String)
    suspend fun unhideNoteSuggestion(suggestion: String)
    suspend fun setTheme(theme: AppTheme)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setFont(font: AppFont)
    suspend fun setDenominationEnabled(denomination: Int, enabled: Boolean)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setScreenshotBlockEnabled(enabled: Boolean)
    suspend fun setHapticFeedbackEnabled(enabled: Boolean)
    suspend fun setHapticFeedbackIntensity(intensity: Float)
    suspend fun setKeepScreenOnEnabled(enabled: Boolean)
    suspend fun setDynamicColorEnabled(enabled: Boolean)
    suspend fun setLastSuccessfulCheck(timestamp: Long)
    suspend fun resetAllData()
    suspend fun restoreSettings(theme: AppTheme, language: AppLanguage, disabledDenominations: Set<Int>)
}
