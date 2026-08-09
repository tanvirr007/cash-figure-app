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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
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

                        val activeRows = sheet.rows.filter { it.quantity > 0 }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            // Header row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBangla) "নোটের বিবরণ" else "Denomination Detail",
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                // Vertical divider
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(40.dp)
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                )
                                Text(
                                    text = if (isBangla) "সাবটোটাল" else "Subtotal",
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }
                            
                            // Horizontal divider below header
                            androidx.compose.material3.HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )

                            // Item rows
                            activeRows.forEachIndexed { index, row ->
                                val denomLabel = if (isBangla) row.denomination.labelBn else row.denomination.label
                                val qtyStr = if (isBangla) app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(row.quantity) else row.quantity.toString()
                                val rowTotalFormatted = CurrencyFormatter.format(row.total, useBengaliDigits = isBangla)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$denomLabel × $qtyStr",
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    // Vertical divider
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(40.dp)
                                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    )
                                    Text(
                                        text = rowTotalFormatted,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                                    )
                                }

                                // Horizontal divider between rows
                                androidx.compose.material3.HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            }

                            // Grand Total row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBangla) "সর্বমোট" else "Grand Total",
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                // Vertical divider
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(44.dp)
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                )
                                Text(
                                    text = CurrencyFormatter.format(sheet.grandTotal, useBengaliDigits = isBangla),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }
                        }
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


}
