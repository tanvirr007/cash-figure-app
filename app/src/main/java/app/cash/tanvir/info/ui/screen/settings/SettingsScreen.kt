package app.cash.tanvir.info.ui.screen.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.HistoryToggleOff
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material.icons.rounded.SystemUpdateAlt
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.data.local.preferences.AppTheme
import app.cash.tanvir.info.ui.screen.settingsdetail.SettingsSection
import app.cash.tanvir.info.util.BanglaDigitConverter
import app.cash.tanvir.info.util.HapticHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChangelog: () -> Unit,
    onNavigateToUpdate: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToSettingsDetail: (SettingsSection) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isBangla = uiState.language == AppLanguage.BANGLA

    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onRestoreFileSelected(uri)
        }
    }

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

            SettingsLinkCard(
                icon = { SettingsIconBadge(Icons.Rounded.AutoAwesome) },
                title = if (isBangla) "অ্যাপ থিম" else "App Theme",
                subtitle = currentThemeText,
                onClick = {
                    HapticHelper.vibrate(context)
                    onNavigateToSettingsDetail(SettingsSection.THEME)
                }
            )

            SettingsLinkCard(
                icon = { SettingsIconBadge(Icons.Rounded.Translate) },
                title = if (isBangla) "ভাষা" else "Language",
                subtitle = currentLangText,
                onClick = {
                    HapticHelper.vibrate(context)
                    onNavigateToSettingsDetail(SettingsSection.LANGUAGE)
                }
            )

            SettingsLinkCard(
                icon = { SettingsIconBadge(Icons.Rounded.AccountBalanceWallet) },
                title = if (isBangla) "নোটসমূহ" else "Currency",
                subtitle = if (isBangla) "হোমপেজের নোটগুলো নিয়ন্ত্রণ করুন" else "Manage homepage notes",
                onClick = {
                    HapticHelper.vibrate(context)
                    onNavigateToSettingsDetail(SettingsSection.CURRENCY)
                }
            )

            SettingsLinkCard(
                icon = { SettingsIconBadge(Icons.Rounded.SettingsSuggest) },
                title = if (isBangla) "টুলস" else "Miscellaneous",
                subtitle = if (isBangla) "অতিরিক্ত ফিচারসমূহ" else "Add-on Features",
                onClick = {
                    HapticHelper.vibrate(context)
                    onNavigateToSettingsDetail(SettingsSection.MISCELLANEOUS)
                }
            )

            // Backup & Restore Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        if (isBangla) "ব্যাকআপ ও রিস্টোর" else "Backup & Restore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                HapticHelper.vibrate(context)
                                viewModel.backupData(context)
                            }
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsIconBadge(Icons.Rounded.CloudUpload)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (isBangla) "ব্যাকআপ ডাটা" else "Backup Data",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (isBangla) "ব্যাকআপ ফাইল সেভ করুন" else "Save backup file to Downloads",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                HapticHelper.vibrate(context)
                                restoreFileLauncher.launch("application/json")
                            }
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsIconBadge(Icons.Rounded.CloudDownload)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (isBangla) "রিস্টোর ডাটা" else "Restore Data",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (isBangla) "ব্যাকআপ ফাইল রিস্টোর করুন" else "Restore from backup file",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            SettingsLinkCard(
                icon = { SettingsIconBadge(Icons.Rounded.SystemUpdateAlt) },
                title = if (isBangla) "আপডেটসমূহ" else "Updates",
                subtitle = if (isBangla) "নতুন ভার্সন চেক করুন" else "Check for new versions",
                onClick = {
                    HapticHelper.vibrate(context)
                    onNavigateToUpdate()
                }
            )

            SettingsLinkCard(
                icon = { SettingsIconBadge(Icons.Rounded.HistoryToggleOff) },
                title = if (isBangla) "পরিবর্তন লগ" else "Changelog",
                subtitle = if (isBangla) "সব ভার্সনের পরিবর্তন দেখুন" else "See what's new in each version",
                onClick = {
                    HapticHelper.vibrate(context)
                    onNavigateToChangelog()
                }
            )

            // Destructive Actions Card
            Card(
                onClick = {
                    HapticHelper.vibrate(context)
                    viewModel.openResetDialog()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsIconBadge(Icons.Rounded.DeleteSweep, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            if (isBangla) "রিসেট করুন" else "Reset All",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            if (isBangla) "সব ডাটা ও সেটিংস মুছে ফেলুন" else "Delete all data and settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            SettingsLinkCard(
                icon = { SettingsIconBadge(Icons.Rounded.Info) },
                title = if (isBangla) "ক্যাশ ফিগার সম্পর্কে" else "About Cash Figure",
                subtitle = if (isBangla) "অ্যাপ সম্পর্কে জানুন" else "Learn about the app",
                onClick = {
                    HapticHelper.vibrate(context)
                    onNavigateToAbout()
                }
            )
        }
    }

    // Explicit Confirmation Dialog for Reset All Data
    if (uiState.showResetConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                viewModel.dismissResetDialog()
            },
            title = { Text(if (isBangla) "সকল ডাটা রিসেট করবেন?" else "Reset All Data?") },
            text = {
                Text(
                    if (isBangla) "এটি সেভ করা সকল হিসাব, ইতিহাস এবং সেটিংস স্থায়ীভাবে মুছে ফেলবে। এই কাজটি পূর্বাবস্থায় ফেরানো যাবে না।"
                    else "This will permanently delete all saved calculations, history, and user settings. This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.vibrate(context)
                        viewModel.confirmResetAllData()
                    }
                ) {
                    Text(
                        if (isBangla) "রিসেট করুন" else "Reset",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    HapticHelper.vibrate(context)
                    viewModel.dismissResetDialog()
                }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Warning Dialog for Restore Data — Restore stays disabled during the 15s countdown
    if (uiState.showRestoreWarningDialog) {
        val countdown = uiState.restoreCountdown
        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                viewModel.dismissRestoreDialog()
            },
            title = { Text(if (isBangla) "ডাটা রিস্টোর করবেন?" else "Restore Data?") },
            text = {
                Text(
                    if (isBangla) "পুরানো বা পূর্বের ডাটা রিস্টোর করলে আপনার বর্তমান ডাটা ওভাররাইট বা ক্ষতিগ্রস্ত হতে পারে।"
                    else "Restoring outdated/old data might corrupt or overwrite your present data."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.vibrate(context)
                        viewModel.confirmRestore(context)
                    },
                    enabled = countdown <= 0
                ) {
                    Text(
                        text = if (countdown > 0) {
                            val secs = if (isBangla) {
                                BanglaDigitConverter.toBangla(countdown)
                            } else {
                                countdown.toString()
                            }
                            if (isBangla) "রিস্টোর করুন ($secs)" else "Restore ($secs)"
                        } else {
                            if (isBangla) "রিস্টোর করুন" else "Restore"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    HapticHelper.vibrate(context)
                    viewModel.dismissRestoreDialog()
                }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsIconBadge(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color = tint.copy(alpha = 0.14f), shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
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
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
