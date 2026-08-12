package app.cash.tanvir.info.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cash_figure_prefs")

enum class AppTheme { SYSTEM, LIGHT, DARK }
enum class AppLanguage { ENGLISH, BANGLA }
enum class AppFont { DEFAULT, GOOGLE_SANS_ROUNDED, GOOGLE_SANS_FLEX, VOLTE_ROUND }

/**
 * DataStore preferences manager for persistent user settings.
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("app_theme")
        val LANGUAGE = stringPreferencesKey("app_language")
        val FONT = stringPreferencesKey("app_font")
        val DISABLED_DENOMINATIONS = stringSetPreferencesKey("disabled_note_denominations")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val SCREENSHOT_BLOCK_ENABLED = booleanPreferencesKey("screenshot_block_enabled")
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val HAPTIC_FEEDBACK_INTENSITY = floatPreferencesKey("haptic_feedback_intensity")
        val KEEP_SCREEN_ON_ENABLED = booleanPreferencesKey("keep_screen_on_enabled")
        val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")
        val LAST_KNOWN_VERSION = longPreferencesKey("last_known_version")
        val LAST_SUCCESSFUL_CHECK = longPreferencesKey("last_successful_check")
    }

    val themeFlow: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.THEME]) {
            "LIGHT" -> AppTheme.LIGHT
            "DARK" -> AppTheme.DARK
            else -> AppTheme.SYSTEM
        }
    }

    val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.LANGUAGE]) {
            "BANGLA" -> AppLanguage.BANGLA
            else -> AppLanguage.ENGLISH
        }
    }

    val fontFlow: Flow<AppFont> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.FONT]) {
            "GOOGLE_SANS_ROUNDED" -> AppFont.GOOGLE_SANS_ROUNDED
            "GOOGLE_SANS_FLEX" -> AppFont.GOOGLE_SANS_FLEX
            "VOLTE_ROUND" -> AppFont.VOLTE_ROUND
            else -> AppFont.DEFAULT
        }
    }

    val disabledDenominationsFlow: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        prefs[Keys.DISABLED_DENOMINATIONS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    val biometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.BIOMETRIC_ENABLED] ?: false
    }

    val screenshotBlockEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SCREENSHOT_BLOCK_ENABLED] ?: false
    }

    val hapticFeedbackEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.HAPTIC_FEEDBACK_ENABLED] ?: false
    }

    val hapticFeedbackIntensityFlow: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[Keys.HAPTIC_FEEDBACK_INTENSITY] ?: 0.5f
    }

    val keepScreenOnEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.KEEP_SCREEN_ON_ENABLED] ?: true
    }

    val dynamicColorEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DYNAMIC_COLOR_ENABLED] ?: true
    }

    val lastKnownVersionFlow: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_KNOWN_VERSION]
    }

    val lastSuccessfulCheckFlow: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_SUCCESSFUL_CHECK]
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME] = theme.name
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.name
        }
    }

    suspend fun setFont(font: AppFont) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FONT] = font.name
        }
    }

    suspend fun setDenominationEnabled(denomination: Int, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.DISABLED_DENOMINATIONS]?.toMutableSet() ?: mutableSetOf()
            if (enabled) {
                current.remove(denomination.toString())
            } else {
                current.add(denomination.toString())
            }
            prefs[Keys.DISABLED_DENOMINATIONS] = current
        }
    }

    suspend fun setDisabledDenominations(disabled: Set<Int>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DISABLED_DENOMINATIONS] = disabled.map { it.toString() }.toSet()
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setScreenshotBlockEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SCREENSHOT_BLOCK_ENABLED] = enabled
        }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }

    suspend fun setHapticFeedbackIntensity(intensity: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTIC_FEEDBACK_INTENSITY] = intensity
        }
    }

    suspend fun setKeepScreenOnEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.KEEP_SCREEN_ON_ENABLED] = enabled
        }
    }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DYNAMIC_COLOR_ENABLED] = enabled
        }
    }

    suspend fun setLastKnownVersion(versionCode: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_KNOWN_VERSION] = versionCode
        }
    }

    suspend fun setLastSuccessfulCheck(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_SUCCESSFUL_CHECK] = timestamp
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
