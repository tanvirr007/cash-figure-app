package app.cash.tanvir.info.data.repository

import app.cash.tanvir.info.data.local.db.dao.SheetDao
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.data.local.preferences.AppTheme
import app.cash.tanvir.info.data.local.preferences.PreferencesManager
import app.cash.tanvir.info.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [SettingsRepository] using DataStore and SheetDao.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val sheetDao: SheetDao
) : SettingsRepository {

    override fun getTheme(): Flow<AppTheme> = preferencesManager.themeFlow

    override fun getLanguage(): Flow<AppLanguage> = preferencesManager.languageFlow

    override fun getDisabledDenominations(): Flow<Set<Int>> = preferencesManager.disabledDenominationsFlow

    override fun getBiometricEnabled(): Flow<Boolean> = preferencesManager.biometricEnabledFlow

    override fun getScreenshotBlockEnabled(): Flow<Boolean> = preferencesManager.screenshotBlockEnabledFlow

    override fun getHapticFeedbackEnabled(): Flow<Boolean> = preferencesManager.hapticFeedbackEnabledFlow

    override fun getHapticFeedbackIntensity(): Flow<Float> = preferencesManager.hapticFeedbackIntensityFlow

    override fun getKeepScreenOnEnabled(): Flow<Boolean> = preferencesManager.keepScreenOnEnabledFlow

    override fun getLastSuccessfulCheck(): Flow<Long?> = preferencesManager.lastSuccessfulCheckFlow

    override suspend fun setTheme(theme: AppTheme) {
        preferencesManager.setTheme(theme)
    }

    override suspend fun setLanguage(language: AppLanguage) {
        preferencesManager.setLanguage(language)
    }

    override suspend fun setDenominationEnabled(denomination: Int, enabled: Boolean) {
        preferencesManager.setDenominationEnabled(denomination, enabled)
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        preferencesManager.setBiometricEnabled(enabled)
    }

    override suspend fun setScreenshotBlockEnabled(enabled: Boolean) {
        preferencesManager.setScreenshotBlockEnabled(enabled)
    }

    override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        preferencesManager.setHapticFeedbackEnabled(enabled)
    }

    override suspend fun setHapticFeedbackIntensity(intensity: Float) {
        preferencesManager.setHapticFeedbackIntensity(intensity)
    }

    override suspend fun setKeepScreenOnEnabled(enabled: Boolean) {
        preferencesManager.setKeepScreenOnEnabled(enabled)
    }

    override suspend fun setLastSuccessfulCheck(timestamp: Long) {
        preferencesManager.setLastSuccessfulCheck(timestamp)
    }

    override suspend fun resetAllData() {
        sheetDao.clearAllHistory()
        sheetDao.hardDeleteSheet(-1L) // clear current working sheet
        sheetDao.resetAutoIncrement() // reset auto-increment so IDs restart from 1
        preferencesManager.clearAll()
    }

    override suspend fun restoreSettings(theme: AppTheme, language: AppLanguage, disabledDenominations: Set<Int>) {
        preferencesManager.setTheme(theme)
        preferencesManager.setLanguage(language)
        preferencesManager.setDisabledDenominations(disabledDenominations)
    }
}
