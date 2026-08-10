package app.cash.tanvir.info.ui.screen.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.activityViewModels
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.domain.model.DownloadedUpdate
import app.cash.tanvir.info.ui.screen.settings.SettingsViewModel
import app.cash.tanvir.info.ui.screen.settings.UpdateErrorType
import app.cash.tanvir.info.ui.screen.settings.UpdateStatus
import app.cash.tanvir.info.util.BanglaDigitConverter
import app.cash.tanvir.info.util.ChangelogParser
import app.cash.tanvir.info.util.HapticHelper
import app.cash.tanvir.info.util.SizeFormatter
import app.cash.tanvir.info.util.getInstalledVersion

/**
 * Full-page Pixel-style updater. Shares the update state machine with the
 * Settings screen via the activity-scoped [SettingsViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: SettingsViewModel = activityViewModels()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isBangla = uiState.language == AppLanguage.BANGLA
    val (_, installedCode) = remember(context) { getInstalledVersion(context) }

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
            }
        } else {
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
        }
    }

    LaunchedEffect(Unit) {
        if (uiState.updateStatus == UpdateStatus.IDLE ||
            uiState.updateStatus == UpdateStatus.UP_TO_DATE
        ) {
            viewModel.checkForUpdate(installedCode = installedCode, fromManualCheck = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBangla) "আপডেট" else "Update") },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState.updateStatus) {
                UpdateStatus.IDLE, UpdateStatus.CHECKING -> {
                    Spacer(modifier = Modifier.height(120.dp))
                    CircularProgressIndicator(modifier = Modifier.size(40.dp), strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (isBangla) "আপডেট চেক হচ্ছে…" else "Checking for updates…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                UpdateStatus.UP_TO_DATE -> {
                    Spacer(modifier = Modifier.height(100.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(96.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        if (isBangla) "আপনি সর্বশেষ ভার্সনে আছেন" else "You're up to date",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (isBangla) "আপনার ডিভাইসে ${BanglaDigitConverter.toBangla(installedCode)} বিল্ড ইনস্টল করা আছে"
                        else "Build $installedCode is installed on this device",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    TextButton(onClick = {
                        HapticHelper.vibrate(context)
                        viewModel.checkForUpdate(installedCode = installedCode, fromManualCheck = true)
                    }) {
                        Text(if (isBangla) "আবার চেক করুন" else "Check again")
                    }
                }

                UpdateStatus.UPDATE_AVAILABLE -> {
                    uiState.updateManifest?.let { manifest ->
                        Spacer(modifier = Modifier.height(32.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    if (isBangla) "নতুন ভার্সন উপলব্ধ" else "New version available",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                InfoRow(
                                    label = if (isBangla) "নাম" else "Name",
                                    value = "Cash Figure"
                                )
                                InfoRow(
                                    label = if (isBangla) "ভার্সন" else "Version",
                                    value = if (isBangla) {
                                        BanglaDigitConverter.toBangla(manifest.versionName.removePrefix("v"))
                                    } else {
                                        manifest.versionName.removePrefix("v")
                                    }
                                )
                                InfoRow(
                                    label = if (isBangla) "বিল্ড" else "Build",
                                    value = if (isBangla) {
                                        BanglaDigitConverter.toBangla(manifest.versionCode)
                                    } else {
                                        manifest.versionCode.toString()
                                    }
                                )
                                manifest.fileSize?.let { size ->
                                    InfoRow(
                                        label = if (isBangla) "আকার" else "Size",
                                        value = SizeFormatter.format(size)
                                    )
                                }
                                if (manifest.changelog.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        if (isBangla) "নতুন কী আছে" else "What's new",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    manifest.changelog.split("\n").forEach { rawLine ->
                                        val isIndented = rawLine.startsWith(" ") || rawLine.startsWith("\t")
                                        val trimmed = rawLine.trim()
                                        when {
                                            trimmed.startsWith("*") -> {
                                                val clean = ChangelogParser.stripCommitHash(
                                                    trimmed.removePrefix("*")
                                                        .trim()
                                                        .removePrefix("**")
                                                        .removeSuffix("**")
                                                        .trim()
                                                )
                                                if (clean.isNotEmpty()) {
                                                    Text(
                                                        text = "• $clean",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    )
                                                }
                                            }
                                            isIndented && trimmed.startsWith("-") -> {
                                                val clean = trimmed.removePrefix("-").trim()
                                                if (clean.isNotEmpty()) {
                                                    Text(
                                                        text = "◦ $clean",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                                                    )
                                                }
                                            }
                                            trimmed.isNotEmpty() -> {
                                                Text(
                                                    text = "• $trimmed",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                HapticHelper.vibrate(context)
                                viewModel.dismissUpdateDialog()
                                onNavigateBack()
                            }) {
                                Text(if (isBangla) "পরে" else "Later")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    HapticHelper.vibrate(context)
                                    viewModel.downloadUpdate()
                                },
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    if (isBangla) "আপডেট" else "Update",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                UpdateStatus.DOWNLOADING -> {
                    Spacer(modifier = Modifier.height(100.dp))
                    Text(
                        if (isBangla) "ফাইল পাওয়া যাচ্ছে…" else "Getting file…",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.downloadedBytes > 0) {
                            val total = if (uiState.totalBytes > 0) {
                                " / ${SizeFormatter.format(uiState.totalBytes)}"
                            } else {
                                ""
                            }
                            "${SizeFormatter.format(uiState.downloadedBytes)}$total"
                        } else {
                            if (isBangla) "অপেক্ষা করুন…" else "Please wait…"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        progress = { uiState.downloadProgress.coerceIn(0f, 1f) }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            HapticHelper.vibrate(context)
                            viewModel.cancelDownload()
                            onNavigateBack()
                        }) {
                            Text(if (isBangla) "বাতিল" else "Cancel")
                        }
                    }
                }

                UpdateStatus.DOWNLOAD_READY -> {
                    Spacer(modifier = Modifier.height(100.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(96.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        if (isBangla) "ডাউনলোড সম্পন্ন" else "Download complete",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            HapticHelper.vibrate(context)
                            viewModel.dismissUpdateDialog()
                            onNavigateBack()
                        }) {
                            Text(if (isBangla) "পরে" else "Later")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                HapticHelper.vibrate(context)
                                uiState.downloadedUpdate?.let { requestInstall(it) }
                            },
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Text(
                                if (isBangla) "ইনস্টল করুন" else "Install",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                UpdateStatus.ERROR -> {
                    Spacer(modifier = Modifier.height(100.dp))
                    Text(
                        text = when (uiState.updateErrorType) {
                            UpdateErrorType.DOWNLOAD_FAILED -> if (isBangla) {
                                "ডাউনলোড ব্যর্থ হয়েছে${uiState.updateErrorReason?.let { ": $it" } ?: ""}"
                            } else {
                                "Download failed${uiState.updateErrorReason?.let { ": $it" } ?: ""}"
                            }
                            else -> if (isBangla) {
                                "আপডেট চেক করা যায়নি। ইন্টারনেট সংযোগ পরীক্ষা করুন।"
                            } else {
                                "Couldn't check for updates. Check your connection."
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                HapticHelper.vibrate(context)
                                viewModel.checkForUpdate(installedCode = installedCode, fromManualCheck = true)
                            },
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Text(
                                if (isBangla) "আবার চেষ্টা করুন" else "Retry",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                UpdateStatus.INSTALLING -> {
                    Spacer(modifier = Modifier.height(120.dp))
                    CircularProgressIndicator(modifier = Modifier.size(40.dp), strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (isBangla) "ইনস্টল হচ্ছে…" else "Installing…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Launches the system package installer for a downloaded APK.
 * URI selection per API level: MediaStore content URI on 29+, FileProvider on ≤ 28.
 */
private fun launchInstaller(
    context: Context,
    update: DownloadedUpdate,
    onUnresolved: () -> Unit
) {
    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Uri.parse(update.uri.toString())
    } else {
        androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", update.file!!)
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
            Toast.LENGTH_SHORT
        ).show()
    }
}
