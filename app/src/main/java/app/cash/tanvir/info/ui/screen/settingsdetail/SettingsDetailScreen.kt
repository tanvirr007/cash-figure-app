package app.cash.tanvir.info.ui.screen.settingsdetail

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import app.cash.tanvir.info.data.local.preferences.AppFont
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.data.local.preferences.AppTheme
import app.cash.tanvir.info.ui.components.VerticalScrollbarIndicator
import app.cash.tanvir.info.ui.screen.settings.SettingsViewModel
import app.cash.tanvir.info.ui.theme.cashFigureColorScheme
import app.cash.tanvir.info.ui.theme.fontFamilyFor
import app.cash.tanvir.info.util.BanglaDigitConverter
import app.cash.tanvir.info.util.CurrencyFormatter
import app.cash.tanvir.info.util.HapticHelper
import app.cash.tanvir.info.util.NumberToWordsConverter

/**
 * Full-page detail view for the Theme / Language / Currency / Miscellaneous
 * settings sections. Shares [SettingsViewModel] with the Settings screen via
 * the activity-scoped instance, so changes apply live.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDetailScreen(
    section: SettingsSection,
    onNavigateBack: () -> Unit
) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: SettingsViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isBangla = uiState.language == AppLanguage.BANGLA

    var pendingThemeName by rememberSaveable { mutableStateOf(uiState.theme.name) }
    val pendingTheme = AppTheme.valueOf(pendingThemeName)
    var pendingLangName by rememberSaveable { mutableStateOf(uiState.language.name) }
    val pendingLang = AppLanguage.valueOf(pendingLangName)
    var pendingFontName by rememberSaveable { mutableStateOf(uiState.font.name) }
    val pendingFont = AppFont.valueOf(pendingFontName)
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }

    val themeSelectionPending = section == SettingsSection.THEME && pendingTheme != uiState.theme
    val langSelectionPending = section == SettingsSection.LANGUAGE && pendingLang != uiState.language
    val fontSelectionPending = section == SettingsSection.FONT && pendingFont != uiState.font
    val selectionPending = when (section) {
        SettingsSection.THEME -> themeSelectionPending
        SettingsSection.LANGUAGE -> langSelectionPending
        SettingsSection.FONT -> fontSelectionPending
        else -> false
    }
    val requestLeave = {
        if (selectionPending) {
            HapticHelper.vibrate(context)
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }
    BackHandler(enabled = selectionPending) { requestLeave() }

    val applyPendingSelection = {
        HapticHelper.vibrate(context)
        when (section) {
            SettingsSection.THEME -> {
                viewModel.setTheme(pendingTheme)
                Toast.makeText(context, if (isBangla) "থিম সেট করা হয়েছে" else "Theme applied", Toast.LENGTH_SHORT).show()
            }
            SettingsSection.LANGUAGE -> {
                viewModel.setLanguage(pendingLang)
                Toast.makeText(context, if (isBangla) "ভাষা পরিবর্তন হয়েছে" else "Language applied", Toast.LENGTH_SHORT).show()
            }
            SettingsSection.FONT -> {
                viewModel.setFont(pendingFont)
                Toast.makeText(context, if (isBangla) "ফন্ট প্রয়োগ করা হয়েছে" else "Font applied", Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
        onNavigateBack()
    }

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

    val title = when (section) {
        SettingsSection.THEME -> if (isBangla) "অ্যাপের থিম" else "App Theme"
        SettingsSection.LANGUAGE -> if (isBangla) "ভাষা" else "Language"
        SettingsSection.FONT -> if (isBangla) "ফন্ট" else "Font"
        SettingsSection.CURRENCY -> if (isBangla) "নোট" else "Currency"
        SettingsSection.MISCELLANEOUS -> if (isBangla) "টুলস" else "Miscellaneous"
    }

    val onBiometricToggle: (Boolean) -> Unit = { checked ->
        HapticHelper.vibrate(context)
        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                authenticateWithFingerprint(context, isBangla, enabling = checked) {
                    viewModel.setBiometricEnabled(checked)
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                if (checked) {
                    val msg = if (isBangla) "অ্যাপ লক চালু করতে ডিভাইসের সেটিংসে গিয়ে ফিঙ্গারপ্রিন্ট বা স্ক্রিন লক (PIN/প্যাটার্ন/পাসওয়ার্ড) সেটআপ করুন।" else "Please set up a screen lock (PIN/pattern/password) or fingerprint in your device settings."
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                } else {
                    viewModel.setBiometricEnabled(false)
                }
            }
            else -> {
                if (checked) {
                    val msg = if (isBangla) "এই ডিভাইসে ফিঙ্গারপ্রিন্ট বা স্ক্রিন লক সেটআপ করা নেই।" else "No fingerprint or screen lock set up on this device."
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                } else {
                    viewModel.setBiometricEnabled(false)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        HapticHelper.vibrate(context)
                        requestLeave()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isBangla) "ফিরে যান" else "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (section == SettingsSection.THEME || section == SettingsSection.LANGUAGE || section == SettingsSection.FONT) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = applyPendingSelection,
                        enabled = selectionPending,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (isBangla) "প্রয়োগ করুন" else "Apply",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                when (section) {
                    SettingsSection.THEME -> {
                        SectionHeader(
                            title = if (isBangla) "অ্যাপের থিম" else "App Theme",
                            subtitle = if (isBangla) "থিম বেছে নিয়ে দেখুন, পছন্দ হলে প্রয়োগ করুন" else "Pick a theme, preview it, then apply"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ThemeContent(
                            isBangla = isBangla,
                            pendingTheme = pendingTheme,
                            dynamicColorEnabled = uiState.dynamicColorEnabled,
                            onSelect = { theme ->
                                HapticHelper.vibrate(context)
                                pendingThemeName = theme.name
                            }
                        )
                    }
                    SettingsSection.LANGUAGE -> {
                        SectionHeader(
                            title = if (isBangla) "ভাষা" else "Language",
                            subtitle = if (isBangla) "ভাষা বেছে নিয়ে দেখুন, পছন্দ হলে প্রয়োগ করুন" else "Pick a language, preview it, then apply"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LanguageContent(
                            isBangla = isBangla,
                            pendingLang = pendingLang,
                            onSelect = { lang ->
                                HapticHelper.vibrate(context)
                                pendingLangName = lang.name
                            }
                        )
                    }
                    SettingsSection.FONT -> {
                        SectionHeader(
                            title = if (isBangla) "ফন্ট" else "Font",
                            subtitle = if (isBangla) "ফন্ট বেছে নিয়ে দেখুন, পছন্দ হলে প্রয়োগ করুন" else "Pick a font, preview it, then apply"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FontContent(
                            isBangla = isBangla,
                            pendingFont = pendingFont,
                            onSelect = { font ->
                                HapticHelper.vibrate(context)
                                pendingFontName = font.name
                            }
                        )
                    }
                    SettingsSection.CURRENCY -> {
                        SectionHeader(
                            title = if (isBangla) "নোট" else "Currency",
                            subtitle = if (isBangla) "ক্যালকুলেটরে কোন নোটগুলো দেখানো হবে তা বেছে নিন" else "Pick which notes show on the calculator homepage"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CurrencyContent(
                            isBangla = isBangla,
                            disabledDenominations = uiState.disabledDenominations,
                            onToggle = { value, checked -> viewModel.toggleDenomination(value, checked) }
                        )
                    }
                    SettingsSection.MISCELLANEOUS -> {
                        MiscellaneousContent(
                            isBangla = isBangla,
                            biometricEnabled = uiState.biometricEnabled,
                            screenshotBlockEnabled = uiState.screenshotBlockEnabled,
                            keepScreenOnEnabled = uiState.keepScreenOnEnabled,
                            dynamicColorEnabled = uiState.dynamicColorEnabled,
                            hapticEnabled = uiState.hapticFeedbackEnabled,
                            hapticIntensity = uiState.hapticFeedbackIntensity,
                            onBiometricToggle = onBiometricToggle,
                            onScreenshotToggle = { checked -> viewModel.setScreenshotBlockEnabled(checked) },
                            onKeepScreenOnToggle = { checked ->
                                HapticHelper.vibrate(context)
                                viewModel.setKeepScreenOnEnabled(checked)
                            },
                            onDynamicColorToggle = { checked ->
                                HapticHelper.vibrate(context)
                                viewModel.setDynamicColorEnabled(checked)
                            },
                            onHapticToggle = { checked -> viewModel.setHapticFeedbackEnabled(checked) },
                            onIntensityChange = { v -> viewModel.setHapticFeedbackIntensity(v) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        BackupRestoreCard(
                            isBangla = isBangla,
                            onBackup = {
                                HapticHelper.vibrate(context)
                                viewModel.backupData(context)
                            },
                            onRestore = {
                                HapticHelper.vibrate(context)
                                restoreFileLauncher.launch("application/json")
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ResetAllCard(
                            isBangla = isBangla,
                            onClick = {
                                HapticHelper.vibrate(context)
                                viewModel.openResetDialog()
                            }
                        )
                    }
                }
            }
            VerticalScrollbarIndicator(
                state = scrollState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }

    // Explicit Confirmation Dialog for Reset All Data — Reset stays disabled during the 10s countdown
    if (uiState.showResetConfirmationDialog) {
        val resetCountdown = uiState.resetCountdown
        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                viewModel.dismissResetDialog()
            },
            title = { Text(if (isBangla) "সব ডেটা রিসেট করবেন?" else "Reset All Data?") },
            text = {
                Text(
                    if (isBangla) "এটি সব সেভ করা হিসাব, ইতিহাস ও সেটিংস স্থায়ীভাবে মুছে ফেলবে। এই কাজটি আর পূর্বাবস্থায় ফেরানো যাবে না।"
                    else "This will permanently delete all saved calculations, history, and user settings. This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.vibrate(context)
                        viewModel.confirmResetAllData()
                    },
                    enabled = resetCountdown <= 0,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.35f),
                        disabledContentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text = if (resetCountdown > 0) {
                            val secs = if (isBangla) {
                                BanglaDigitConverter.toBangla(resetCountdown)
                            } else {
                                resetCountdown.toString()
                            }
                            if (isBangla) "রিসেট করুন ($secs)" else "Reset ($secs)"
                        } else {
                            if (isBangla) "রিসেট করুন" else "Reset"
                        },
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
            title = { Text(if (isBangla) "ডেটা পুনরুদ্ধার করবেন?" else "Restore Data?") },
            text = {
                Text(
                    if (isBangla) "পুরোনো ডেটা পুনরুদ্ধার করলে আপনার বর্তমান ডেটা নষ্ট বা মুছে যেতে পারে।"
                    else "Restoring outdated/old data might corrupt or overwrite your present data."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.vibrate(context)
                        viewModel.confirmRestore(context)
                    },
                    enabled = countdown <= 0,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.35f),
                        disabledContentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text = if (countdown > 0) {
                            val secs = if (isBangla) {
                                BanglaDigitConverter.toBangla(countdown)
                            } else {
                                countdown.toString()
                            }
                            if (isBangla) "পুনরুদ্ধার করুন ($secs)" else "Restore ($secs)"
                        } else {
                            if (isBangla) "পুনরুদ্ধার করুন" else "Restore"
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

    // Discard dialog for the Theme/Language pickers — shown when leaving with an unapplied selection
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                showDiscardDialog = false
            },
            title = { Text(if (isBangla) "পরিবর্তন বাতিল করবেন?" else "Discard changes?") },
            text = {
                Text(
                    if (isBangla) "আপনার করা পরিবর্তনগুলো এখনো প্রয়োগ করা হয়নি।"
                    else "Your selection has not been applied yet."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    HapticHelper.vibrate(context)
                    showDiscardDialog = false
                    pendingThemeName = uiState.theme.name
                    pendingLangName = uiState.language.name
                    pendingFontName = uiState.font.name
                    onNavigateBack()
                }) {
                    Text(
                        if (isBangla) "পরিবর্তন বাতিল করুন" else "Discard",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    HapticHelper.vibrate(context)
                    showDiscardDialog = false
                }) {
                    Text(if (isBangla) "সম্পাদনা চালিয়ে যান" else "Keep editing")
                }
            }
        )
    }
}

@Composable
private fun ResetAllCard(isBangla: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    if (isBangla) "রিসেট করুন" else "Reset All",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    if (isBangla) "সব ডেটা ও সেটিংস মুছে ফেলুন" else "Delete all data and settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun BackupRestoreCard(
    isBangla: Boolean,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    SettingsCard {
        Text(
            if (isBangla) "ব্যাকআপ ও পুনরুদ্ধার" else "Backup & Restore",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        BackupRestoreRow(
            icon = Icons.Rounded.CloudUpload,
            title = if (isBangla) "ডেটা ব্যাকআপ" else "Backup Data",
            subtitle = if (isBangla) "ব্যাকআপ ফাইল ডাউনলোড ফোল্ডারে সেভ করুন" else "Save backup file to Downloads",
            onClick = onBackup
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        BackupRestoreRow(
            icon = Icons.Rounded.CloudDownload,
            title = if (isBangla) "ডেটা পুনরুদ্ধার" else "Restore Data",
            subtitle = if (isBangla) "ব্যাকআপ ফাইল থেকে পুনরুদ্ধার করুন" else "Restore from backup file",
            onClick = onRestore
        )
    }
}

@Composable
private fun BackupRestoreRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                HapticHelper.vibrate(context)
                onClick()
            }
            .padding(vertical = 6.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f), shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
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
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            content()
        }
    }
}

@Composable
private fun ThemeContent(
    isBangla: Boolean,
    pendingTheme: AppTheme,
    dynamicColorEnabled: Boolean,
    onSelect: (AppTheme) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppTheme.entries.forEach { theme ->
            ThemeOptionCard(
                isBangla = isBangla,
                theme = theme,
                selected = theme == pendingTheme,
                onClick = { onSelect(theme) }
            )
        }
        ThemePreviewCard(
            isBangla = isBangla,
            previewTheme = pendingTheme,
            dynamicColorEnabled = dynamicColorEnabled
        )
    }
}

@Composable
private fun ThemeOptionCard(
    isBangla: Boolean,
    theme: AppTheme,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (theme) {
                    AppTheme.SYSTEM -> if (isBangla) "সিস্টেম থিম" else "Follow System"
                    AppTheme.LIGHT -> if (isBangla) "লাইট থিম" else "Light Theme"
                    AppTheme.DARK -> if (isBangla) "ডার্ক থিম" else "Dark Theme"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when (theme) {
                    AppTheme.SYSTEM -> if (isBangla) "ডিভাইসের থিম অনুসরণ করুন" else "Match your device's theme"
                    AppTheme.LIGHT -> if (isBangla) "সবসময় লাইট থিম ব্যবহার করুন" else "Always use the light theme"
                    AppTheme.DARK -> if (isBangla) "সবসময় ডার্ক থিম ব্যবহার করুন" else "Always use the dark theme"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (selected) {
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun ThemePreviewCard(
    isBangla: Boolean,
    previewTheme: AppTheme,
    dynamicColorEnabled: Boolean
) {
    val previewIsDark = when (previewTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    val scheme = cashFigureColorScheme(isDark = previewIsDark, dynamicColor = dynamicColorEnabled)
    SettingsCard {
        Text(
            if (isBangla) "দেখুন" else "Preview",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(scheme.background)
                .border(1.dp, scheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(scheme.primary)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(scheme.onPrimary, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(6.dp)
                            .background(scheme.onPrimary.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(scheme.primaryContainer)
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(scheme.primary, RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "৳",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            if (isBangla) "৫,০০০ টাকা" else "৳ 5,000",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimaryContainer
                        )
                        Text(
                            if (isBangla) "১০ × ৫০০ টাকার নোট" else "10 × 500 Tk notes",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            PreviewNoteRow(scheme)
            Spacer(modifier = Modifier.height(6.dp))
            PreviewNoteRow(scheme)
        }
    }
}

@Composable
private fun PreviewNoteRow(scheme: ColorScheme) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(scheme.tertiary, RoundedCornerShape(5.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .background(scheme.surfaceVariant, RoundedCornerShape(5.dp))
        )
    }
}

@Composable
private fun LanguageContent(
    isBangla: Boolean,
    pendingLang: AppLanguage,
    onSelect: (AppLanguage) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LanguageOptionCard(
            isBangla = isBangla,
            lang = AppLanguage.ENGLISH,
            selected = pendingLang == AppLanguage.ENGLISH,
            onClick = { onSelect(AppLanguage.ENGLISH) }
        )
        LanguageOptionCard(
            isBangla = isBangla,
            lang = AppLanguage.BANGLA,
            selected = pendingLang == AppLanguage.BANGLA,
            onClick = { onSelect(AppLanguage.BANGLA) }
        )
        LanguageSampleCard(isBangla = isBangla, previewLang = pendingLang)
    }
}

@Composable
private fun LanguageOptionCard(
    isBangla: Boolean,
    lang: AppLanguage,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (lang == AppLanguage.ENGLISH) "English" else "বাংলা",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when (lang) {
                    AppLanguage.ENGLISH -> if (isBangla) "পুরো অ্যাপে ইংরেজি ব্যবহার করুন" else "Use English throughout the app"
                    AppLanguage.BANGLA -> if (isBangla) "পুরো অ্যাপে বাংলা ব্যবহার করুন" else "Use Bangla throughout the app"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (selected) {
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun LanguageSampleCard(
    isBangla: Boolean,
    previewLang: AppLanguage
) {
    val previewIsBangla = previewLang == AppLanguage.BANGLA
    val amount = if (previewIsBangla) {
        "মোট: ৳ " + CurrencyFormatter.formatNumber(12345L, useBengaliDigits = true)
    } else {
        "Total: ৳ " + CurrencyFormatter.formatNumber(12345L)
    }
    val words = if (previewIsBangla) {
        NumberToWordsConverter.toBangla(12345L)
    } else {
        NumberToWordsConverter.toEnglish(12345L)
    }
    SettingsCard {
        Text(
            if (isBangla) "নমুনা" else "Sample",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LanguageSampleRow(
            badge = if (previewIsBangla) "বাং" else "EN",
            badgeColor = MaterialTheme.colorScheme.primary,
            amount = amount,
            words = words
        )
    }
}

@Composable
private fun LanguageSampleRow(
    badge: String,
    badgeColor: Color,
    amount: String,
    words: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color = badgeColor.copy(alpha = 0.14f), shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                badge,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                amount,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                words,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CurrencyContent(
    isBangla: Boolean,
    disabledDenominations: Set<Int>,
    onToggle: (Int, Boolean) -> Unit
) {
    val context = LocalContext.current
    val togglableValues = listOf(1, 2, 5, 10, 20, 50)
    SettingsCard {
        Text(
            if (isBangla) "হোম পেজে কোন নোটগুলো থাকবে তা নিয়ন্ত্রণ করুন" else "Manage homepage notes",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        listOf(
            Triple(1, "1 Tk", "১ টাকা"),
            Triple(2, "2 Tk", "২ টাকা"),
            Triple(5, "5 Tk", "৫ টাকা"),
            Triple(10, "10 Tk", "১০ টাকা"),
            Triple(20, "20 Tk", "২০ টাকা"),
            Triple(50, "50 Tk", "৫০ টাকা")
        ).forEachIndexed { index, (value, labelEn, labelBn) ->
            if (index > 0) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            }
            val isEnabled = value !in disabledDenominations
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = isEnabled,
                        role = Role.Switch,
                        onValueChange = { checked ->
                            HapticHelper.vibrate(context)
                            onToggle(value, checked)
                        }
                    )
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isBangla) labelBn else labelEn,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Switch(checked = isEnabled, onCheckedChange = null)
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    CurrencySummaryCard(
        isBangla = isBangla,
        disabledDenominations = disabledDenominations,
        togglableValues = togglableValues
    )
    if (disabledDenominations.containsAll(togglableValues)) {
        Spacer(modifier = Modifier.height(12.dp))
        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isBangla) "এই নোটগুলো হোম পেজে দেখানো হবে না।"
                    else "These notes won't show on the homepage",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CurrencySummaryCard(
    isBangla: Boolean,
    disabledDenominations: Set<Int>,
    togglableValues: List<Int>
) {
    val enabledCount = togglableValues.count { it !in disabledDenominations }
    val enabledText = if (isBangla) {
        "${BanglaDigitConverter.toBangla(enabledCount)} / ${BanglaDigitConverter.toBangla(togglableValues.size)} চালু"
    } else {
        "$enabledCount of ${togglableValues.size} enabled"
    }
    SettingsCard {
        Text(
            if (isBangla) "সারসংক্ষেপ" else "Summary",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            if (isBangla) "১০০, ২০০, ৫০০ ও ১০০০ টাকার নোট সবসময় হোম পেজে থাকবে।"
            else "100, 200, 500 and 1000 Tk notes always stay on the homepage",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isBangla) "ছোট নোট (১–৫০ টাকা)" else "Small notes (1–50 Tk)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                enabledText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        val animatedProgress by animateFloatAsState(
            targetValue = enabledCount / togglableValues.size.toFloat(),
            animationSpec = tween(durationMillis = 400),
            label = "summaryProgress"
        )
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun MiscellaneousContent(
    isBangla: Boolean,
    biometricEnabled: Boolean,
    screenshotBlockEnabled: Boolean,
    keepScreenOnEnabled: Boolean,
    dynamicColorEnabled: Boolean,
    hapticEnabled: Boolean,
    hapticIntensity: Float,
    onBiometricToggle: (Boolean) -> Unit,
    onScreenshotToggle: (Boolean) -> Unit,
    onKeepScreenOnToggle: (Boolean) -> Unit,
    onDynamicColorToggle: (Boolean) -> Unit,
    onHapticToggle: (Boolean) -> Unit,
    onIntensityChange: (Float) -> Unit
) {
    val context = LocalContext.current
    SettingsCard {
        GroupLabel(text = if (isBangla) "নিরাপত্তা" else "Security")
        ToggleRow(
            icon = { Icon(Icons.Rounded.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) },
            title = if (isBangla) "অ্যাপ লক" else "App Lock",
            subtitle = if (isBangla) "অ্যাপ লক চালু করুন" else "Enable app lock",
            checked = biometricEnabled,
            onToggle = onBiometricToggle
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ToggleRow(
            icon = { Icon(Icons.Rounded.Security, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) },
            title = if (isBangla) "স্ক্রিন সুরক্ষা" else "Screen Protection",
            subtitle = if (isBangla) "স্ক্রিন ক্যাপচার বন্ধ রাখুন" else "Prevent screen capture",
            checked = screenshotBlockEnabled,
            onToggle = { checked ->
                HapticHelper.vibrate(context)
                onScreenshotToggle(checked)
            }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        GroupLabel(text = if (isBangla) "ডিসপ্লে ও ফিডব্যাক" else "Display & Feedback")
        ToggleRow(
            icon = { Icon(Icons.Rounded.BrightnessHigh, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) },
            title = if (isBangla) "স্ক্রিন চালু রাখুন" else "Keep Screen On",
            subtitle = if (isBangla) "বেশি ব্যাটারি খরচ হয়" else "Uses more battery",
            checked = keepScreenOnEnabled,
            onToggle = onKeepScreenOnToggle
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ToggleRow(
            icon = { Icon(Icons.Rounded.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) },
            title = if (isBangla) "ডায়নামিক কালার" else "Dynamic Color",
            subtitle = if (isBangla) "ম্যাটেরিয়াল ইউ-এর কালার ব্যবহার করুন" else "Use Material You colors",
            checked = dynamicColorEnabled,
            onToggle = onDynamicColorToggle
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ToggleRow(
            icon = { Icon(Icons.Rounded.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) },
            title = if (isBangla) "হ্যাপটিক ফিডব্যাক" else "Haptic Feedback",
            subtitle = if (isBangla) "বাটন ও টগল চাপলে কম্পন দিন" else "Vibrate on interactions",
            checked = hapticEnabled,
            onToggle = onHapticToggle
        )
        if (hapticEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                    if (isBangla) "কম্পনের তীব্রতা" else "Vibration Intensity",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Slider(
                        value = hapticIntensity,
                        onValueChange = onIntensityChange,
                        onValueChangeFinished = { HapticHelper.vibrate(context) },
                        valueRange = 0.1f..1.0f,
                        modifier = Modifier.weight(1f)
                    )
                    val percentage = (hapticIntensity * 100).toInt()
                    val intensityText = if (isBangla) {
                        BanglaDigitConverter.toBangla(percentage)
                    } else {
                        "$percentage"
                    }
                    Text(
                        text = "$intensityText%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    )
}

@Composable
private fun ToggleRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onToggle
            )
            .padding(vertical = 6.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

private fun authenticateWithFingerprint(
    context: Context,
    isBangla: Boolean,
    enabling: Boolean,
    onSuccess: () -> Unit
) {
    val activity = context as? FragmentActivity ?: return
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(
            if (enabling) {
                if (isBangla) "অ্যাপ লক চালু করুন" else "Enable App Lock"
            } else {
                if (isBangla) "অ্যাপ লক বন্ধ করুন" else "Disable App Lock"
            }
        )
        .setSubtitle(
            if (enabling) {
                if (isBangla) "অ্যাপ লক চালু করতে চাইলে নিশ্চিত করুন" else "Confirm to enable app lock"
            } else {
                if (isBangla) "অ্যাপ লক বন্ধ করতে চাইলে নিশ্চিত করুন" else "Confirm to disable app lock"
            }
        )
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()

    biometricPrompt.authenticate(promptInfo)
}

@Composable
private fun FontContent(
    isBangla: Boolean,
    pendingFont: AppFont,
    onSelect: (AppFont) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppFont.entries.forEach { font ->
            FontOptionCard(
                isBangla = isBangla,
                font = font,
                selected = font == pendingFont,
                onClick = { onSelect(font) }
            )
        }
        FontPreviewCard(isBangla = isBangla, pendingFont = pendingFont)
    }
}

@Composable
private fun FontOptionCard(
    isBangla: Boolean,
    font: AppFont,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (font) {
                    AppFont.DEFAULT -> if (isBangla) "ডিফল্ট" else "Default"
                    AppFont.GOOGLE_SANS_ROUNDED -> "Google Sans Rounded"
                    AppFont.GOOGLE_SANS_FLEX -> "Google Sans Flex"
                    AppFont.VOLTE_ROUND -> "Volte Round"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when (font) {
                    AppFont.DEFAULT -> if (isBangla) "ডিভাইসের সিস্টেম ফন্ট ব্যবহার করুন" else "Use your device's system font"
                    AppFont.GOOGLE_SANS_ROUNDED -> if (isBangla) "গোলাকার ও আধুনিক চেহারা" else "Rounded, modern look"
                    AppFont.GOOGLE_SANS_FLEX -> if (isBangla) "নমনীয় ও ভার্সেটাইল স্টাইল" else "Flexible, versatile style"
                    AppFont.VOLTE_ROUND -> if (isBangla) "পরিষ্কার ও গোলাকার ডিজাইন" else "Clean, rounded design"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (selected) {
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun FontPreviewCard(
    isBangla: Boolean,
    pendingFont: AppFont
) {
    val previewFamily = fontFamilyFor(pendingFont, isBangla)
    SettingsCard {
        Text(
            if (isBangla) "দেখুন" else "Preview",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.background)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(6.dp)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "৳",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = previewFamily),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            if (isBangla) "৫,০০০ টাকা" else "৳ 5,000",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = previewFamily),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            if (isBangla) "১০ × ৫০০ টাকার নোট" else "10 × 500 Tk notes",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = previewFamily),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            PreviewNoteRow(MaterialTheme.colorScheme)
            Spacer(modifier = Modifier.height(6.dp))
            PreviewNoteRow(MaterialTheme.colorScheme)
        }
    }
}

