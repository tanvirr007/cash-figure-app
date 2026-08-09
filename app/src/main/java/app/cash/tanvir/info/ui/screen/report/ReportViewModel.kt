package app.cash.tanvir.info.ui.screen.report

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.domain.model.Sheet
import app.cash.tanvir.info.domain.repository.SettingsRepository
import app.cash.tanvir.info.domain.repository.SheetRepository
import app.cash.tanvir.info.util.report.CsvReportGenerator
import app.cash.tanvir.info.util.report.PdfReportGenerator
import app.cash.tanvir.info.util.report.PrintHelper
import app.cash.tanvir.info.util.report.StorageUtil
import app.cash.tanvir.info.util.report.TxtReportGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class ExportFormat { PDF, CSV, TXT }

data class ReportUiState(
    val sheet: Sheet? = null,
    val isBangla: Boolean = false,
    val exportStatusMessage: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val sheetRepository: SheetRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sheetId: Long = savedStateHandle.get<Long>("sheetId") ?: -1L
    val fromSave: Boolean = savedStateHandle.get<Boolean>("fromSave") ?: false

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getLanguage().collect { lang ->
                _uiState.update { it.copy(isBangla = lang == AppLanguage.BANGLA) }
            }
        }

        viewModelScope.launch {
            val loadedSheet = if (sheetId > 0) {
                sheetRepository.getSheetById(sheetId)
            } else {
                // If no specific sheet ID passed, load current working sheet
                sheetRepository.getCurrentSheet().let { flow ->
                    var current: Sheet? = null
                    flow.collect { current = it }
                    current
                }
            }
            _uiState.update { it.copy(sheet = loadedSheet) }
        }
    }

    fun exportReport(context: Context, format: ExportFormat) {
        val currentSheet = uiState.value.sheet ?: return
        val isBangla = uiState.value.isBangla

        val (ext, mime, data) = when (format) {
            ExportFormat.PDF -> Triple("pdf", "application/pdf", PdfReportGenerator.generatePdf(context, currentSheet, isBangla))
            ExportFormat.CSV -> Triple("csv", "text/csv", CsvReportGenerator.generateCsv(currentSheet, isBangla))
            ExportFormat.TXT -> Triple("txt", "text/plain", TxtReportGenerator.generateTxt(currentSheet, isBangla))
        }

        val fileName = StorageUtil.generateFileName(ext)
        val savedUri = StorageUtil.saveReportFile(context, fileName, mime, data, subFolder = ext)

        val message = if (savedUri != null) {
            if (isBangla) "ডাউনলোড/CashFigure/$ext/$fileName এ এক্সপোর্ট করা হয়েছে" else "Exported to Downloads/CashFigure/$ext/$fileName"
        } else {
            if (isBangla) "রিপোর্ট এক্সপোর্ট করতে ব্যর্থ হয়েছে" else "Failed to export report"
        }
        _uiState.update { it.copy(exportStatusMessage = message) }
    }

    fun printReport(context: Context) {
        val currentSheet = uiState.value.sheet ?: return
        PrintHelper.printSheet(context, currentSheet, uiState.value.isBangla)
    }

    fun shareReport(context: Context) {
        val currentSheet = uiState.value.sheet ?: return
        val isBangla = uiState.value.isBangla
        val pdfData = PdfReportGenerator.generatePdf(context, currentSheet, isBangla)

        val cacheDir = File(context.cacheDir, "reports")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val tempFile = File(cacheDir, "CashFigure_Share.pdf")
        tempFile.writeBytes(pdfData)

        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, if (isBangla) "ক্যাশ রিপোর্ট শেয়ার করুন" else "Share Cash Report"))
    }

    fun updateSheetRemark(remark: String) {
        val currentSheet = _uiState.value.sheet ?: return
        val sanitizedRemark = remark.replace("\n", " ").replace("\r", " ")
        val updatedSheet = currentSheet.copy(remark = sanitizedRemark)
        viewModelScope.launch {
            sheetRepository.updateSheet(updatedSheet)
            _uiState.update { it.copy(sheet = updatedSheet) }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(exportStatusMessage = null) }
    }
}
