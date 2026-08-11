package app.cash.tanvir.info.ui.screen.settingsdetail

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Vibration
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.data.local.preferences.AppTheme
import app.cash.tanvir.info.ui.screen.settings.SettingsViewModel
import app.cash.tanvir.info.util.BanglaDigitConverter
import app.cash.tanvir.info.util.HapticHelper

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
        SettingsSection.THEME -> if (isBangla) "অ্যাপ থিম" else "App Theme"
        SettingsSection.LANGUAGE -> if (isBangla) "ভাষা" else "Language"
        SettingsSection.CURRENCY -> if (isBangla) "নোটসমূহ" else "Currency"
        SettingsSection.MISCELLANEOUS -> if (isBangla) "টুলস" else "Miscellaneous"
    }

    val onBiometricToggle: (Boolean) -> Unit = { checked ->
        HapticHelper.vibrate(context)
        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                authenticateWithFingerprint(context, isBangla, enabling = checked) {
                    viewModel.setBiometricEnabled(checked)
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                if (checked) {
                    val msg = if (isBangla) "অনুগ্রহ করে আপনার ডিভাইসের সেটিংসে ফিঙ্গারপ্রিন্ট সেটআপ করুন。" else "Please set up fingerprint/biometrics in your device settings."
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                } else {
                    viewModel.setBiometricEnabled(false)
                }
            }
            else -> {
                val msg = if (isBangla) "এই ডিভাইসে ফিঙ্গারপ্রিন্ট সেটআপ নেই।" else "No fingerprint set up on this device."
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = {
                        HapticHelper.vibrate(context)
                        onNavigateBack()
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (section) {
                SettingsSection.THEME -> ThemeContent(isBangla, uiState.theme, viewModel::setTheme)
                SettingsSection.LANGUAGE -> LanguageContent(isBangla, uiState.language, viewModel::setLanguage)
                SettingsSection.CURRENCY -> CurrencyContent(
                    isBangla = isBangla,
                    disabledDenominations = uiState.disabledDenominations,
                    onToggle = { value, checked -> viewModel.toggleDenomination(value, checked) }
                )
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
    }

    // Explicit Confirmation Dialog for Reset All Data — Reset stays disabled during the 10s countdown
    if (uiState.showResetConfirmationDialog) {
        val resetCountdown = uiState.resetCountdown
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
                    },
                    enabled = resetCountdown <= 0
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
                    if (isBangla) "সব ডাটা ও সেটিংস মুছে ফেলুন" else "Delete all data and settings",
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
            if (isBangla) "ব্যাকআপ ও রিস্টোর" else "Backup & Restore",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        BackupRestoreRow(
            icon = Icons.Rounded.CloudUpload,
            title = if (isBangla) "ব্যাকআপ ডাটা" else "Backup Data",
            subtitle = if (isBangla) "ব্যাকআপ ফাইল সেভ করুন" else "Save backup file to Downloads",
            onClick = onBackup
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        BackupRestoreRow(
            icon = Icons.Rounded.CloudDownload,
            title = if (isBangla) "রিস্টোর ডাটা" else "Restore Data",
            subtitle = if (isBangla) "ব্যাকআপ ফাইল রিস্টোর করুন" else "Restore from backup file",
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
    current: AppTheme,
    onSelect: (AppTheme) -> Unit
) {
    val context = LocalContext.current
    SettingsCard {
        AppTheme.entries.forEach { theme ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        HapticHelper.vibrate(context)
                        onSelect(theme)
                    }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = current == theme, onClick = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (theme) {
                        AppTheme.SYSTEM -> if (isBangla) "সিস্টেম থিম" else "Follow System"
                        AppTheme.LIGHT -> if (isBangla) "লাইট থিম" else "Light Theme"
                        AppTheme.DARK -> if (isBangla) "ডার্ক থিম" else "Dark Theme"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun LanguageContent(
    isBangla: Boolean,
    current: AppLanguage,
    onSelect: (AppLanguage) -> Unit
) {
    val context = LocalContext.current
    SettingsCard {
        AppLanguage.entries.forEach { lang ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        HapticHelper.vibrate(context)
                        onSelect(lang)
                    }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = current == lang, onClick = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (lang == AppLanguage.ENGLISH) "English" else "বাংলা",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
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
    SettingsCard {
        Text(
            if (isBangla) "হোমপেজের নোটগুলো নিয়ন্ত্রণ করুন" else "Manage homepage notes",
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
            subtitle = if (isBangla) "ম্যাটেরিয়াল ইউ কালার ব্যবহার করুন (Android 12+)" else "Use Material You colors (Android 12+)",
            checked = dynamicColorEnabled,
            onToggle = onDynamicColorToggle
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ToggleRow(
            icon = { Icon(Icons.Rounded.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) },
            title = if (isBangla) "হ্যাপটিক ফিডব্যাক" else "Haptic Feedback",
            subtitle = if (isBangla) "বাটন ও টগলে কম্পন সক্রিয় করুন" else "Vibrate on interactions",
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
                if (isBangla) "অ্যাপ লক চালু করতে নিশ্চিত করুন" else "Confirm to enable app lock"
            } else {
                if (isBangla) "অ্যাপ লক বন্ধ করতে নিশ্চিত করুন" else "Confirm to disable app lock"
            }
        )
        .setNegativeButtonText(if (isBangla) "বাতিল" else "Cancel")
        .build()

    biometricPrompt.authenticate(promptInfo)
}
