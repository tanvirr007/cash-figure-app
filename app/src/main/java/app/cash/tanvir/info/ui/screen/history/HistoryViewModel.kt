package app.cash.tanvir.info.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.tanvir.info.domain.model.Sheet
import app.cash.tanvir.info.domain.repository.SheetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val searchQuery: String = "",
    val lastDeletedSheetId: Long? = null,
    val showRenameDialogForSheet: Sheet? = null,
    val showDeleteConfirmationForSheet: Sheet? = null,
    val currentLanguage: app.cash.tanvir.info.data.local.preferences.AppLanguage = app.cash.tanvir.info.data.local.preferences.AppLanguage.ENGLISH
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val sheetRepository: SheetRepository,
    private val settingsRepository: app.cash.tanvir.info.domain.repository.SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getLanguage().collect { lang ->
                _uiState.update { it.copy(currentLanguage = lang) }
            }
        }
    }

    // Observe sheets list reactively based on search query
    val sheets: StateFlow<List<Sheet>> = _uiState
        .flatMapLatest { state ->
            if (state.searchQuery.isBlank()) {
                sheetRepository.getAllSheets()
            } else {
                sheetRepository.searchSheets(state.searchQuery)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun togglePin(sheet: Sheet) {
        viewModelScope.launch {
            // Update entity with toggled isPinned state by re-saving or updating entity
            val updated = sheet.copy(updatedAt = System.currentTimeMillis())
            sheetRepository.updateSheet(updated)
        }
    }

    fun toggleFavorite(sheet: Sheet) {
        viewModelScope.launch {
            val updated = sheet.copy(updatedAt = System.currentTimeMillis())
            sheetRepository.updateSheet(updated)
        }
    }

    fun renameSheet(sheet: Sheet, newName: String) {
        viewModelScope.launch {
            val updated = sheet.copy(name = newName, updatedAt = System.currentTimeMillis())
            sheetRepository.updateSheet(updated)
            _uiState.update { it.copy(showRenameDialogForSheet = null) }
        }
    }

    fun deleteSheet(sheetId: Long) {
        viewModelScope.launch {
            sheetRepository.softDeleteSheet(sheetId)
            _uiState.update { it.copy(lastDeletedSheetId = sheetId) }
        }
    }

    fun undoDelete() {
        val deletedId = _uiState.value.lastDeletedSheetId ?: return
        viewModelScope.launch {
            sheetRepository.restoreSheet(deletedId)
            _uiState.update { it.copy(lastDeletedSheetId = null) }
        }
    }

    fun openRenameDialog(sheet: Sheet) {
        _uiState.update { it.copy(showRenameDialogForSheet = sheet) }
    }

    fun dismissRenameDialog() {
        _uiState.update { it.copy(showRenameDialogForSheet = null) }
    }

    fun openDeleteConfirmation(sheet: Sheet) {
        _uiState.update { it.copy(showDeleteConfirmationForSheet = sheet) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmationForSheet = null) }
    }

    fun confirmDeleteSheet() {
        val sheetToDelete = _uiState.value.showDeleteConfirmationForSheet ?: return
        deleteSheet(sheetToDelete.id)
        _uiState.update { it.copy(showDeleteConfirmationForSheet = null) }
    }
}
