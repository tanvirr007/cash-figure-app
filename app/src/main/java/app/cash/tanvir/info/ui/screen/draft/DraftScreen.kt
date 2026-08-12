package app.cash.tanvir.info.ui.screen.draft

import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.domain.model.Sheet
import app.cash.tanvir.info.ui.screen.settings.SettingsViewModel
import app.cash.tanvir.info.util.BanglaDigitConverter
import app.cash.tanvir.info.util.CurrencyFormatter
import app.cash.tanvir.info.util.DateTimeFormatter
import app.cash.tanvir.info.util.HapticHelper

/**
 * Dedicated Draft page: saved drafts list with load-into-calculator and discard
 * controls, plus an empty state. Reuses the activity-scoped SettingsViewModel
 * so the draft list and discard-confirmation state stay in sync.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftScreen(
    onNavigateBack: () -> Unit,
    onOpenDraft: (Long) -> Unit,
    onLoadIntoCalculator: (Long) -> Unit
) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: SettingsViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isBangla = uiState.language == AppLanguage.BANGLA

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBangla) "ড্রাফট" else "Draft") },
                navigationIcon = {
                    IconButton(onClick = {
                        HapticHelper.vibrate(context)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (isBangla) "ফিরে যান" else "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (uiState.drafts.isEmpty()) {
            // Empty state: icon + short explanation instead of a blank page
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bookmarks,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isBangla) "কোনো ড্রাফট নেই" else "No drafts yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isBangla) {
                        "কিছু হিসাব করে ব্যাক চাপলে ড্রাফট হিসেবে সেভ করতে পারবেন — পরে আবার চালিয়ে যেতে পারবেন"
                    } else {
                        "Count some cash and press back to save it as a draft — pick it up again anytime"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val savedCount = uiState.drafts.size
                    Text(
                        text = if (isBangla) {
                            "${BanglaDigitConverter.toBangla(savedCount.toLong())}টি ড্রাফট সংরক্ষিত আছে"
                        } else {
                            if (savedCount == 1) "1 draft saved" else "$savedCount drafts saved"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(items = uiState.drafts, key = { it.id }) { draft ->
                    DraftRowItem(
                        draft = draft,
                        isBangla = isBangla,
                        onOpen = {
                            HapticHelper.vibrate(context)
                            onOpenDraft(draft.id)
                        },
                        onLoadIntoCalculator = {
                            HapticHelper.vibrate(context)
                            onLoadIntoCalculator(draft.id)
                        },
                        onDiscard = {
                            HapticHelper.vibrate(context)
                            viewModel.openDiscardDraftDialog(draft.id)
                        }
                    )
                }
            }
        }
    }

    // Discard-draft confirmation (2 buttons, one line)
    if (uiState.showDiscardDraftDialog) {
        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                viewModel.dismissDiscardDraftDialog()
            },
            title = { Text(if (isBangla) "ড্রাফটটি বাতিল করবেন?" else "Discard Draft?") },
            text = {},
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.vibrate(context)
                        viewModel.confirmDiscardDraft()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(if (isBangla) "মুছে ফেলুন" else "Discard")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    HapticHelper.vibrate(context)
                    viewModel.dismissDiscardDraftDialog()
                }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun DraftRowItem(
    draft: Sheet,
    isBangla: Boolean,
    onOpen: () -> Unit,
    onLoadIntoCalculator: () -> Unit,
    onDiscard: () -> Unit
) {
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
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
                    text = draftTitle(draft.name, isBangla),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = CurrencyFormatter.format(draft.grandTotal, useBengaliDigits = isBangla),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isBangla) {
                    "${BanglaDigitConverter.toBangla(draft.totalPieces)}টি নোট • " +
                        DateTimeFormatter.format(draft.updatedAt, isBangla = true)
                } else {
                    "${draft.totalPieces} pieces • ${DateTimeFormatter.format(draft.updatedAt, isBangla = false)}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onLoadIntoCalculator) {
                    Icon(
                        imageVector = Icons.Rounded.Calculate,
                        contentDescription = if (isBangla) "ক্যালকুলেটরে লোড করুন" else "Load into Calculator",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDiscard) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteSweep,
                        contentDescription = if (isBangla) "ড্রাফট বাতিল করুন" else "Discard draft",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun draftTitle(name: String, isBangla: Boolean): String {
    if (name.isBlank()) return if (isBangla) "ড্রাফট" else "Draft"
    if (!isBangla) return name
    val number = name.substringAfter("Draft #", missingDelimiterValue = "")
    return if (number.isNotEmpty()) "ড্রাফট #$number" else "ড্রাফট"
}
