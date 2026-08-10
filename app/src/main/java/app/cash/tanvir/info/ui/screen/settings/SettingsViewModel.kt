package app.cash.tanvir.info.ui.screen.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.data.local.preferences.AppTheme
import app.cash.tanvir.info.domain.model.Denomination
import app.cash.tanvir.info.domain.model.DenominationRow
import app.cash.tanvir.info.domain.model.DownloadedUpdate
import app.cash.tanvir.info.domain.model.ReleaseChangelog
import app.cash.tanvir.info.domain.model.Sheet
import app.cash.tanvir.info.domain.model.UpdateManifest
import app.cash.tanvir.info.domain.repository.SettingsRepository
import app.cash.tanvir.info.domain.repository.SheetRepository
import app.cash.tanvir.info.domain.repository.UpdateRepository
import app.cash.tanvir.info.util.report.StorageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/**
 * OTA update state machine (see ota.md §11).
 */
enum class UpdateStatus {
    IDLE,             // nothing happened yet
    CHECKING,         // manifest fetch in flight
    UP_TO_DATE,       // manifest fetched, installed is newest
    UPDATE_AVAILABLE, // manifest fetched, newer version exists (dialog ready)
    DOWNLOADING,      // APK streaming; downloadProgress in 0f..1f
    DOWNLOAD_READY,   // APK fully downloaded; ready to install
    INSTALLING,       // install intent launched (transient)
    ERROR             // check or download failed; updateErrorType populated
}

enum class UpdateErrorType {
    CHECK_FAILED,
    DOWNLOAD_FAILED
}

enum class ChangelogStatus {
    IDLE,       // not loaded yet (fetched lazily on first expand)
    LOADING,
    LOADED,
    ERROR
}

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val disabledDenominations: Set<Int> = emptySet(),
    val showResetConfirmationDialog: Boolean = false,
    val showRestoreWarningDialog: Boolean = false,
    val pendingRestoreUri: Uri? = null,
    val statusMessage: String? = null,
    val biometricEnabled: Boolean = false,
    val screenshotBlockEnabled: Boolean = false,
    val hapticFeedbackEnabled: Boolean = false,
    val hapticFeedbackIntensity: Float = 0.5f,
    val updateStatus: UpdateStatus = UpdateStatus.IDLE,
    val updateManifest: UpdateManifest? = null,
    val downloadedUpdate: DownloadedUpdate? = null,  // set on DOWNLOAD_READY
    val downloadProgress: Float = 0f,        // 0f..1f; -1f sentinel for indeterminate
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,              // -1 when the server omits Content-Length
    val updateErrorType: UpdateErrorType? = null,
    val updateErrorReason: String? = null,   // raw exception message (download failures)
    val isUpdateDialogVisible: Boolean = false,
    val changelogStatus: ChangelogStatus = ChangelogStatus.IDLE,
    val changelog: List<ReleaseChangelog> = emptyList()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val sheetRepository: SheetRepository,
    private val updateRepository: UpdateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var downloadJob: Job? = null

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
        viewModelScope.launch {
            settingsRepository.getBiometricEnabled().collect { enabled ->
                _uiState.update { it.copy(biometricEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getScreenshotBlockEnabled().collect { enabled ->
                _uiState.update { it.copy(screenshotBlockEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getHapticFeedbackEnabled().collect { enabled ->
                _uiState.update { it.copy(hapticFeedbackEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getHapticFeedbackIntensity().collect { intensity ->
                _uiState.update { it.copy(hapticFeedbackIntensity = intensity) }
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

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBiometricEnabled(enabled)
        }
    }

    fun setScreenshotBlockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setScreenshotBlockEnabled(enabled)
        }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHapticFeedbackEnabled(enabled)
        }
    }

    fun setHapticFeedbackIntensity(intensity: Float) {
        viewModelScope.launch {
            settingsRepository.setHapticFeedbackIntensity(intensity)
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
                    sheetObj.put("remark", sheet.remark)
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

                    val remark = sheetObj.optString("remark", "")
                    val sheet = Sheet(
                        id = id,
                        name = name,
                        rows = rows,
                        grandTotal = grandTotal,
                        totalPieces = totalPieces,
                        activeDenominations = activeDenom,
                        createdAt = createdAt,
                        updatedAt = updatedAt,
                        remark = remark
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

    /**
     * Fetches the remote manifest and compares against the installed version code.
     * @param installedCode the installed versionCode (computed in the screen, which
     *                      already owns the version pair)
     * @param fromManualCheck false for the launch auto-check (silent failure/up-to-date)
     */
    fun checkForUpdate(installedCode: Long, fromManualCheck: Boolean = true) {
        if (_uiState.value.updateStatus == UpdateStatus.CHECKING) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    updateStatus = UpdateStatus.CHECKING,
                    updateErrorType = null,
                    updateErrorReason = null,
                    updateManifest = null
                )
            }
            // Small artificial delay so the CHECKING state is perceptible (modern feel)
            delay(2000)
            val manifest = updateRepository.fetchManifest()
            _uiState.update { state ->
                when {
                    manifest == null -> state.copy(
                        updateStatus = UpdateStatus.ERROR,
                        updateErrorType = UpdateErrorType.CHECK_FAILED,
                        isUpdateDialogVisible = fromManualCheck
                    )
                    manifest.versionCode <= installedCode -> state.copy(
                        updateStatus = UpdateStatus.UP_TO_DATE,
                        isUpdateDialogVisible = false
                    )
                    else -> state.copy(
                        updateStatus = UpdateStatus.UPDATE_AVAILABLE,
                        updateManifest = manifest,
                        isUpdateDialogVisible = true
                    )
                }
            }
        }
    }

    /** Re-opens the update dialog when an update is already known (inline "Update now"). */
    fun showUpdateDialog() {
        _uiState.update {
            if (it.updateManifest != null) it.copy(isUpdateDialogVisible = true) else it
        }
    }

    fun downloadUpdate() {
        if (_uiState.value.updateStatus != UpdateStatus.UPDATE_AVAILABLE) return
        val manifest = _uiState.value.updateManifest ?: return
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    updateStatus = UpdateStatus.DOWNLOADING,
                    downloadProgress = 0f,
                    updateErrorType = null,
                    updateErrorReason = null
                )
            }
            try {
                val update = updateRepository.downloadApk(manifest) { downloaded, total ->
                    val progress = if (total > 0) downloaded.toFloat() / total.toFloat() else -1f
                    _uiState.update { s ->
                        if (s.updateStatus == UpdateStatus.DOWNLOADING) {
                            s.copy(
                                downloadProgress = progress,
                                downloadedBytes = downloaded,
                                totalBytes = total
                            )
                        } else {
                            s
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        updateStatus = UpdateStatus.DOWNLOAD_READY,
                        downloadProgress = 1f,
                        downloadedUpdate = update
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        updateStatus = UpdateStatus.ERROR,
                        updateErrorType = UpdateErrorType.DOWNLOAD_FAILED,
                        updateErrorReason = e.message,
                        downloadProgress = 0f,
                        downloadedBytes = 0L,
                        totalBytes = 0L
                    )
                }
            }
        }
    }

    /** Cancels an in-flight download and clears the dialog (rollback handled in the repo). */
    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        _uiState.update {
            it.copy(
                updateStatus = UpdateStatus.IDLE,
                isUpdateDialogVisible = false,
                downloadProgress = 0f,
                downloadedBytes = 0L,
                totalBytes = 0L,
                downloadedUpdate = null
            )
        }
    }

    /**
     * "Later"/back/tap-outside for the update dialog. A running download is cancelled.
     */
    fun dismissUpdateDialog() {
        if (_uiState.value.updateStatus == UpdateStatus.DOWNLOADING) {
            cancelDownload()
            return
        }
        _uiState.update {
            it.copy(
                isUpdateDialogVisible = false,
                updateStatus = if (it.updateStatus == UpdateStatus.DOWNLOADING) it.updateStatus else UpdateStatus.IDLE
            )
        }
    }

    /** Called by the screen right before the system installer intent fires. */
    fun onInstallLaunched() {
        _uiState.update { it.copy(updateStatus = UpdateStatus.INSTALLING) }
    }

    /**
     * Fetches the remote changelog (all releases, newest first).
     * Fetches once per session; [force] reloads after a failure.
     */
    fun loadChangelog(force: Boolean = false) {
        val state = _uiState.value
        if (state.changelogStatus == ChangelogStatus.LOADING) return
        if (!force && state.changelogStatus == ChangelogStatus.LOADED) return
        viewModelScope.launch {
            _uiState.update { it.copy(changelogStatus = ChangelogStatus.LOADING) }
            val changelog = updateRepository.fetchReleaseChangelogs()
            _uiState.update {
                if (changelog.isEmpty()) {
                    it.copy(changelogStatus = ChangelogStatus.ERROR)
                } else {
                    it.copy(changelogStatus = ChangelogStatus.LOADED, changelog = changelog)
                }
            }
        }
    }
}
