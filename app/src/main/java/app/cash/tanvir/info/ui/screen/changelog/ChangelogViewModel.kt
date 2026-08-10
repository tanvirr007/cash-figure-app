package app.cash.tanvir.info.ui.screen.changelog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.domain.model.ReleaseChangelog
import app.cash.tanvir.info.domain.repository.SettingsRepository
import app.cash.tanvir.info.domain.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ChangelogStatus {
    IDLE,
    LOADING,
    LOADED,
    ERROR
}

data class ChangelogUiState(
    val status: ChangelogStatus = ChangelogStatus.IDLE,
    val changelog: List<ReleaseChangelog> = emptyList(),
    val isBangla: Boolean = false
)

@HiltViewModel
class ChangelogViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val updateRepository: UpdateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangelogUiState())
    val uiState: StateFlow<ChangelogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getLanguage().collect { language ->
                _uiState.update { it.copy(isBangla = language == AppLanguage.BANGLA) }
            }
        }
    }

    /**
     * Fetches the remote changelog (all releases, newest first).
     * Fetches once per session; [force] reloads after a failure.
     */
    fun loadChangelog(force: Boolean = false) {
        val state = _uiState.value
        if (state.status == ChangelogStatus.LOADING) return
        if (!force && state.status == ChangelogStatus.LOADED) return
        viewModelScope.launch {
            _uiState.update { it.copy(status = ChangelogStatus.LOADING) }
            val changelog = updateRepository.fetchReleaseChangelogs()
            _uiState.update {
                if (changelog.isEmpty()) {
                    it.copy(status = ChangelogStatus.ERROR)
                } else {
                    it.copy(status = ChangelogStatus.LOADED, changelog = changelog)
                }
            }
        }
    }
}
