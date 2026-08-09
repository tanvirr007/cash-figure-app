package app.cash.tanvir.info.ui.screen.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.cash.tanvir.info.domain.model.Sheet
import app.cash.tanvir.info.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onSelectSheet: (Sheet) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheets by viewModel.sheets.collectAsState()
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
                viewModel.undoDelete()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBangla) "হিসাবের ইতিহাস" else "Calculation History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = if (isBangla) "ফিরে যান" else "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
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
                                if (isBangla) "এখনো কোনো সেভ করা হিসাব নেই" else "No saved calculations yet"
                            } else {
                                if (isBangla) "কোনো মিল থাকা হিসাব পাওয়া যায়নি" else "No matching calculations found"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sheets, key = { it.id }) { sheet ->
                        HistoryCard(
                            sheet = sheet,
                            isBangla = isBangla,
                            onClick = { onSelectSheet(sheet) },
                            onRename = { viewModel.openRenameDialog(sheet) },
                            onDelete = { viewModel.openDeleteConfirmation(sheet) }
                        )
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (uiState.showRenameDialogForSheet != null) {
        val targetSheet = uiState.showRenameDialogForSheet!!
        var renameText by remember { mutableStateOf(targetSheet.name) }

        AlertDialog(
            onDismissRequest = { viewModel.dismissRenameDialog() },
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
                TextButton(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameSheet(targetSheet, renameText.trim())
                        }
                    }
                ) {
                    Text(if (isBangla) "সেভ করুন" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRenameDialog() }) {
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
            onDismissRequest = { viewModel.dismissDeleteConfirmation() },
            title = { Text(if (isBangla) "হিসাবটি মুছে ফেলবেন?" else "Delete Calculation?") },
            text = { Text(if (isBangla) "আপনি কি সত্যিই \"$sheetName\" মুছে ফেলতে চান?" else "Are you sure you want to delete \"$sheetName\"?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDeleteSheet() }
                ) {
                    Text(if (isBangla) "মুছে ফেলুন" else "Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteConfirmation() }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

private fun formatHistoryDate(timestamp: Long, isBangla: Boolean): String {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
    val str = dateFormat.format(Date(timestamp))
    if (!isBangla) return str

    var bnStr = app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(str)
    bnStr = bnStr.replace("Jan", "জানুয়ারি")
        .replace("Feb", "ফেব্রুয়ারি")
        .replace("Mar", "মার্চ")
        .replace("Apr", "এপ্রিল")
        .replace("May", "মে")
        .replace("Jun", "জুন")
        .replace("Jul", "জুলাই")
        .replace("Aug", "আগস্ট")
        .replace("Sep", "সেপ্টেম্বর")
        .replace("Oct", "অক্টোবর")
        .replace("Nov", "নভেম্বর")
        .replace("Dec", "ডিসেম্বর")
        .replace("AM", "এএম")
        .replace("PM", "পিএম")
        .replace("am", "এএম")
        .replace("pm", "পিএম")
    return bnStr
}

@Composable
private fun HistoryCard(
    sheet: Sheet,
    isBangla: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedDate = formatHistoryDate(sheet.updatedAt, isBangla)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sheet.name.ifEmpty { if (isBangla) "সেভ করা হিসাব" else "Saved Sheet" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.format(sheet.grandTotal, useBengaliDigits = isBangla),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isBangla) {
                    "$formattedDate · ${app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(sheet.totalPieces)} টি নোট · ${app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(sheet.activeDenominations)} টি নোটের ধরণ"
                } else {
                    "$formattedDate · ${sheet.totalPieces} pieces · ${sheet.activeDenominations} denom."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onRename) {
                    Icon(Icons.Default.Edit, contentDescription = if (isBangla) "নাম পরিবর্তন" else "Rename", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = if (isBangla) "মুছে ফেলুন" else "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            }
        }
    }
}
