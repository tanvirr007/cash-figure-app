package app.cash.tanvir.info.ui.screen.settingsdetail

import android.content.Context
import android.widget.Toast
import androidx.activity.activityViewModels
import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
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
    val activity = LocalActivity.current
    val viewModel: SettingsViewModel by activity.activityViewModels()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isBangla = uiState.language == AppLanguage.BANGLA

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
                authenticateWithFingerprint(context, isBangla) {
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
                SettingsSection.MISCELLANEOUS -> MiscellaneousContent(
                    isBangla = isBangla,
                    biometricEnabled = uiState.biometricEnabled,
                    screenshotBlockEnabled = uiState.screenshotBlockEnabled,
                    hapticEnabled = uiState.hapticFeedbackEnabled,
                    hapticIntensity = uiState.hapticFeedbackIntensity,
                    onBiometricToggle = onBiometricToggle,
                    onScreenshotToggle = { checked -> viewModel.setScreenshotBlockEnabled(checked) },
                    onHapticToggle = { checked -> viewModel.setHapticFeedbackEnabled(checked) },
                    onIntensityChange = { v -> viewModel.setHapticFeedbackIntensity(v) }
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        onValueChange = { checked -> onToggle(value, checked) }
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
    hapticEnabled: Boolean,
    hapticIntensity: Float,
    onBiometricToggle: (Boolean) -> Unit,
    onScreenshotToggle: (Boolean) -> Unit,
    onHapticToggle: (Boolean) -> Unit,
    onIntensityChange: (Float) -> Unit
) {
    val context = LocalContext.current
    SettingsCard {
        ToggleRow(
            icon = { Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
            title = if (isBangla) "অ্যাপ লক" else "App Lock",
            subtitle = if (isBangla) "অ্যাপ লক চালু করুন" else "Enable app lock",
            checked = biometricEnabled,
            onToggle = onBiometricToggle
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ToggleRow(
            icon = { Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
            title = if (isBangla) "স্ক্রিন সুরক্ষা" else "Screen Protection",
            subtitle = if (isBangla) "স্ক্রিন ক্যাপচার বন্ধ রাখুন" else "Prevent screen capture",
            checked = screenshotBlockEnabled,
            onToggle = onScreenshotToggle
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ToggleRow(
            icon = { Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
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
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
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
