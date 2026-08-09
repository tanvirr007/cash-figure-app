package app.cash.tanvir.info.ui.screen.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.data.local.preferences.AppTheme
import app.cash.tanvir.info.domain.model.Denomination
import app.cash.tanvir.info.domain.model.DenominationRow
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
    val showRestoreWarningDialog: Boolean = false,
    val pendingRestoreUri: Uri? = null,
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

    fun onRestoreFileSelected(uri: Uri) {
        _uiState.update { it.copy(pendingRestoreUri = uri, showRestoreWarningDialog = true) }
    }

    fun dismissRestoreDialog() {
        _uiState.update { it.copy(showRestoreWarningDialog = false, pendingRestoreUri = null) }
    }

    fun confirmRestore(context: Context) {
        val uri = uiState.value.pendingRestoreUri
        _uiState.update { it.copy(showRestoreWarningDialog = false, pendingRestoreUri = null) }
        if (uri != null) {
            restoreDataFromUri(context, uri)
        }
    }

    fun backupData(context: Context) {
        viewModelScope.launch {
            try {
                val sheets = sheetRepository.getAllSheets().first()
                val backupObj = JSONObject()
                backupObj.put("version", 1)
                backupObj.put("timestamp", System.currentTimeMillis())

                // Backup App Settings
                val settingsObj = JSONObject().apply {
                    put("theme", uiState.value.theme.name)
                    put("language", uiState.value.language.name)
                    val disabledArray = JSONArray()
                    uiState.value.disabledDenominations.forEach { disabledArray.put(it) }
                    put("disabledDenominations", disabledArray)
                }
                backupObj.put("settings", settingsObj)

                // Backup Calculation Sheets
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

                // Restore Settings if present in JSON
                val settingsObj = backupObj.optJSONObject("settings")
                if (settingsObj != null) {
                    val themeStr = settingsObj.optString("theme", "SYSTEM")
                    val langStr = settingsObj.optString("language", "ENGLISH")
                    val restoredTheme = try { AppTheme.valueOf(themeStr) } catch (_: Exception) { AppTheme.SYSTEM }
                    val restoredLang = try { AppLanguage.valueOf(langStr) } catch (_: Exception) { AppLanguage.ENGLISH }

                    val disabledArray = settingsObj.optJSONArray("disabledDenominations")
                    val restoredDisabled = mutableSetOf<Int>()
                    if (disabledArray != null) {
                        for (i in 0 until disabledArray.length()) {
                            val denomVal = disabledArray.optInt(i, -1)
                            if (denomVal != -1) restoredDisabled.add(denomVal)
                        }
                    }
                    settingsRepository.restoreSettings(restoredTheme, restoredLang, restoredDisabled)
                }

                // Restore Calculation Sheets
                val sheetsArray = backupObj.optJSONArray("sheets") ?: JSONArray()
                val restoredSheets = mutableListOf<Sheet>()
                for (i in 0 until sheetsArray.length()) {
                    val sheetObj = sheetsArray.getJSONObject(i)
                    val id = sheetObj.optLong("id", 0L)
                    val name = sheetObj.optString("name", "")
                    val grandTotal = sheetObj.optLong("grandTotal", 0L)
                    val totalPieces = sheetObj.optLong("totalPieces", 0L)
                    val activeDenom = sheetObj.optInt("activeDenominations", 0)
                    val createdAt = sheetObj.optLong("createdAt", System.currentTimeMillis())
                    val updatedAt = sheetObj.optLong("updatedAt", System.currentTimeMillis())

                    val quantitiesObj = sheetObj.optJSONObject("quantities") ?: JSONObject()
                    val quantitiesMap = mutableMapOf<Int, Long>()
                    quantitiesObj.keys().forEach { k ->
                        val v = k.toIntOrNull()
                        if (v != null) {
                            val q = quantitiesObj.optString(k, "0").toLongOrNull() ?: 0L
                            quantitiesMap[v] = q
                        }
                    }

                    val rows = Denomination.ALL.map { denom ->
                        val q = quantitiesMap[denom.value] ?: 0L
                        DenominationRow(
                            denomination = denom,
                            quantity = q,
                            total = denom.value.toLong() * q
                        )
                    }

                    val sheet = Sheet(
                        id = id,
                        name = name,
                        rows = rows,
                        grandTotal = grandTotal,
                        totalPieces = totalPieces,
                        activeDenominations = activeDenom,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )
                    restoredSheets.add(sheet)
                }

                sheetRepository.restoreSheets(restoredSheets)
                _uiState.update { it.copy(statusMessage = "Successfully restored ${restoredSheets.size} item(s) from backup (v$version)") }
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "Failed to restore backup: ${e.message}") }
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
