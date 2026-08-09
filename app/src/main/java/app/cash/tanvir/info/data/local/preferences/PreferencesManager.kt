package app.cash.tanvir.info.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
        val DISABLED_DENOMINATIONS = stringSetPreferencesKey("disabled_note_denominations")
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

    val disabledDenominationsFlow: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        prefs[Keys.DISABLED_DENOMINATIONS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
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

    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
