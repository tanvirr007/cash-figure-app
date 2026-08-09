package app.cash.tanvir.info.ui.screen.calculator

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.util.HapticHelper

import app.cash.tanvir.info.ui.screen.calculator.components.DashboardCard
import app.cash.tanvir.info.ui.screen.calculator.components.DenominationRowItem

/**
 * Main calculator screen with denomination inputs, dashboard, breakdown, and navigation actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onNavigateToHistory: () -> Unit = {},
    onNavigateToReport: (Long, Boolean) -> Unit = { _, _ -> },
    onNavigateToSettings: () -> Unit = {},
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isBangla = uiState.currentLanguage == AppLanguage.BANGLA
    val isIdle = uiState.quantities.values.all { it.isEmpty() }
    var showClearAllConfirmation by remember { mutableStateOf(false) }
    var showBreakdownDialog by remember { mutableStateOf(false) }
    var showAddNotesDialog by remember { mutableStateOf(false) }
    var notesInputText by remember { mutableStateOf("") }
    var placeholderText by remember { mutableStateOf("") }
    val fullPlaceholder = "BRAC BANK PLC"

    androidx.compose.runtime.LaunchedEffect(showAddNotesDialog) {
        if (showAddNotesDialog) {
            notesInputText = ""
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
    }

    // Denomination value pending single-row clear confirmation (null = no dialog)
    var pendingClearDenomination by remember { mutableStateOf<Int?>(null) }

    // Single toast reference — cancel the previous before showing a new one to avoid stacking
    var activeToast by remember { mutableStateOf<Toast?>(null) }
    // Debounce save clicks: ignore rapid repeated taps within 500ms
    var lastSaveClick by remember { mutableLongStateOf(0L) }

    // Back-to-exit: require two presses within 2 seconds
    var lastBackPress by remember { mutableLongStateOf(0L) }
    BackHandler {
        val now = System.currentTimeMillis()
        if (now - lastBackPress < 2000) {
            (context as? android.app.Activity)?.finish()
        } else {
            lastBackPress = now
            val msg = if (isBangla) "বের হতে আবার ব্যাক চাপুন" else "Press back again to exit"
            activeToast?.cancel()
            activeToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT).also { it.show() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "ক্যাশ ফিগার" else "Cash Figure",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    // History icon button
                    IconButton(onClick = {
                        HapticHelper.vibrate(context)
                        onNavigateToHistory()
                    }) {
                        Icon(Icons.Default.History, contentDescription = if (isBangla) "ইতিহাস" else "History")
                    }
                    // Settings icon button
                    IconButton(onClick = {
                        HapticHelper.vibrate(context)
                        onNavigateToSettings()
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = if (isBangla) "সেটিংস" else "Settings")
                    }
                    // Clear all button with confirmation or idle toast
                    IconButton(
                        onClick = {
                            HapticHelper.vibrate(context)
                            if (isIdle) {
                                val msg = if (isBangla) "মুছে ফেলার মতো কিছু নেই" else "Nothing to clear"
                                activeToast?.cancel()
                                activeToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT).also { it.show() }
                            } else {
                                showClearAllConfirmation = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = if (isBangla) "সব মুছুন" else "Clear All",
                            tint = if (isIdle) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = 8.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Dashboard card
            item {
                val words = if (isBangla) uiState.amountInWordsBn else uiState.amountInWordsEn
                DashboardCard(
                    grandTotalFormatted = uiState.grandTotalFormatted,
                    amountInWords = words,
                    totalPieces = uiState.totalPieces,
                    activeDenominations = uiState.activeDenominations,
                    isBangla = isBangla,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Denomination rows
            itemsIndexed(
                items = uiState.rows,
                key = { _, row -> row.denomination.value }
            ) { index, row ->
                val label = if (isBangla) row.denomination.labelBn else row.denomination.label
                DenominationRowItem(
                    denominationValue = row.denomination.value,
                    denominationLabel = label,
                    quantityText = uiState.quantities[row.denomination.value] ?: "",
                    rowTotal = row.total,
                    isLastRow = index == uiState.rows.lastIndex,
                    onQuantityChange = { viewModel.updateQuantity(row.denomination.value, it) },
                    onClear = {
                        HapticHelper.vibrate(context)
                        pendingClearDenomination = row.denomination.value
                    },
                    isBangla = isBangla
                )
            }

            // Action row: Save to History
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Button(
                        onClick = {
                            HapticHelper.vibrate(context)
                            val now = System.currentTimeMillis()
                            if (now - lastSaveClick < 500) return@Button
                            lastSaveClick = now

                            if (uiState.grandTotal <= 0L) {
                                val msg = if (isBangla) "০ টাকা সেভ করা সম্ভব নয়" else "Cannot save 0 amount calculation"
                                activeToast?.cancel()
                                activeToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT).also { it.show() }
                            } else {
                                showBreakdownDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(if (isBangla) "সেভ করুন" else "Save")
                    }
                }
            }


        }
    }

    // Confirmation dialog for Header Clear All button
    if (showClearAllConfirmation) {
        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                showClearAllConfirmation = false
            },
            title = { Text(if (isBangla) "সব এন্ট্রি মুছে ফেলবেন?" else "Clear All Entries?") },
            text = { Text(if (isBangla) "আপনি কি সমস্ত ইনপুট সংখ্যা মুছে নতুন হিসাব শুরু করতে চান?" else "Are you sure you want to clear all denomination entries?") },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.vibrate(context)
                        viewModel.clearAll()
                        showClearAllConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(if (isBangla) "মুছে ফেলুন" else "Clear All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    HapticHelper.vibrate(context)
                    showClearAllConfirmation = false
                }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Confirmation dialog for individual row clear (✕ button)
    pendingClearDenomination?.let { denomValue ->
        val row = uiState.rows.find { it.denomination.value == denomValue }
        val denomLabel = if (row != null) {
            if (isBangla) row.denomination.labelBn else row.denomination.label
        } else {
            denomValue.toString()
        }
        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                pendingClearDenomination = null
            },
            title = {
                Text(
                    if (isBangla) "মুছে ফেলবেন?"
                    else "Clear?"
                )
            },
            text = {
                Text(
                    if (isBangla) "আপনি কি $denomLabel এর সংখ্যা মুছে ফেলতে চান?"
                    else "Are you sure you want to clear the quantity for $denomLabel?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.vibrate(context)
                        viewModel.clearRow(denomValue)
                        pendingClearDenomination = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(if (isBangla) "মুছে ফেলুন" else "Clear")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    HapticHelper.vibrate(context)
                    pendingClearDenomination = null
                }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Cash Breakdown Dialog on Save Click
    if (showBreakdownDialog) {
        val activeRows = uiState.rows.filter { it.quantity > 0 }
        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                showBreakdownDialog = false
            },
            title = {
                Text(
                    text = if (isBangla) "ক্যাশ ব্রেকডাউন" else "Cash Breakdown",
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
                            val rowTotalFormatted = app.cash.tanvir.info.util.CurrencyFormatter.format(row.total, useBengaliDigits = isBangla)

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
                                text = uiState.grandTotalFormatted,
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
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        HapticHelper.vibrate(context)
                        showBreakdownDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.vibrate(context)
                        showBreakdownDialog = false
                        showAddNotesDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(if (isBangla) "পরবর্তী" else "Next", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Add Notes Dialog on Save
    if (showAddNotesDialog) {
        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                showAddNotesDialog = false
            },
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
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        HapticHelper.vibrate(context)
                        showAddNotesDialog = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.vibrate(context)
                        viewModel.saveToHistory(remark = notesInputText.trim()) { savedId, savedAmount ->
                            val msg = if (isBangla) "লেনদেন সেভ হয়েছে: $savedAmount" else "Transaction saved: $savedAmount"
                            activeToast?.cancel()
                            activeToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT).also { it.show() }
                            onNavigateToReport(savedId, false)
                        }
                        showAddNotesDialog = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(if (isBangla) "সেভ করুন" else "Save", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
