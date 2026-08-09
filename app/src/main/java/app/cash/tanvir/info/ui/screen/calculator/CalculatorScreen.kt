package app.cash.tanvir.info.ui.screen.calculator

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.ui.screen.calculator.components.BreakdownSection
import app.cash.tanvir.info.ui.screen.calculator.components.DashboardCard
import app.cash.tanvir.info.ui.screen.calculator.components.DenominationRowItem

/**
 * Main calculator screen with denomination inputs, dashboard, breakdown, and navigation actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onNavigateToHistory: () -> Unit = {},
    onNavigateToReport: (Long) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isBangla = uiState.currentLanguage == AppLanguage.BANGLA
    var showClearAllConfirmation by remember { mutableStateOf(false) }
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
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = if (isBangla) "ইতিহাস" else "History")
                    }
                    // Settings icon button
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = if (isBangla) "সেটিংস" else "Settings")
                    }
                    // Clear all button with confirmation
                    IconButton(onClick = { showClearAllConfirmation = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = if (isBangla) "সব মুছুন" else "Clear All",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
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
                    onClear = { pendingClearDenomination = row.denomination.value },
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
                            val now = System.currentTimeMillis()
                            if (now - lastSaveClick < 500) return@Button
                            lastSaveClick = now

                            val saved = viewModel.saveToHistory()
                            val msg = if (saved) {
                                if (isBangla) "হিস্ট্রিতে সেভ করা হয়েছে" else "Saved to history"
                            } else {
                                if (isBangla) "০ টাকা সেভ করা সম্ভব নয়" else "Cannot save 0 amount calculation"
                            }
                            activeToast?.cancel()
                            activeToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT).also { it.show() }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(if (isBangla) "সেভ করুন" else "Save")
                    }
                }
            }

            // Breakdown section
            item {
                Spacer(modifier = Modifier.height(12.dp))
                BreakdownSection(
                    rows = uiState.rows,
                    isExpanded = uiState.isBreakdownExpanded,
                    onToggle = { viewModel.toggleBreakdown() },
                    isBangla = isBangla,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }

    // Confirmation dialog for Header Clear All button
    if (showClearAllConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmation = false },
            title = { Text(if (isBangla) "সব এন্ট্রি মুছে ফেলবেন?" else "Clear All Entries?") },
            text = { Text(if (isBangla) "আপনি কি সমস্ত ইনপুট সংখ্যা মুছে নতুন হিসাব শুরু করতে চান?" else "Are you sure you want to clear all denomination entries?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAll()
                        showClearAllConfirmation = false
                    }
                ) {
                    Text(if (isBangla) "মুছে ফেলুন" else "Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirmation = false }) {
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
            onDismissRequest = { pendingClearDenomination = null },
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
                TextButton(
                    onClick = {
                        viewModel.clearRow(denomValue)
                        pendingClearDenomination = null
                    }
                ) {
                    Text(
                        if (isBangla) "মুছে ফেলুন" else "Clear",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingClearDenomination = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
