package app.cash.tanvir.info.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.tanvir.info.data.local.preferences.AppFont
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.data.local.preferences.AppTheme
import app.cash.tanvir.info.data.local.preferences.PreferencesManager
import app.cash.tanvir.info.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the first-launch onboarding wizard.
 */
data class OnboardingUiState(
    val pageIndex: Int = 0,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val font: AppFont = AppFont.DEFAULT,
    val theme: AppTheme = AppTheme.SYSTEM
)

/**
 * ViewModel for the first-launch wizard.
 * Language / font / theme apply live (the wizard itself previews each choice);
 * the onboarding flag is set only when the user finishes or skips.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val language = settingsRepository.getLanguage().first()
            val font = settingsRepository.getFont().first()
            val theme = settingsRepository.getTheme().first()
            _uiState.update { it.copy(language = language, font = font, theme = theme) }
        }
    }

    fun selectLanguage(language: AppLanguage) {
        _uiState.update { it.copy(language = language) }
        viewModelScope.launch { settingsRepository.setLanguage(language) }
    }

    fun selectFont(font: AppFont) {
        _uiState.update { it.copy(font = font) }
        viewModelScope.launch { settingsRepository.setFont(font) }
    }

    fun selectTheme(theme: AppTheme) {
        _uiState.update { it.copy(theme = theme) }
        viewModelScope.launch { settingsRepository.setTheme(theme) }
    }

    fun next() {
        _uiState.update { it.copy(pageIndex = (it.pageIndex + 1).coerceAtMost(MAX_PAGE)) }
    }

    fun back() {
        _uiState.update { it.copy(pageIndex = (it.pageIndex - 1).coerceAtLeast(0)) }
    }

    /**
     * Finish the wizard (Done or Skip). Keeps whatever was selected so far
     * and marks onboarding as completed.
     */
    fun complete(onDone: () -> Unit) {
        viewModelScope.launch {
            preferencesManager.setOnboardingCompleted(true)
            onDone()
        }
    }

    companion object {
        const val MAX_PAGE = 3
    }
}
