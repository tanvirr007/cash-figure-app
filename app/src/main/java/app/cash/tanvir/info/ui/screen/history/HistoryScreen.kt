package app.cash.tanvir.info.ui.screen.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.cash.tanvir.info.domain.model.Sheet
import app.cash.tanvir.info.ui.components.CompactTopBar
import app.cash.tanvir.info.ui.components.VerticalScrollbarIndicator
import app.cash.tanvir.info.util.CurrencyFormatter
import app.cash.tanvir.info.util.HapticHelper
import app.cash.tanvir.info.util.NumberToWordsConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onSelectSheet: (Sheet) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheets by viewModel.sheets.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val isBangla = uiState.currentLanguage == app.cash.tanvir.info.data.local.preferences.AppLanguage.BANGLA

    // Show undo snackbar when item is deleted
    LaunchedEffect(uiState.lastDeletedSheetId) {
        if (uiState.lastDeletedSheetId != null) {
            val result = snackbarHostState.showSnackbar(
                message = if (isBangla) "শিটটি মুছে ফেলা হয়েছে" else "Sheet deleted",
                actionLabel = if (isBangla) "পূর্বাবস্থায় আনুন" else "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                HapticHelper.vibrate(context)
                viewModel.undoDelete()
            }
        }
    }

    Scaffold(
        topBar = {
            CompactTopBar(
                title = if (isBangla) "হিসাবের ইতিহাস" else "Calculation History"
            )
        },
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(if (isBangla) "নাম বা পরিমাণ দিয়ে খুঁজুন..." else "Search by name or amount...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = if (isBangla) "খুঁজুন" else "Search") },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            HapticHelper.vibrate(context)
                            viewModel.onSearchQueryChange("")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isBangla) "সাফ করুন" else "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (sheets.isEmpty()) {
                // Empty state placeholder
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.padding(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Text(
                            text = if (uiState.searchQuery.isEmpty()) {
                                if (isBangla) "এখনো কোনো হিসাব সেভ করা হয়নি" else "No saved calculations yet"
                            } else {
                                if (isBangla) "কোনো মিলে যাওয়া হিসাব পাওয়া যায়নি" else "No matching calculations found"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    val listState = rememberLazyListState()
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sheets, key = { it.id }) { sheet ->
                            HistoryCard(
                                sheet = sheet,
                                isBangla = isBangla,
                                modifier = Modifier.animateItem(),
                                onClick = {
                                    HapticHelper.vibrate(context)
                                    onSelectSheet(sheet)
                                },
                                onRename = {
                                    HapticHelper.vibrate(context)
                                    viewModel.openRenameDialog(sheet)
                                },
                                onDelete = {
                                    HapticHelper.vibrate(context)
                                    viewModel.openDeleteConfirmation(sheet)
                                }
                            )
                        }
                    }
                    VerticalScrollbarIndicator(
                        state = listState,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }

    // Rename Dialog
    if (uiState.showRenameDialogForSheet != null) {
        val targetSheet = uiState.showRenameDialogForSheet!!
        var renameText by remember { mutableStateOf(targetSheet.name) }

        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                viewModel.dismissRenameDialog()
            },
            title = { Text(if (isBangla) "শিটের নাম পরিবর্তন" else "Rename Sheet") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text(if (isBangla) "শিটের নাম" else "Sheet Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.vibrate(context)
                        if (renameText.isNotBlank()) {
                            viewModel.renameSheet(targetSheet, renameText.trim())
                        }
                    },
                    enabled = renameText.isNotBlank()
                ) {
                    Text(if (isBangla) "সেভ করুন" else "Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    HapticHelper.vibrate(context)
                    viewModel.dismissRenameDialog()
                }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteConfirmationForSheet != null) {
        val targetSheet = uiState.showDeleteConfirmationForSheet!!
        val sheetName = targetSheet.name.ifEmpty { if (isBangla) "সেভ করা হিসাব" else "Saved Sheet" }

        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                viewModel.dismissDeleteConfirmation()
            },
            title = { Text(if (isBangla) "হিসাবটি মুছে ফেলবেন?" else "Delete Calculation?") },
            text = { Text(if (isBangla) "আপনি কি \"$sheetName\" মুছে ফেলতে চান?" else "Are you sure you want to delete \"$sheetName\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.vibrate(context)
                        viewModel.confirmDeleteSheet()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(if (isBangla) "মুছে ফেলুন" else "Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    HapticHelper.vibrate(context)
                    viewModel.dismissDeleteConfirmation()
                }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

private fun formatHistoryDate(timestamp: Long, isBangla: Boolean): String {
    return app.cash.tanvir.info.util.DateTimeFormatter.format(timestamp, isBangla)
}

@Composable
private fun HistoryCard(
    sheet: Sheet,
    isBangla: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedDate = formatHistoryDate(sheet.updatedAt, isBangla)

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title with actions in the top-right corner (draft-card style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sheet.name.ifEmpty { if (isBangla) "সেভ করা হিসাব" else "Saved Sheet" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = if (isBangla) "নাম পরিবর্তন" else "Rename",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = if (isBangla) "মুছে ফেলুন" else "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Amount right after the title
            Text(
                text = CurrencyFormatter.format(sheet.grandTotal, useBengaliDigits = isBangla),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (isBangla) {
                    "$formattedDate · ${app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(sheet.totalPieces)} টি নোট · ${app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(sheet.activeDenominations)} ধরণের নোট"
                } else {
                    "$formattedDate · ${if (sheet.totalPieces == 1L) "1 piece" else "${sheet.totalPieces} pieces"} · ${sheet.activeDenominations} denom."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Amount in words
            Text(
                text = if (isBangla) {
                    "কথায়: ${NumberToWordsConverter.toBangla(sheet.grandTotal)}"
                } else {
                    "In words: ${NumberToWordsConverter.toEnglish(sheet.grandTotal)}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            if (!sheet.remark.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                val sanitizedRemark = sheet.remark.replace("\n", " ").replace("\r", " ")
                Text(
                    text = if (isBangla) "মন্তব্য: $sanitizedRemark" else "Notes: $sanitizedRemark",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
