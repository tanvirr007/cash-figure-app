package app.cash.tanvir.info.ui.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restore
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.data.local.preferences.AppTheme
import app.cash.tanvir.info.util.BanglaDigitConverter
import app.cash.tanvir.info.util.HapticHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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

    var isThemeExpanded by remember { mutableStateOf(false) }
    var isLanguageExpanded by remember { mutableStateOf(false) }
    var isHomepageNotesExpanded by remember { mutableStateOf(false) }
    var isMiscExpanded by remember { mutableStateOf(false) }

    val isBangla = uiState.language == AppLanguage.BANGLA

    val (versionName, versionCode) = remember(context) {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            val vName = packageInfo.versionName ?: "1.0.0"
            val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            Pair(vName, vCode)
        } catch (e: Exception) {
            Pair("1.0.0", 1L)
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
                        Icon(Icons.Default.ArrowBack, contentDescription = if (isBangla) "ফিরে যান" else "Back")
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
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
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
                            contentDescription = if (isThemeExpanded) "Collapse" else "Expand",
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
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
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
                            contentDescription = if (isLanguageExpanded) "Collapse" else "Expand",
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
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Column {
                                Text(
                                    if (isBangla) "হোমপেজ নোটসমূহ" else "Homepage Notes",
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
                            contentDescription = if (isHomepageNotesExpanded) "Collapse" else "Expand",
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
                                        .clickable {
                                            HapticHelper.vibrate(context)
                                            viewModel.toggleDenomination(value, !isEnabled)
                                        }
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isBangla) labelBn else labelEn,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Switch(
                                        checked = isEnabled,
                                        onCheckedChange = { checked ->
                                            HapticHelper.vibrate(context)
                                            viewModel.toggleDenomination(value, checked)
                                        }
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
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
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
                            contentDescription = if (isMiscExpanded) "Collapse" else "Expand",
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
                                    .clickable {
                                        HapticHelper.vibrate(context)
                                        val targetState = !uiState.biometricEnabled
                                        if (targetState) {
                                            val biometricManager = BiometricManager.from(context)
                                            val canAuthenticate = biometricManager.canAuthenticate(BIOMETRIC_STRONG)
                                            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                                                viewModel.setBiometricEnabled(true)
                                            } else if (canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                                                val msg = if (isBangla) "অনুগ্রহ করে আপনার ডিভাইসের সেটিংসে ফিঙ্গারপ্রিন্ট সেটআপ করুন।" else "Please set up fingerprint/biometrics in your device settings."
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            } else {
                                                val msg = if (isBangla) "এই ডিভাইসে বায়োমেট্রিক অথেন্টিকেশন সমর্থিত বা উপলব্ধ নয়।" else "Biometric authentication is not supported or available on this device."
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            viewModel.setBiometricEnabled(false)
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            if (isBangla) "অ্যাপ লক" else "App Lock",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            if (isBangla) "অ্যাপ লক চালু করুন" else "Enable app lock",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Switch(
                                    checked = uiState.biometricEnabled,
                                    onCheckedChange = { checked ->
                                        HapticHelper.vibrate(context)
                                        if (checked) {
                                            val biometricManager = BiometricManager.from(context)
                                            val canAuthenticate = biometricManager.canAuthenticate(BIOMETRIC_STRONG)
                                            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                                                viewModel.setBiometricEnabled(true)
                                            } else if (canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                                                val msg = if (isBangla) "অনুগ্রহ করে আপনার ডিভাইসের সেটিংসে ফিঙ্গারপ্রিন্ট সেটআপ করুন।" else "Please set up fingerprint/biometrics in your device settings."
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            } else {
                                                val msg = if (isBangla) "এই ডিভাইসে বায়োমেট্রিক অথেন্টিকেশন সমর্থিত বা উপলব্ধ নয়।" else "Biometric authentication is not supported or available on this device."
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            viewModel.setBiometricEnabled(false)
                                        }
                                    }
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                            // Screenshot Block Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        HapticHelper.vibrate(context)
                                        viewModel.setScreenshotBlockEnabled(!uiState.screenshotBlockEnabled)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            if (isBangla) "স্ক্রিন সুরক্ষা" else "Screen Protection",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            if (isBangla) "স্ক্রিনশট ও ভিডিও রেকর্ড ব্লক করুন" else "Block screenshots and screen recording",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Switch(
                                    checked = uiState.screenshotBlockEnabled,
                                    onCheckedChange = { checked ->
                                        HapticHelper.vibrate(context)
                                        viewModel.setScreenshotBlockEnabled(checked)
                                    }
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                            // Haptic Feedback Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        HapticHelper.vibrate(context)
                                        viewModel.setHapticFeedbackEnabled(!uiState.hapticFeedbackEnabled)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            if (isBangla) "হ্যাপটিক ফিডব্যাক" else "Haptic Feedback",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            if (isBangla) "বাটন ও টগলে কম্পন সক্রিয় করুন" else "Vibrate on interactions",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Switch(
                                    checked = uiState.hapticFeedbackEnabled,
                                    onCheckedChange = { checked ->
                                        HapticHelper.vibrate(context)
                                        viewModel.setHapticFeedbackEnabled(checked)
                                    }
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
                                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
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
                                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
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
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
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
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
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
}
