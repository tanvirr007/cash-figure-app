package app.cash.tanvir.info.ui.screen.calculator

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.util.HapticHelper

import app.cash.tanvir.info.ui.components.CompactTopBar
import app.cash.tanvir.info.ui.components.VerticalScrollbarIndicator
import app.cash.tanvir.info.ui.screen.calculator.components.DashboardCard
import app.cash.tanvir.info.ui.screen.calculator.components.DenominationRowItem
import app.cash.tanvir.info.ui.screen.calculator.components.QuantityPickerSheet

/**
 * Main calculator screen with denomination inputs, dashboard, breakdown, and navigation actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onNavigateToReport: (Long, Boolean) -> Unit = { _, _ -> },
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isCompact = LocalConfiguration.current.screenWidthDp < 360
    val isBangla = uiState.currentLanguage == AppLanguage.BANGLA
    var showClearAllConfirmation by remember { mutableStateOf(false) }
    var showAddNotesDialog by remember { mutableStateOf(false) }
    var showExitWithDraftDialog by remember { mutableStateOf(false) }
    var notesInputText by remember { mutableStateOf("") }
    val fullPlaceholder = "BRAC BANK PLC"

    // Denomination value pending single-row clear confirmation (null = no dialog)
    var pendingClearDenomination by remember { mutableStateOf<Int?>(null) }

    // Single toast reference — cancel the previous before showing a new one to avoid stacking
    var activeToast by remember { mutableStateOf<Toast?>(null) }
    // Debounce save clicks: ignore rapid repeated taps within 500ms
    var lastSaveClick by remember { mutableLongStateOf(0L) }

    // Denomination row whose quantity picker sheet is open (null = closed)
    var pickerRowValue by remember { mutableStateOf<Int?>(null) }

    val onSaveClick: () -> Unit = {
        HapticHelper.vibrate(context)
        val now = System.currentTimeMillis()
        if (now - lastSaveClick >= 500) {
            lastSaveClick = now
            if (uiState.grandTotal <= 0L) {
                val msg = if (isBangla) "০ টাকার হিসাব সেভ করা যাবে না" else "Cannot save 0 amount calculation"
                activeToast?.cancel()
                activeToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT).also { it.show() }
            } else {
                showAddNotesDialog = true
            }
        }
    }

    // Back-to-exit: with a live count, ask to save as draft; otherwise two presses within 2 seconds
    var lastBackPress by remember { mutableLongStateOf(0L) }
    BackHandler {
        if (uiState.grandTotal > 0L) {
            showExitWithDraftDialog = true
        } else {
            val now = System.currentTimeMillis()
            if (now - lastBackPress < 2000) {
                (context as? android.app.Activity)?.finish()
            } else {
                lastBackPress = now
                val msg = if (isBangla) "অ্যাপ থেকে বের হতে আবার ব্যাক চাপুন" else "Press back again to exit"
                activeToast?.cancel()
                activeToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT).also { it.show() }
            }
        }
    }

    Scaffold(
        topBar = {
            CompactTopBar(
                title = if (isBangla) "ক্যাশ ফিগার" else "Cash Figure"
            )
        },
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            AnimatedVisibility(
                visible = uiState.grandTotal > 0L,
                enter = scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(250)
                ) + fadeIn(tween(250)),
                exit = scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(200)
                ) + fadeOut(tween(200))
            ) {
                ExtendedFloatingActionButton(
                    onClick = onSaveClick,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "সেভ করুন" else "Save",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = if (isCompact) 4.dp else 8.dp,
                    end = if (isCompact) 4.dp else 8.dp,
                    top = 8.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
            // Dashboard card
            item {
                val words = if (isBangla) uiState.amountInWordsBn else uiState.amountInWordsEn
                DashboardCard(
                    grandTotal = uiState.grandTotal,
                    amountInWords = words,
                    totalPieces = uiState.totalPieces,
                    activeDenominations = uiState.activeDenominations,
                    isBangla = isBangla,
                    onClearAll = { showClearAllConfirmation = true },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Cash Breakdown section header
            item {
                Text(
                    text = if (isBangla) "নোটের হিসাব" else "Cash Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Denomination rows — one connected card, dividers between rows
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        // First-time hint: point at the tap-to-count interaction when idle
                        if (uiState.totalPieces == 0L) {
                            Text(
                                text = if (isBangla) "হিসাব শুরু করতে নোটের সংখ্যায় চাপ দিন" else "Tap a note's count to begin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                            )
                        }
                        uiState.rows.forEachIndexed { index, row ->
                            if (index > 0) {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                )
                            }
                            val label = if (isBangla) row.denomination.labelBn else row.denomination.label
                            DenominationRowItem(
                                denominationLabel = label,
                                quantityText = uiState.quantities[row.denomination.value] ?: "",
                                rowTotal = row.total,
                                onOpenPicker = {
                                    HapticHelper.vibrate(context)
                                    pickerRowValue = row.denomination.value
                                },
                                onClear = {
                                    HapticHelper.vibrate(context)
                                    pendingClearDenomination = row.denomination.value
                                },
                                isBangla = isBangla
                            )
                        }
                    }
                }
            }
        }
        VerticalScrollbarIndicator(
            state = listState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
    }

    // Quantity picker sheet for the tapped row
    pickerRowValue?.let { value ->
        val pickerRow = uiState.rows.find { it.denomination.value == value }
        if (pickerRow != null) {
            QuantityPickerSheet(
                denominationLabel = if (isBangla) pickerRow.denomination.labelBn else pickerRow.denomination.label,
                denominationValue = pickerRow.denomination.value,
                quantityText = uiState.quantities[pickerRow.denomination.value] ?: "",
                isBangla = isBangla,
                onQuantityChange = { viewModel.updateQuantity(pickerRow.denomination.value, it) },
                onDismiss = { pickerRowValue = null }
            )
        }
    }

    // Confirmation dialog for Header Clear All button
    if (showClearAllConfirmation) {
        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                showClearAllConfirmation = false
            },
            title = { Text(if (isBangla) "সব হিসাব মুছে ফেলবেন?" else "Clear All Entries?") },
            text = { Text(if (isBangla) "আপনি কি সব নোটের সংখ্যা মুছে ফেলে নতুন করে হিসাব শুরু করতে চান?" else "Are you sure you want to clear all denomination entries?") },
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

    // Draft dialog: back pressed while a count is in progress — save as draft and clear,
    // or discard; the app stays open in both cases.
    if (showExitWithDraftDialog) {
        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                showExitWithDraftDialog = false
            },
            title = { Text(if (isBangla) "ড্রাফট হিসেবে সেভ করবেন?" else "Save as Draft?") },
            text = {
                if (uiState.loadedDraftId > 0L) {
                    Text(
                        text = if (isBangla) {
                            "আগের ড্রাফটটি থাকবে — নতুন একটি ড্রাফট তৈরি হবে"
                        } else {
                            "The loaded draft stays — a new draft will be created"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.vibrate(context)
                        viewModel.saveAsDraftAndClear()
                        showExitWithDraftDialog = false
                        val msg = if (isBangla) "নতুন ড্রাফট সেভ হয়েছে" else "New draft saved"
                        activeToast?.cancel()
                        activeToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT).also { it.show() }
                    }
                ) {
                    Text(if (isBangla) "ড্রাফটে সেভ করুন" else "Save to Draft")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        HapticHelper.vibrate(context)
                        viewModel.discardDraft()
                        showExitWithDraftDialog = false
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(if (isBangla) "মুছে ফেলুন" else "Discard")
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
                    if (isBangla) "আপনি কি $denomLabel-এর সংখ্যা মুছে ফেলতে চান?"
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

    // Add Notes Dialog on Save
    if (showAddNotesDialog) {
        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                showAddNotesDialog = false
            },
            title = {
                Text(
                    text = if (isBangla) "মন্তব্য যোগ করুন" else "Add Notes",
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
                            "রিপোর্ট তৈরি করতে একটি মন্তব্য যোগ করুন (যেমন ব্যাংকের নাম বা উদ্দেশ্য)ঃ" 
                            else "Please add a note to generate the report (e.g. bank name or purpose):",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    val isLimitReached = notesInputText.length == 30
                    val interactionSource = remember { MutableInteractionSource() }
                    val isNotesFocused by interactionSource.collectIsFocusedAsState()
                    OutlinedTextField(
                        value = notesInputText,
                        onValueChange = { input ->
                            val sanitized = app.cash.tanvir.info.util.BanglaTextSanitizer.colonToVisarga(
                                input.replace("\n", " ").replace("\r", " "), isBangla
                            )
                            if (sanitized.length <= 30) {
                                notesInputText = sanitized
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            if (notesInputText.isNotEmpty() || isNotesFocused) {
                                Text(if (isBangla) "মন্তব্য" else "Notes")
                            }
                        },
                        placeholder = {
                            Text(
                                text = fullPlaceholder,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        interactionSource = interactionSource,
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
                        if (notesInputText.trim().isBlank()) {
                            val msg = if (isBangla) "মন্তব্য ছাড়া সেভ করা যাবে না" else "Cannot save without a note"
                            activeToast?.cancel()
                            activeToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT).also { it.show() }
                            return@Button
                        }
                        viewModel.saveToHistory(remark = notesInputText.trim()) { savedId, savedAmount ->
                            val msg = if (isBangla) "হিসাব সেভ হয়েছেঃ $savedAmount" else "Transaction saved: $savedAmount"
                            activeToast?.cancel()
                            activeToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT).also { it.show() }
                            onNavigateToReport(savedId, false)
                        }
                        showAddNotesDialog = false
                    },
                    enabled = notesInputText.isNotBlank(),
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
