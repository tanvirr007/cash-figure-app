package app.cash.tanvir.info.ui.screen.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.data.local.preferences.AppTheme
import app.cash.tanvir.info.domain.model.Sheet
import app.cash.tanvir.info.domain.repository.SettingsRepository
import app.cash.tanvir.info.domain.repository.SheetRepository
import app.cash.tanvir.info.util.report.StorageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val disabledDenominations: Set<Int> = emptySet(),
    val showResetConfirmationDialog: Boolean = false,
    val statusMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val sheetRepository: SheetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getTheme().collect { theme ->
                _uiState.update { it.copy(theme = theme) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getLanguage().collect { lang ->
                _uiState.update { it.copy(language = lang) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getDisabledDenominations().collect { disabled ->
                _uiState.update { it.copy(disabledDenominations = disabled) }
            }
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
        }
    }

    fun toggleDenomination(denomination: Int, enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDenominationEnabled(denomination, enabled)
        }
    }

    fun openResetDialog() {
        _uiState.update { it.copy(showResetConfirmationDialog = true) }
    }

    fun dismissResetDialog() {
        _uiState.update { it.copy(showResetConfirmationDialog = false) }
    }

    fun confirmResetAllData() {
        viewModelScope.launch {
            settingsRepository.resetAllData()
            _uiState.update {
                it.copy(
                    showResetConfirmationDialog = false,
                    statusMessage = "All data and settings have been reset."
                )
            }
        }
    }

    fun backupData(context: Context) {
        viewModelScope.launch {
            try {
                val sheets = sheetRepository.getAllSheets().first()
                val backupObj = JSONObject()
                backupObj.put("version", 1)
                backupObj.put("timestamp", System.currentTimeMillis())

                val sheetsArray = JSONArray()
                sheets.forEach { sheet ->
                    val sheetObj = JSONObject()
                    sheetObj.put("id", sheet.id)
                    sheetObj.put("name", sheet.name)
                    sheetObj.put("grandTotal", sheet.grandTotal)
                    sheetObj.put("totalPieces", sheet.totalPieces)
                    sheetObj.put("activeDenominations", sheet.activeDenominations)
                    sheetObj.put("createdAt", sheet.createdAt)
                    sheetObj.put("updatedAt", sheet.updatedAt)

                    val quantitiesObj = JSONObject()
                    sheet.rows.forEach { row ->
                        quantitiesObj.put(row.denomination.value.toString(), row.quantity.toString())
                    }
                    sheetObj.put("quantities", quantitiesObj)
                    sheetsArray.put(sheetObj)
                }

                backupObj.put("sheets", sheetsArray)
                val jsonBytes = backupObj.toString(2).toByteArray(Charsets.UTF_8)

                val fileName = "CashFigure_Backup_${System.currentTimeMillis()}.json"
                val savedUri = StorageUtil.saveReportFile(context, fileName, "application/json", jsonBytes, subFolder = "backup")

                val msg = if (savedUri != null) "Backup created in Downloads/CashFigure/backup/$fileName" else "Backup failed"
                _uiState.update { it.copy(statusMessage = msg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "Error backing up data: ${e.message}") }
            }
        }
    }

    fun restoreDataFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val jsonStr = context.contentResolver.openInputStream(uri)?.use {
                    it.bufferedReader().readText()
                } ?: throw Exception("Cannot read file")

                val backupObj = JSONObject(jsonStr)
                val version = backupObj.optInt("version", 1)
                val sheetsArray = backupObj.optJSONArray("sheets") ?: JSONArray()

                var restoredCount = 0
                for (i in 0 until sheetsArray.length()) {
                    val sheetObj = sheetsArray.getJSONObject(i)
                    val name = sheetObj.optString("name", "Restored Sheet")
                    val grandTotal = sheetObj.optLong("grandTotal", 0L)
                    val totalPieces = sheetObj.optLong("totalPieces", 0L)
                    val activeDenom = sheetObj.optInt("activeDenominations", 0)

                    val quantitiesObj = sheetObj.optJSONObject("quantities") ?: JSONObject()
                    val restoredMap = mutableMapOf<Int, String>()
                    quantitiesObj.keys().forEach { k ->
                        val v = k.toIntOrNull()
                        if (v != null) restoredMap[v] = quantitiesObj.optString(k, "0")
                    }

                    sheetRepository.saveCurrentSheet(restoredMap, grandTotal, totalPieces, activeDenom)
                    restoredCount++
                }

                _uiState.update { it.copy(statusMessage = "Successfully restored $restoredCount item(s) from backup (v$version)") }
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "Failed to restore backup: ${e.message}") }
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
