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
    suspend fun setTheme(theme: AppTheme)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setDenominationEnabled(denomination: Int, enabled: Boolean)
    suspend fun resetAllData()
}
