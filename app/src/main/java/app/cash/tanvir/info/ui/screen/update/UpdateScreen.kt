package app.cash.tanvir.info.ui.screen.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.domain.model.DownloadedUpdate
import app.cash.tanvir.info.domain.model.UpdateManifest
import app.cash.tanvir.info.ui.animation.contentEnterTransition
import app.cash.tanvir.info.ui.animation.contentExitTransition
import app.cash.tanvir.info.ui.animation.pressScale
import app.cash.tanvir.info.ui.animation.shouldReduceMotion
import app.cash.tanvir.info.ui.components.VerticalScrollbarIndicator
import app.cash.tanvir.info.ui.screen.settings.SettingsViewModel
import app.cash.tanvir.info.ui.screen.settings.UpdateErrorType
import app.cash.tanvir.info.ui.screen.settings.UpdateStatus
import app.cash.tanvir.info.util.BanglaDigitConverter
import app.cash.tanvir.info.util.ChangelogParser
import app.cash.tanvir.info.util.DateTimeFormatter
import app.cash.tanvir.info.util.HapticHelper
import app.cash.tanvir.info.util.SizeFormatter
import app.cash.tanvir.info.util.getInstalledUpdatedAt
import app.cash.tanvir.info.util.getInstalledVersion

/**
 * Full-page Pixel-system-update-style updater. Shares the update state machine
 * with the Settings screen via the activity-scoped [SettingsViewModel].
 * Only the presentation changed — all update/download/install logic is reused.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun UpdateScreen(
    onNavigateBack: () -> Unit
) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: SettingsViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isBangla = uiState.language == AppLanguage.BANGLA
    val reducedMotion = shouldReduceMotion()
    val (installedName, installedCode) = remember(context) { getInstalledVersion(context) }
    val installedUpdatedAt = remember(context) { getInstalledUpdatedAt(context) }

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
            val msg = if (isBangla) "ক্যাশ ফিগার আপডেট করতে সেটিংসে গিয়ে \"এই অ্যাপ থেকে ইনস্টল\" অনুমতি দিন"
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
            viewModel.checkForUpdate(installedName = installedName, installedCode = installedCode, fromManualCheck = true)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME &&
                viewModel.uiState.value.updateStatus == UpdateStatus.INSTALLING
            ) {
                viewModel.onReturnedFromInstaller(getInstalledVersion(context).second)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {},
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
                ),
                windowInsets = TopAppBarDefaults.windowInsets.exclude(WindowInsets(top = 8.dp))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .widthIn(max = 600.dp)
                ) {
                    AnimatedContent(
                        targetState = uiState.updateStatus,
                        transitionSpec = {
                            contentEnterTransition(reducedMotion) togetherWith contentExitTransition(reducedMotion)
                        },
                        label = "updateStatus"
                    ) { status ->
                        when (status) {
                            UpdateStatus.UP_TO_DATE -> {
                            UpToDateContent(
                                isBangla = isBangla,
                                installedName = installedName,
                                installedCode = installedCode,
                                installedUpdatedAt = installedUpdatedAt,
                                lastSuccessfulCheck = uiState.lastSuccessfulCheck,
                                onCheckAgain = {
                                    HapticHelper.vibrate(context)
                                    viewModel.checkForUpdate(installedName = installedName, installedCode = installedCode, fromManualCheck = true)
                                }
                            )
                        }

                        UpdateStatus.DOWNLOADING -> {
                            DownloadingContent(
                                isBangla = isBangla,
                                downloadedBytes = uiState.downloadedBytes,
                                totalBytes = uiState.totalBytes,
                                progress = uiState.downloadProgress,
                                onCancel = {
                                    HapticHelper.vibrate(context)
                                    viewModel.cancelDownload()
                                    onNavigateBack()
                                }
                            )
                        }

                        UpdateStatus.DOWNLOAD_READY -> {
                            DownloadReadyContent(
                                isBangla = isBangla,
                                versionName = uiState.updateManifest?.versionName.orEmpty(),
                                onInstall = {
                                    HapticHelper.vibrate(context)
                                    uiState.downloadedUpdate?.let { requestInstall(it) }
                                }
                            )
                        }

                        else -> {
                            val scrollState = rememberScrollState()
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollState)
                                ) {
                                    when (uiState.updateStatus) {
                                        UpdateStatus.IDLE, UpdateStatus.CHECKING -> {
                                            CheckingContent(isBangla = isBangla)
                                        }

                                        UpdateStatus.UPDATE_AVAILABLE -> {
                                            uiState.updateManifest?.let { manifest ->
                                                UpdateAvailableContent(
                                                    isBangla = isBangla,
                                                    manifest = manifest,
                                                    onDownload = {
                                                        HapticHelper.vibrate(context)
                                                        viewModel.downloadUpdate()
                                                    }
                                                )
                                            }
                                        }

                                        UpdateStatus.INSTALLING -> {
                                            InstallingContent(isBangla = isBangla)
                                        }

                                        UpdateStatus.ERROR -> {
                                            ErrorContent(
                                                isBangla = isBangla,
                                                errorType = uiState.updateErrorType,
                                                errorReason = uiState.updateErrorReason,
                                                onRetry = {
                                                    HapticHelper.vibrate(context)
                                                    viewModel.checkForUpdate(installedName = installedName, installedCode = installedCode, fromManualCheck = true)
                                                }
                                            )
                                        }

                                    else -> Unit
                                }
                                }
                                VerticalScrollbarIndicator(
                                    state = scrollState,
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )
                            }
                        }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateIcon(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Outlined.SystemUpdate,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CheckIcon(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CheckingContent(isBangla: Boolean) {
    Spacer(modifier = Modifier.height(120.dp))
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (isBangla) "আপডেট চেক হচ্ছে…" else "Checking for updates…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UpToDateContent(
    isBangla: Boolean,
    installedName: String,
    installedCode: Long,
    installedUpdatedAt: Long,
    lastSuccessfulCheck: Long?,
    onCheckAgain: () -> Unit
) {
    val scrollState = rememberScrollState()
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            CheckIcon()
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                if (isBangla) "আপনার অ্যাপটি এখন সর্বশেষ সংস্করণে আছে" else "You're already up to date",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isBangla) {
                    "আপনার ডিভাইসে অ্যাপ v${BanglaDigitConverter.toBangla(installedName)} (বিল্ড ${BanglaDigitConverter.toBangla(installedCode)}) ইনস্টল করা আছে"
                } else {
                    "App v$installedName (Build $installedCode) is installed on this device"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (installedUpdatedAt > 0L) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isBangla) {
                        "আপডেট হয়েছে: ${DateTimeFormatter.formatUpdatedOn(installedUpdatedAt, isBangla = true)}"
                    } else {
                        "Updated on: ${DateTimeFormatter.formatUpdatedOn(installedUpdatedAt, isBangla = false)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            lastSuccessfulCheck?.let { timestamp ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isBangla) {
                        "সর্বশেষ সফল আপডেট চেক: ${DateTimeFormatter.formatTime(timestamp, isBangla = true)}"
                    } else {
                        "Last successful check for update: ${DateTimeFormatter.formatTime(timestamp, isBangla = false)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        val checkInteractionSource = remember { MutableInteractionSource() }
        Button(
            onClick = onCheckAgain,
            interactionSource = checkInteractionSource,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp)
                .pressScale(checkInteractionSource)
        ) {
            Text(
                if (isBangla) "আপডেট চেক করুন" else "Check for update",
                fontWeight = FontWeight.SemiBold
            )
        }
        VerticalScrollbarIndicator(
            state = scrollState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun UpdateAvailableContent(
    isBangla: Boolean,
    manifest: UpdateManifest,
    onDownload: () -> Unit
) {
    Spacer(modifier = Modifier.height(40.dp))
    UpdateIcon()
    Spacer(modifier = Modifier.height(28.dp))
    Text(
        if (isBangla) "আপডেট উপলব্ধ" else "Update Available",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(16.dp))

    val versionText = manifest.versionName.removePrefix("v")
    val versionDisplay = if (isBangla) BanglaDigitConverter.toBangla(versionText) else versionText
    val sizeText = manifest.fileSize?.let { SizeFormatter.format(it) }
    Text(
        text = buildString {
            append(if (isBangla) "ক্যাশ ফিগার" else "Cash Figure")
            if (sizeText != null) {
                append("\n")
                append(if (isBangla) "আকার: ${BanglaDigitConverter.toBangla(sizeText)}" else "Size: $sizeText")
            }
            append("\n")
            append(if (isBangla) "উপলব্ধ সংস্করণ: $versionDisplay" else "Available version: $versionDisplay")
            append("\n\n")
            append(
                if (isBangla) {
                    "নতুন ফিচার ও বাগ ফিক্সের জন্য এখনই আপডেট করুন"
                } else {
                    "Update now for the latest features and bug fixes"
                }
            )
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (manifest.changelog.isNotBlank()) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            if (isBangla) "নতুন কী আছে" else "What's new?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        ChangelogText(changelog = manifest.changelog)
    }

    Spacer(modifier = Modifier.height(40.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val downloadInteractionSource = remember { MutableInteractionSource() }
        Button(
            onClick = onDownload,
            interactionSource = downloadInteractionSource,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            modifier = Modifier.pressScale(downloadInteractionSource)
        ) {
            Text(
                if (isBangla) "ডাউনলোড" else "Download",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun ChangelogText(changelog: String) {
    Column {
        var pendingSubItem: String? = null
        changelog.split("\n").forEach { rawLine ->
            val isIndented = rawLine.startsWith(" ") || rawLine.startsWith("\t")
            val trimmed = rawLine.trim()
            when {
                trimmed.startsWith("*") -> {
                    pendingSubItem?.let { renderSubItem(it) }
                    pendingSubItem = null
                    val clean = ChangelogParser.stripCommitHash(
                        trimmed.removePrefix("*").trim()
                    ).removePrefix("**").removeSuffix("**").trim()
                    if (clean.isNotEmpty()) {
                        Text(
                            text = "• $clean",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                isIndented && trimmed.startsWith("-") -> {
                    pendingSubItem?.let { renderSubItem(it) }
                    pendingSubItem = trimmed.removePrefix("-").trim()
                }
                trimmed.isNotEmpty() -> {
                    if (pendingSubItem != null) {
                        pendingSubItem = "$pendingSubItem $trimmed"
                    } else {
                        Text(
                            text = "• $trimmed",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
        pendingSubItem?.let { renderSubItem(it) }
    }
}

@Composable
private fun renderSubItem(text: String) {
    if (text.isNotEmpty()) {
        Text(
            text = "◦ $text",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}

@Composable
private fun DownloadingContent(
    isBangla: Boolean,
    downloadedBytes: Long,
    totalBytes: Long,
    progress: Float,
    onCancel: () -> Unit
) {
    val scrollState = rememberScrollState()
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                if (isBangla) "ডাউনলোড হচ্ছে…" else "Downloading…",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (downloadedBytes > 0) {
                    val total = if (totalBytes > 0) {
                        " / ${SizeFormatter.format(totalBytes)}"
                    } else {
                        ""
                    }
                    "${SizeFormatter.format(downloadedBytes)}$total"
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
                    .height(4.dp),
                progress = { progress.coerceIn(0f, 1f) }
            )
        }
        val cancelInteractionSource = remember { MutableInteractionSource() }
        Button(
            onClick = onCancel,
            interactionSource = cancelInteractionSource,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp)
                .pressScale(cancelInteractionSource)
        ) {
            Text(
                if (isBangla) "বাতিল" else "Cancel",
                fontWeight = FontWeight.SemiBold
            )
        }
        VerticalScrollbarIndicator(
            state = scrollState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun DownloadReadyContent(
    isBangla: Boolean,
    versionName: String,
    onInstall: () -> Unit
) {
    val scrollState = rememberScrollState()
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            CheckIcon()
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                if (isBangla) "ডাউনলোড সম্পন্ন" else "Download complete",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            val versionDisplay = if (isBangla) {
                BanglaDigitConverter.toBangla(versionName.removePrefix("v"))
            } else {
                versionName.removePrefix("v")
            }
            Text(
                if (isBangla) "ক্যাশ ফিগার v$versionDisplay ইনস্টল করার জন্য প্রস্তুত" else "Cash Figure v$versionDisplay is ready to install",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        val installInteractionSource = remember { MutableInteractionSource() }
        Button(
            onClick = onInstall,
            interactionSource = installInteractionSource,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp)
                .pressScale(installInteractionSource)
        ) {
            Text(
                if (isBangla) "ইনস্টল করুন" else "Install",
                fontWeight = FontWeight.SemiBold
            )
        }
        VerticalScrollbarIndicator(
            state = scrollState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun InstallingContent(isBangla: Boolean) {
    Spacer(modifier = Modifier.height(120.dp))
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (isBangla) "ইনস্টল হচ্ছে…" else "Installing…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorContent(
    isBangla: Boolean,
    errorType: UpdateErrorType?,
    errorReason: String?,
    onRetry: () -> Unit
) {
    Spacer(modifier = Modifier.height(40.dp))
    val isDownloadFailed = errorType == UpdateErrorType.DOWNLOAD_FAILED
    Text(
        text = when {
            isDownloadFailed -> if (isBangla) "ডাউনলোড ব্যর্থ হয়েছে" else "Download failed"
            else -> if (isBangla) "আপডেট চেক করা যায়নি" else "Couldn't check for updates"
        },
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = if (isDownloadFailed) {
            errorReason?.let {
                if (isBangla) "ডাউনলোড ব্যর্থ হয়েছে: $it" else "Download failed: $it"
            } ?: if (isBangla) "আবার চেষ্টা করুন।" else "Please try again."
        } else {
            if (isBangla) "ইন্টারনেট সংযোগ পরীক্ষা করুন এবং আবার চেষ্টা করুন।" else "Check your internet connection and try again."
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error
    )
    Spacer(modifier = Modifier.height(28.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val retryInteractionSource = remember { MutableInteractionSource() }
        Button(
            onClick = onRetry,
            interactionSource = retryInteractionSource,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            modifier = Modifier.pressScale(retryInteractionSource)
        ) {
            Text(
                if (isBangla) "আবার চেষ্টা করুন" else "Retry",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
}

/**
 * Launches the system package installer for a downloaded APK.
 * The APK lives in app-private storage; its FileProvider content URI
 * (built by the repository) is granted to the installer directly.
 */
private fun launchInstaller(
    context: Context,
    update: DownloadedUpdate,
    onUnresolved: () -> Unit
) {
    val uri = Uri.parse(update.uri.toString())
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
            if (isBangla) "ডাউনলোড লিংকটি খোলা যায়নি" else "Couldn't open the download link",
            Toast.LENGTH_SHORT
        ).show()
    }
}
