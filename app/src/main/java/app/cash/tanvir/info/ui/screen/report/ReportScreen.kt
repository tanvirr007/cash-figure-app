package app.cash.tanvir.info.ui.screen.report

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.cash.tanvir.info.util.CurrencyFormatter
import app.cash.tanvir.info.util.NumberToWordsConverter
import app.cash.tanvir.info.util.HapticHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var pendingExportFormat by remember { mutableStateOf<ExportFormat?>(null) }
    var showNotesPrompt by remember { mutableStateOf(viewModel.fromSave) }
    var notesInputText by remember { mutableStateOf("") }
    var placeholderText by remember { mutableStateOf("") }
    val fullPlaceholder = "BRAC BANK PLC"

    LaunchedEffect(uiState.exportStatusMessage) {
        uiState.exportStatusMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearStatusMessage()
        }
    }

    val sheet = uiState.sheet

    val isBangla = uiState.isBangla

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBangla) "রিপোর্ট এক্সপোর্ট ও প্রিন্ট" else "Export & Print Report") },
                navigationIcon = {
                    IconButton(onClick = {
                        HapticHelper.vibrate(context)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = if (isBangla) "ফিরে যান" else "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (sheet == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(if (isBangla) "রিপোর্টের জন্য কোনো শিট লোড করা হয়নি" else "No sheet loaded for reporting")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Report Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = sheet.name.ifEmpty { if (isBangla) "ক্যাশ হিসাবের রিপোর্ট" else "Cash Calculation Report" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBangla) {
                                "সর্বমোট: ${CurrencyFormatter.format(sheet.grandTotal, useBengaliDigits = true)}"
                            } else {
                                "Grand Total: ${CurrencyFormatter.format(sheet.grandTotal, useBengaliDigits = false)}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val words = if (isBangla) NumberToWordsConverter.toBangla(sheet.grandTotal) else NumberToWordsConverter.toEnglish(sheet.grandTotal)
                        Text(
                            text = if (isBangla) "কথায়: $words" else "Words: $words",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isBangla) "বিস্তারিত হিসাব:" else "Breakdown:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        sheet.rows.filter { it.quantity > 0 }.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val label = if (isBangla) row.denomination.labelBn else row.denomination.label
                                val qtyStr = if (isBangla) app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(row.quantity) else row.quantity.toString()
                                Text("$label × $qtyStr", style = MaterialTheme.typography.bodyMedium)
                                Text(CurrencyFormatter.format(row.total, useBengaliDigits = isBangla), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBangla) "সর্বমোট" else "Grand Total",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = CurrencyFormatter.format(sheet.grandTotal, useBengaliDigits = isBangla),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                    append(if (isBangla) "নোট: " else "Notes: ")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    append(sheet.remark.ifBlank { "N/A" })
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Text(if (isBangla) "এক্সপোর্ট অপশন" else "Export Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            HapticHelper.vibrate(context)
                            pendingExportFormat = ExportFormat.PDF
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text("PDF")
                    }
                    Button(
                        onClick = {
                            HapticHelper.vibrate(context)
                            pendingExportFormat = ExportFormat.CSV
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text("CSV")
                    }
                    Button(
                        onClick = {
                            HapticHelper.vibrate(context)
                            pendingExportFormat = ExportFormat.TXT
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.TextSnippet, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text("TXT")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            HapticHelper.vibrate(context)
                            viewModel.printReport(context)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(if (isBangla) "প্রিন্ট" else "Print")
                    }
                    OutlinedButton(
                        onClick = {
                            HapticHelper.vibrate(context)
                            viewModel.shareReport(context)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(if (isBangla) "শেয়ার" else "Share")
                    }
                }
            }
        }
    }

    // Export Confirmation Dialog
    pendingExportFormat?.let { format ->
        val formatName = format.name
        val titleText = if (isBangla) "এক্সপোর্ট নিশ্চিত করুন" else "Confirm Export"
        val messageText = if (isBangla) {
            "আপনি কি নিশ্চিত যে আপনি হিসাবটি $formatName ফাইল হিসেবে এক্সপোর্ট করতে চান?"
        } else {
            "Are you sure you want to export the calculation as a $formatName file?"
        }
        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                pendingExportFormat = null
            },
            title = { Text(titleText) },
            text = { Text(messageText) },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.vibrate(context)
                        viewModel.exportReport(context, format)
                        pendingExportFormat = null
                    }
                ) {
                    Text(if (isBangla) "নিশ্চিত করুন" else "Confirm", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    HapticHelper.vibrate(context)
                    pendingExportFormat = null
                }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Compulsory Notes Prompt Dialog
    if (showNotesPrompt) {
        LaunchedEffect(Unit) {
            while (true) {
                for (i in 1..fullPlaceholder.length) {
                    placeholderText = fullPlaceholder.substring(0, i)
                    kotlinx.coroutines.delay(150L)
                }
                kotlinx.coroutines.delay(2000L)
                placeholderText = ""
                kotlinx.coroutines.delay(500L)
            }
        }

        AlertDialog(
            onDismissRequest = {}, // compulsory, cannot be dismissed by clicking outside
            title = {
                Text(
                    text = if (isBangla) "নোট যোগ করুন" else "Add Notes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isBangla) 
                            "রিপোর্ট তৈরি করতে অনুগ্রহ করে একটি নোট যোগ করুন (যেমন ব্যাংকের নাম বা উদ্দেশ্য):" 
                            else "Please add a note to generate the report (e.g. bank name or purpose):",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    val isLimitReached = notesInputText.length == 30
                    OutlinedTextField(
                        value = notesInputText,
                        onValueChange = { input ->
                            val sanitized = input.replace("\n", " ").replace("\r", " ")
                            if (sanitized.length <= 30) {
                                notesInputText = sanitized
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (isBangla) "নোট" else "Notes") },
                        placeholder = { Text(placeholderText) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                val remaining = 30 - notesInputText.length
                                val counterText = if (isBangla) {
                                    "${app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(remaining)} অবশিষ্ট"
                                } else {
                                    "$remaining remaining"
                                }
                                Text(
                                    text = counterText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isLimitReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.vibrate(context)
                        if (notesInputText.isNotBlank()) {
                            viewModel.updateSheetRemark(notesInputText.trim())
                            showNotesPrompt = false
                        }
                    },
                    enabled = notesInputText.isNotBlank(), // only enabled if note is non-blank
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBangla) "সম্পাদনা" else "Edit")
                }
            }
        )
    }
}
