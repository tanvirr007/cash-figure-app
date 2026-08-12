package app.cash.tanvir.info.ui.screen.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.HistoryToggleOff
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material.icons.rounded.SystemUpdateAlt
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.data.local.preferences.AppTheme
import app.cash.tanvir.info.domain.model.Sheet
import app.cash.tanvir.info.ui.screen.settingsdetail.SettingsSection
import app.cash.tanvir.info.util.BanglaDigitConverter
import app.cash.tanvir.info.util.CurrencyFormatter
import app.cash.tanvir.info.util.DateTimeFormatter
import app.cash.tanvir.info.util.HapticHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChangelog: () -> Unit,
    onNavigateToUpdate: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToSettingsDetail: (SettingsSection) -> Unit,
    onNavigateToDraftReport: (Long) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isBangla = uiState.language == AppLanguage.BANGLA
    var draftsExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBangla) "সেটিংস" else "Settings") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val currentThemeText = when (uiState.theme) {
                AppTheme.SYSTEM -> if (isBangla) "সিস্টেম থিম" else "Follow System"
                AppTheme.LIGHT -> if (isBangla) "লাইট থিম" else "Light Theme"
                AppTheme.DARK -> if (isBangla) "ডার্ক থিম" else "Dark Theme"
            }
            val currentLangText = when (uiState.language) {
                AppLanguage.ENGLISH -> if (isBangla) "ইংরেজি" else "English"
                AppLanguage.BANGLA -> if (isBangla) "বাংলা" else "Bangla"
            }

            // General group: App Theme / Language / Currency
            SettingsGroupCard(title = if (isBangla) "সাধারণ" else "General") {
                SettingsGroupRow(
                    icon = Icons.Rounded.AutoAwesome,
                    title = if (isBangla) "অ্যাপ থিম" else "App Theme",
                    subtitle = if (isBangla) "থিম বেছে নিন ও প্রিভিউ দেখুন" else "Pick a theme and preview it",
                    trailing = currentThemeText,
                    onClick = {
                        HapticHelper.vibrate(context)
                        onNavigateToSettingsDetail(SettingsSection.THEME)
                    }
                )
                SettingsGroupDivider()
                SettingsGroupRow(
                    icon = Icons.Rounded.Translate,
                    title = if (isBangla) "ভাষা" else "Language",
                    subtitle = if (isBangla) "ভাষা বেছে নিন ও নমুনা দেখুন" else "Pick a language and preview it",
                    trailing = currentLangText,
                    onClick = {
                        HapticHelper.vibrate(context)
                        onNavigateToSettingsDetail(SettingsSection.LANGUAGE)
                    }
                )
                SettingsGroupDivider()
                SettingsGroupRow(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    title = if (isBangla) "নোটসমূহ" else "Currency",
                    subtitle = if (isBangla) "হোমপেজের নোটগুলো নিয়ন্ত্রণ করুন" else "Manage homepage notes",
                    onClick = {
                        HapticHelper.vibrate(context)
                        onNavigateToSettingsDetail(SettingsSection.CURRENCY)
                    }
                )
            }

            // Draft group: saved drafts list with save/discard controls (expandable)
            DraftsGroupCard(
                isBangla = isBangla,
                expanded = draftsExpanded,
                drafts = uiState.drafts,
                onToggle = {
                    HapticHelper.vibrate(context)
                    draftsExpanded = !draftsExpanded
                },
                onOpenDraft = {
                    HapticHelper.vibrate(context)
                    onNavigateToDraftReport(it)
                },
                onSaveToHistory = {
                    HapticHelper.vibrate(context)
                    viewModel.saveDraftToHistory(it)
                },
                onDiscardDraft = {
                    HapticHelper.vibrate(context)
                    viewModel.openDiscardDraftDialog(it)
                }
            )

            // Update group: check for updates / changelog
            SettingsGroupCard(title = if (isBangla) "আপডেট" else "Update") {
                SettingsGroupRow(
                    icon = Icons.Rounded.SystemUpdateAlt,
                    title = if (isBangla) "আপডেট চেক করুন" else "Check for updates",
                    subtitle = if (isBangla) "নতুন ভার্সন খুঁজে ইনস্টল করুন" else "Find and install the latest version",
                    onClick = {
                        HapticHelper.vibrate(context)
                        onNavigateToUpdate()
                    }
                )
                SettingsGroupDivider()
                SettingsGroupRow(
                    icon = Icons.Rounded.HistoryToggleOff,
                    title = if (isBangla) "পরিবর্তন লগ" else "Changelog",
                    subtitle = if (isBangla) "সব ভার্সনের পরিবর্তন দেখুন" else "See what's new in each version",
                    onClick = {
                        HapticHelper.vibrate(context)
                        onNavigateToChangelog()
                    }
                )
            }

            SettingsLinkCard(
                icon = { SettingsIconBadge(Icons.Rounded.SettingsSuggest) },
                title = if (isBangla) "টুলস" else "Miscellaneous",
                subtitle = if (isBangla) "অতিরিক্ত ফিচার ও ডাটা টুলস" else "Extra features & data tools",
                onClick = {
                    HapticHelper.vibrate(context)
                    onNavigateToSettingsDetail(SettingsSection.MISCELLANEOUS)
                }
            )

            SettingsLinkCard(
                icon = { SettingsIconBadge(Icons.Rounded.Person) },
                title = if (isBangla) "লেখক" else "Author",
                subtitle = if (isBangla) "অ্যাপ সম্পর্কে জানুন" else "Learn about the app",
                onClick = {
                    HapticHelper.vibrate(context)
                    onNavigateToAbout()
                }
            )
        }
    }

    // Minimal discard-draft confirmation (2 buttons, one line)
    if (uiState.showDiscardDraftDialog) {
        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                viewModel.dismissDiscardDraftDialog()
            },
            title = { Text(if (isBangla) "ড্রাফট বাতিল করবেন?" else "Discard Draft?") },
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
private fun DraftsGroupCard(
    isBangla: Boolean,
    expanded: Boolean,
    drafts: List<Sheet>,
    onToggle: () -> Unit,
    onOpenDraft: (Long) -> Unit,
    onSaveToHistory: (Sheet) -> Unit,
    onDiscardDraft: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(vertical = 6.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingsIconBadge(Icons.Rounded.Bookmarks)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBangla) "ড্রাফট" else "Draft",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when {
                            drafts.isEmpty() -> if (isBangla) "কোনো ড্রাফট নেই" else "No drafts"
                            isBangla -> "${BanglaDigitConverter.toBangla(drafts.size.toLong())} টি ড্রাফট"
                            else -> "${drafts.size} drafts"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (isBangla) "ড্রাফট তালিকা" else "Draft list",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    if (drafts.isEmpty()) {
                        Text(
                            text = if (isBangla) "কোনো সংরক্ষিত ড্রাফট নেই" else "No saved drafts",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    } else {
                        drafts.forEach { draft ->
                            SettingsGroupDivider()
                            DraftRowItem(
                                draft = draft,
                                isBangla = isBangla,
                                onOpen = { onOpenDraft(draft.id) },
                                onSaveToHistory = { onSaveToHistory(draft) },
                                onDiscard = { onDiscardDraft(draft.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DraftRowItem(
    draft: Sheet,
    isBangla: Boolean,
    onOpen: () -> Unit,
    onSaveToHistory: () -> Unit,
    onDiscard: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(vertical = 4.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = CurrencyFormatter.format(draft.grandTotal, useBengaliDigits = isBangla),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (isBangla) {
                    "${BanglaDigitConverter.toBangla(draft.totalPieces)} টি নোট • " +
                        DateTimeFormatter.format(draft.updatedAt, isBangla = true)
                } else {
                    "${draft.totalPieces} pieces • ${DateTimeFormatter.format(draft.updatedAt, isBangla = false)}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        IconButton(onClick = onSaveToHistory) {
            Icon(
                imageVector = Icons.Rounded.Save,
                contentDescription = if (isBangla) "ইতিহাসে সেভ করুন" else "Save to History",
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

@Composable
private fun SettingsIconBadge(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color = tint.copy(alpha = 0.14f), shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsGroupCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun SettingsGroupDivider() {
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun SettingsGroupRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBadge(icon)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    trailing,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsLinkCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
