package app.cash.tanvir.info.ui.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.data.local.preferences.AppTheme
import app.cash.tanvir.info.domain.model.DownloadedUpdate
import app.cash.tanvir.info.util.BanglaDigitConverter
import app.cash.tanvir.info.util.HapticHelper
import app.cash.tanvir.info.util.getInstalledVersion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    autoCheck: Boolean = false,
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

    // OTA install: opens "allow from this source" settings on API 26+ when blocked.
    val installSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val canInstall = Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()
        if (canInstall) {
            uiState.downloadedUpdate?.let { update ->
                viewModel.onInstallLaunched()
                launchInstaller(context, update) {
                    uiState.updateManifest?.downloadUrl?.let { url ->
                        openInBrowserFallback(context, url, isBangla)
                    }
                }
                viewModel.dismissUpdateDialog()
            }
        } else {
            // Still blocked after the settings round-trip → browser fallback
            uiState.updateManifest?.downloadUrl?.let { url ->
                openInBrowserFallback(context, url, isBangla)
            }
        }
    }

    val requestInstall: (DownloadedUpdate) -> Unit = { update ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            val msg = if (isBangla) "আপডেটের জন্য সেটিংসে এই অ্যাপ থেকে ইনস্টল অনুমতি দিন"
            else "Allow installs from this app in Settings to update Cash Figure"
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            installSettingsLauncher.launch(
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
            )
        } else {
            viewModel.onInstallLaunched()
            launchInstaller(context, update) {
                uiState.updateManifest?.downloadUrl?.let { url ->
                    openInBrowserFallback(context, url, isBangla)
                }
            }
            viewModel.dismissUpdateDialog()
        }
    }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearStatusMessage()
        }
    }

    var isThemeExpanded by remember { mutableStateOf(false) }
    var isLanguageExpanded by remember { mutableStateOf(false) }
    var isHomepageNotesExpanded by remember { mutableStateOf(false) }
    var isMiscExpanded by remember { mutableStateOf(false) }
    var isUpdatesExpanded by remember { mutableStateOf(autoCheck) }

    val onBiometricToggle: (Boolean) -> Unit = { checked ->
        HapticHelper.vibrate(context)
        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                authenticateWithFingerprint(context, isBangla) {
                    viewModel.setBiometricEnabled(checked)
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                if (checked) {
                    val msg = if (isBangla) "অনুগ্রহ করে আপনার ডিভাইসের সেটিংসে ফিঙ্গারপ্রিন্ট সেটআপ করুন।" else "Please set up fingerprint/biometrics in your device settings."
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                } else {
                    viewModel.setBiometricEnabled(false)
                }
            }
            else -> {
                if (checked) {
                    val msg = if (isBangla) "এই ডিভাইসে বায়োমেট্রিক অথেন্টিকেশন সমর্থিত বা উপলব্ধ নয়।" else "Biometric authentication is not supported or available on this device."
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                } else {
                    viewModel.setBiometricEnabled(false)
                }
            }
        }
    }

    val (versionName, versionCode) = remember(context) { getInstalledVersion(context) }

    // Auto-triggered check when arriving from the launch OTA dialog
    LaunchedEffect(Unit) {
        if (autoCheck) {
            viewModel.checkForUpdate(installedCode = versionCode, fromManualCheck = true)
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
            // Theme Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    val headerShape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isThemeExpanded) 0.dp else 16.dp,
                        bottomEnd = if (isThemeExpanded) 0.dp else 16.dp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(headerShape)
                            .clickable {
                                HapticHelper.vibrate(context)
                                isThemeExpanded = !isThemeExpanded
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    if (isBangla) "অ্যাপ থিম" else "App Theme",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!isThemeExpanded) {
                                    val currentThemeText = when (uiState.theme) {
                                        AppTheme.SYSTEM -> if (isBangla) "সিস্টেম থিম" else "Follow System"
                                        AppTheme.LIGHT -> if (isBangla) "লাইট থিম" else "Light Theme"
                                        AppTheme.DARK -> if (isBangla) "ডার্ক থিম" else "Dark Theme"
                                    }
                                    Text(
                                        text = currentThemeText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = if (isThemeExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isBangla) {
                                if (isThemeExpanded) "ভাঁজ করুন" else "প্রসারিত করুন"
                            } else {
                                if (isThemeExpanded) "Collapse" else "Expand"
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AnimatedVisibility(
                        visible = isThemeExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AppTheme.entries.forEach { theme ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            HapticHelper.vibrate(context)
                                            viewModel.setTheme(theme)
                                        }
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = uiState.theme == theme,
                                        onClick = {
                                            HapticHelper.vibrate(context)
                                            viewModel.setTheme(theme)
                                        }
                                    )
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
                }
            }

            // Language Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    val headerShape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isLanguageExpanded) 0.dp else 16.dp,
                        bottomEnd = if (isLanguageExpanded) 0.dp else 16.dp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(headerShape)
                            .clickable {
                                HapticHelper.vibrate(context)
                                isLanguageExpanded = !isLanguageExpanded
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    if (isBangla) "ভাষা" else "Language",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!isLanguageExpanded) {
                                    val currentLangText = when (uiState.language) {
                                        AppLanguage.ENGLISH -> if (isBangla) "ইংরেজি" else "English"
                                        AppLanguage.BANGLA -> if (isBangla) "বাংলা" else "Bangla"
                                    }
                                    Text(
                                        text = currentLangText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = if (isLanguageExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isBangla) {
                                if (isLanguageExpanded) "ভাঁজ করুন" else "প্রসারিত করুন"
                            } else {
                                if (isLanguageExpanded) "Collapse" else "Expand"
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AnimatedVisibility(
                        visible = isLanguageExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        HapticHelper.vibrate(context)
                                        viewModel.setLanguage(AppLanguage.ENGLISH)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                  RadioButton(
                                      selected = uiState.language == AppLanguage.ENGLISH,
                                      onClick = {
                                          HapticHelper.vibrate(context)
                                          viewModel.setLanguage(AppLanguage.ENGLISH)
                                      }
                                  )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isBangla) "ইংরেজি" else "English", style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        HapticHelper.vibrate(context)
                                        viewModel.setLanguage(AppLanguage.BANGLA)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                  RadioButton(
                                      selected = uiState.language == AppLanguage.BANGLA,
                                      onClick = {
                                          HapticHelper.vibrate(context)
                                          viewModel.setLanguage(AppLanguage.BANGLA)
                                      }
                                  )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isBangla) "বাংলা" else "Bangla", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Homepage Notes Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    val headerShape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isHomepageNotesExpanded) 0.dp else 16.dp,
                        bottomEnd = if (isHomepageNotesExpanded) 0.dp else 16.dp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(headerShape)
                            .clickable {
                                HapticHelper.vibrate(context)
                                isHomepageNotesExpanded = !isHomepageNotesExpanded
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    if (isBangla) "নোটসমূহ" else "Currency",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!isHomepageNotesExpanded) {
                                    Text(
                                        if (isBangla) "হোমপেজের নোটগুলো নিয়ন্ত্রণ করুন" else "Manage homepage notes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = if (isHomepageNotesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isBangla) {
                                if (isHomepageNotesExpanded) "ভাঁজ করুন" else "প্রসারিত করুন"
                            } else {
                                if (isHomepageNotesExpanded) "Collapse" else "Expand"
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AnimatedVisibility(
                        visible = isHomepageNotesExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (isBangla) "হোমপেজের নোটগুলো নিয়ন্ত্রণ করুন" else "Manage homepage notes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
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
                                val isEnabled = value !in uiState.disabledDenominations
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .toggleable(
                                            value = isEnabled,
                                            role = Role.Switch,
                                            onValueChange = { checked ->
                                                HapticHelper.vibrate(context)
                                                viewModel.toggleDenomination(value, checked)
                                            }
                                        )
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
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
                                    Switch(
                                        checked = isEnabled,
                                        onCheckedChange = null
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Miscellaneous Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    val headerShape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMiscExpanded) 0.dp else 16.dp,
                        bottomEnd = if (isMiscExpanded) 0.dp else 16.dp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(headerShape)
                            .clickable {
                                HapticHelper.vibrate(context)
                                isMiscExpanded = !isMiscExpanded
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    if (isBangla) "টুলস" else "Miscellaneous",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!isMiscExpanded) {
                                    Text(
                                        if (isBangla) "অতিরিক্ত ফিচারসমূহ" else "Add-on Features",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = if (isMiscExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isBangla) {
                                if (isMiscExpanded) "ভাঁজ করুন" else "প্রসারিত করুন"
                            } else {
                                if (isMiscExpanded) "Collapse" else "Expand"
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AnimatedVisibility(
                        visible = isMiscExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(4.dp))

                            // Fingerprint Lock Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .toggleable(
                                        value = uiState.biometricEnabled,
                                        role = Role.Switch,
                                        onValueChange = onBiometricToggle
                                    )
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp)
                                ) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            if (isBangla) "অ্যাপ লক" else "App Lock",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            if (isBangla) "অ্যাপ লক চালু করুন" else "Enable app lock",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Switch(
                                    checked = uiState.biometricEnabled,
                                    onCheckedChange = null
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                            // Screenshot Block Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .toggleable(
                                        value = uiState.screenshotBlockEnabled,
                                        role = Role.Switch,
                                        onValueChange = { checked ->
                                            HapticHelper.vibrate(context)
                                            viewModel.setScreenshotBlockEnabled(checked)
                                        }
                                    )
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp)
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            if (isBangla) "স্ক্রিন সুরক্ষা" else "Screen Protection",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            if (isBangla) "স্ক্রিন ক্যাপচার বন্ধ রাখুন" else "Prevent screen capture",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Switch(
                                    checked = uiState.screenshotBlockEnabled,
                                    onCheckedChange = null
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                            // Haptic Feedback Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .toggleable(
                                        value = uiState.hapticFeedbackEnabled,
                                        role = Role.Switch,
                                        onValueChange = { checked ->
                                            HapticHelper.vibrate(context)
                                            viewModel.setHapticFeedbackEnabled(checked)
                                        }
                                    )
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp)
                                ) {
                                    Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            if (isBangla) "হ্যাপটিক ফিডব্যাক" else "Haptic Feedback",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            if (isBangla) "বাটন ও টগলে কম্পন সক্রিয় করুন" else "Vibrate on interactions",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Switch(
                                    checked = uiState.hapticFeedbackEnabled,
                                    onCheckedChange = null
                                )
                            }

                            if (uiState.hapticFeedbackEnabled) {
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
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Slider(
                                            value = uiState.hapticFeedbackIntensity,
                                            onValueChange = { newValue ->
                                                viewModel.setHapticFeedbackIntensity(newValue)
                                            },
                                            onValueChangeFinished = {
                                                HapticHelper.vibrate(context)
                                            },
                                            valueRange = 0.1f..1.0f,
                                            modifier = Modifier.weight(1f)
                                        )
                                        val percentage = (uiState.hapticFeedbackIntensity * 100).toInt()
                                        val intensityText = if (isBangla) {
                                            "${app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(percentage)}%"
                                        } else {
                                            "$percentage%"
                                        }
                                        Text(
                                            text = intensityText,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

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
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                HapticHelper.vibrate(context)
                                viewModel.backupData(context)
                            }
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                HapticHelper.vibrate(context)
                                restoreFileLauncher.launch("application/json")
                            }
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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

            // Updates Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    val headerShape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUpdatesExpanded) 0.dp else 16.dp,
                        bottomEnd = if (isUpdatesExpanded) 0.dp else 16.dp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(headerShape)
                            .clickable {
                                HapticHelper.vibrate(context)
                                isUpdatesExpanded = !isUpdatesExpanded
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    if (isBangla) "আপডেটসমূহ" else "Updates",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!isUpdatesExpanded) {
                                    Text(
                                        if (isBangla) "নতুন ভার্সন চেক করুন" else "Check for new versions",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = if (isUpdatesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isBangla) {
                                if (isUpdatesExpanded) "ভাঁজ করুন" else "প্রসারিত করুন"
                            } else {
                                if (isUpdatesExpanded) "Collapse" else "Expand"
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AnimatedVisibility(
                        visible = isUpdatesExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(4.dp))
                            val versionLine = if (isBangla) {
                                "বর্তমান ভার্সন: ${BanglaDigitConverter.toBangla(versionName.removePrefix("v"))} (বিল্ড ${BanglaDigitConverter.toBangla(versionCode)})"
                            } else {
                                "Current version: ${versionName.removePrefix("v")} (Build $versionCode)"
                            }
                            Text(
                                text = versionLine,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                            // Check for updates row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        HapticHelper.vibrate(context)
                                        viewModel.checkForUpdate(installedCode = versionCode, fromManualCheck = true)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        if (isBangla) "আপডেট চেক করুন" else "Check for updates",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        if (isBangla) "সর্বশেষ ভার্সন পরীক্ষা করুন" else "Check for the latest version",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                            UpdateStatusLine(
                                isBangla = isBangla,
                                updateStatus = uiState.updateStatus,
                                manifest = uiState.updateManifest,
                                downloadedUpdate = uiState.downloadedUpdate,
                                downloadProgress = uiState.downloadProgress,
                                errorType = uiState.updateErrorType,
                                errorReason = uiState.updateErrorReason,
                                onCheckForUpdate = {
                                    viewModel.checkForUpdate(installedCode = versionCode, fromManualCheck = true)
                                },
                                onShowUpdateDialog = { viewModel.showUpdateDialog() },
                                onInstall = { update -> requestInstall(update) }
                            )
                        }
                    }
                }
            }

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
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
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

            // About Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            if (isBangla) "ক্যাশ ফিগার সম্পর্কে" else "About Cash Figure",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val formattedVersionName = versionName.removePrefix("v")
                    val versionText = if (isBangla) {
                        "ভার্সন ${BanglaDigitConverter.toBangla(formattedVersionName)} (বিল্ড ${BanglaDigitConverter.toBangla(versionCode)})"
                    } else {
                        "Version $formattedVersionName (Build $versionCode)"
                    }
                    Text(
                        text = versionText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
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
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        if (isBangla) "সবকিছু রিসেট করুন" else "Reset Everything",
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

    // Warning Dialog for Restore Data
    if (uiState.showRestoreWarningDialog) {
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
                    }
                ) {
                    Text(
                        if (isBangla) "রিস্টোর করুন" else "Restore",
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

    // Update Dialog
    if (uiState.isUpdateDialogVisible && uiState.updateManifest != null) {
        val manifest = uiState.updateManifest!!
        AlertDialog(
            onDismissRequest = {
                HapticHelper.vibrate(context)
                viewModel.dismissUpdateDialog()
            },
            title = {
                Text(
                    if (isBangla) "আপডেট উপলব্ধ" else "Update available",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .heightIn(max = 320.dp)
                ) {
                    Text(
                        text = if (isBangla) {
                            "ভার্সন ${BanglaDigitConverter.toBangla(manifest.versionName)} · বিল্ড ${BanglaDigitConverter.toBangla(manifest.versionCode)}"
                        } else {
                            "Version ${manifest.versionName} · Build ${manifest.versionCode}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (manifest.changelog.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (isBangla) "নতুন কী আছে" else "What's new",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        var isFirstBullet = true
                        manifest.changelog.split("\n").forEach { rawLine ->
                            val isIndented = rawLine.startsWith(" ") || rawLine.startsWith("\t")
                            val trimmed = rawLine.trim()
                            when {
                                trimmed.startsWith("*") -> {
                                    val clean = trimmed.removePrefix("*")
                                        .trim()
                                        .removePrefix("**")
                                        .removeSuffix("**")
                                        .trim()
                                    if (clean.isNotEmpty()) {
                                        if (!isFirstBullet) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        isFirstBullet = false
                                        Text(
                                            text = "• $clean",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                isIndented && trimmed.startsWith("-") -> {
                                    val clean = trimmed.removePrefix("-").trim()
                                    if (clean.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "◦ $clean",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                }
                                trimmed.isNotEmpty() -> {
                                    if (!isFirstBullet) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    isFirstBullet = false
                                    Text(
                                        text = "• $trimmed",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                    when (uiState.updateStatus) {
                        UpdateStatus.DOWNLOAD_READY -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                if (isBangla) "ডাউনলোড সম্পন্ন।" else "Download complete.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        UpdateStatus.ERROR -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            val errorText = when (uiState.updateErrorType) {
                                UpdateErrorType.DOWNLOAD_FAILED -> if (isBangla) {
                                    "ডাউনলোড ব্যর্থ হয়েছে: ${uiState.updateErrorReason ?: ""}"
                                } else {
                                    "Download failed: ${uiState.updateErrorReason ?: ""}"
                                }
                                else -> if (isBangla) {
                                    "আপডেট চেক করা যায়নি। ইন্টারনেট সংযোগ পরীক্ষা করে আবার চেষ্টা করুন।"
                                } else {
                                    "Couldn't check for updates. Check your connection and try again."
                                }
                            }
                            Text(
                                text = errorText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        else -> {}
                    }
                }
            },
            confirmButton = {
                when (uiState.updateStatus) {
                    UpdateStatus.UPDATE_AVAILABLE -> Button(
                        onClick = {
                            HapticHelper.vibrate(context)
                            viewModel.downloadUpdate()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            if (isBangla) "আপডেট ও ইনস্টল" else "Update & Install",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    UpdateStatus.DOWNLOADING -> Button(
                        onClick = {},
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.widthIn(min = 180.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = LocalContentColor.current
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val pct = (uiState.downloadProgress * 100).toInt().coerceIn(0, 100)
                        val progressText = if (uiState.downloadProgress >= 0f) {
                            if (isBangla) {
                                "ডাউনলোড হচ্ছে… ${BanglaDigitConverter.toBangla(pct)}%"
                            } else {
                                "Downloading… $pct%"
                            }
                        } else {
                            if (isBangla) "ডাউনলোড হচ্ছে…" else "Downloading…"
                        }
                        Text(progressText)
                    }
                    UpdateStatus.DOWNLOAD_READY -> Button(
                        onClick = {
                            HapticHelper.vibrate(context)
                            uiState.downloadedUpdate?.let { requestInstall(it) }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            if (isBangla) "ইনস্টল করুন" else "Install",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    UpdateStatus.ERROR -> Button(
                        onClick = {
                            HapticHelper.vibrate(context)
                            viewModel.checkForUpdate(installedCode = versionCode, fromManualCheck = true)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            if (isBangla) "আবার চেষ্টা করুন" else "Retry",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    else -> {}
                }
            },
            dismissButton = {
                when (uiState.updateStatus) {
                    UpdateStatus.UPDATE_AVAILABLE, UpdateStatus.DOWNLOAD_READY -> OutlinedButton(
                        onClick = {
                            HapticHelper.vibrate(context)
                            viewModel.dismissUpdateDialog()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isBangla) "পরে" else "Later")
                    }
                    UpdateStatus.DOWNLOADING, UpdateStatus.ERROR -> OutlinedButton(
                        onClick = {
                            HapticHelper.vibrate(context)
                            viewModel.dismissUpdateDialog()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isBangla) "বাতিল" else "Cancel")
                    }
                    else -> {}
                }
            }
        )
    }
}

private fun authenticateWithFingerprint(
    context: Context,
    isBangla: Boolean,
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
        .setTitle(if (isBangla) "ক্যাশ ফিগার অ্যাপ আনলক করুন" else "Unlock Cash Figure App")
        .setSubtitle(if (isBangla) "আপনার পরিচয় যাচাই করুন" else "Verify it’s you")
        .setNegativeButtonText(if (isBangla) "বাতিল" else "Cancel")
        .build()

    biometricPrompt.authenticate(promptInfo)
}

@Composable
private fun UpdateStatusLine(
    isBangla: Boolean,
    updateStatus: UpdateStatus,
    manifest: app.cash.tanvir.info.domain.model.UpdateManifest?,
    downloadedUpdate: DownloadedUpdate?,
    downloadProgress: Float,
    errorType: UpdateErrorType?,
    errorReason: String?,
    onCheckForUpdate: () -> Unit,
    onShowUpdateDialog: () -> Unit,
    onInstall: (DownloadedUpdate) -> Unit
) {
    val context = LocalContext.current
    when (updateStatus) {
        UpdateStatus.IDLE -> {}
        UpdateStatus.CHECKING -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (isBangla) "আপডেট চেক হচ্ছে…" else "Checking for updates…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        UpdateStatus.UP_TO_DATE -> {
            Text(
                if (isBangla) "আপনি সর্বশেষ ভার্সনে আছেন" else "You're on the latest version",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
            )
        }
        UpdateStatus.UPDATE_AVAILABLE -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = if (isBangla) {
                        "নতুন ভার্সন ${BanglaDigitConverter.toBangla(manifest?.versionName ?: "")} পাওয়া গেছে"
                    } else {
                        "New version ${manifest?.versionName ?: ""} is available"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Button(
                    onClick = {
                        HapticHelper.vibrate(context)
                        onShowUpdateDialog()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBangla) "এখনই আপডেট করুন" else "Update now")
                }
            }
        }
        UpdateStatus.DOWNLOADING -> {
            Text(
                text = if (downloadProgress >= 0f) {
                    val pct = (downloadProgress * 100).toInt()
                    if (isBangla) "ডাউনলোড হচ্ছে… ${BanglaDigitConverter.toBangla(pct)}%" else "Downloading… $pct%"
                } else {
                    if (isBangla) "ডাউনলোড হচ্ছে…" else "Downloading…"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
            )
        }
        UpdateStatus.DOWNLOAD_READY -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    if (isBangla) "ডাউনলোড সম্পন্ন — ইনস্টল করুন" else "Download complete — install to update",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                TextButton(
                    onClick = {
                        HapticHelper.vibrate(context)
                        downloadedUpdate?.let { onInstall(it) }
                    }
                ) {
                    Text(if (isBangla) "ইনস্টল করুন" else "Install")
                }
            }
        }
        UpdateStatus.ERROR -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = if (errorType == UpdateErrorType.DOWNLOAD_FAILED) {
                        if (isBangla) "ডাউনলোড ব্যর্থ হয়েছে${errorReason?.let { ": $it" } ?: ""}" else "Download failed${errorReason?.let { ": $it" } ?: ""}"
                    } else {
                        if (isBangla) "আপডেট চেক করা যায়নি" else "Couldn't check for updates"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                TextButton(onClick = {
                    HapticHelper.vibrate(context)
                    onCheckForUpdate()
                }) {
                    Text(if (isBangla) "আবার চেষ্টা করুন" else "Retry")
                }
            }
        }
        UpdateStatus.INSTALLING -> {}
    }
}

/**
 * Launches the system package installer for a downloaded APK.
 * URI selection per API level: MediaStore content URI on 29+, FileProvider on ≤ 28.
 * [onUnresolved] fires when no activity can handle the install intent (browser fallback).
 */
private fun launchInstaller(
    context: Context,
    update: DownloadedUpdate,
    onUnresolved: () -> Unit
) {
    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Uri.parse(update.uri.toString())
    } else {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", update.file!!)
    }
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        onUnresolved()
    }
}

private fun openInBrowserFallback(context: Context, url: String, isBangla: Boolean) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(
            context,
            if (isBangla) "ডাউনলোড লিংক খোলা যায়নি" else "Couldn't open the download link",
            Toast.LENGTH_LONG
        ).show()
    }
}
