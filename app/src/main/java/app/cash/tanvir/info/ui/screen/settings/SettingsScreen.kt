package app.cash.tanvir.info.ui.screen.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.HistoryToggleOff
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material.icons.rounded.SystemUpdateAlt
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
    onNavigateToDraft: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
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
            // General group: App Theme / Language / Currency
            SettingsGroupCard(title = if (isBangla) "সাধারণ" else "General") {
                SettingsGroupRow(
                    icon = Icons.Rounded.AutoAwesome,
                    title = if (isBangla) "অ্যাপের থিম" else "App Theme",
                    subtitle = if (isBangla) "থিম বেছে নিয়ে দেখুন" else "Pick a theme and preview it",
                    onClick = {
                        HapticHelper.vibrate(context)
                        onNavigateToSettingsDetail(SettingsSection.THEME)
                    }
                )
                SettingsGroupDivider()
                SettingsGroupRow(
                    icon = Icons.Rounded.Translate,
                    title = if (isBangla) "ভাষা" else "Language",
                    subtitle = if (isBangla) "ভাষা বেছে নিয়ে দেখুন" else "Pick a language and preview it",
                    onClick = {
                        HapticHelper.vibrate(context)
                        onNavigateToSettingsDetail(SettingsSection.LANGUAGE)
                    }
                )
                SettingsGroupDivider()
                SettingsGroupRow(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    title = if (isBangla) "নোট" else "Currency",
                    subtitle = if (isBangla) "হোম পেজে কোন নোটগুলো থাকবে তা নিয়ন্ত্রণ করুন" else "Manage homepage notes",
                    onClick = {
                        HapticHelper.vibrate(context)
                        onNavigateToSettingsDetail(SettingsSection.CURRENCY)
                    }
                )
            }

            // Draft link card: opens the dedicated Draft page (red dot when drafts exist)
            DraftLinkCard(
                isBangla = isBangla,
                hasDrafts = uiState.drafts.isNotEmpty(),
                draftsCount = uiState.drafts.size,
                onClick = {
                    HapticHelper.vibrate(context)
                    onNavigateToDraft()
                }
            )

            // Update group: check for updates / changelog
            SettingsGroupCard(title = if (isBangla) "আপডেট" else "Update") {
                SettingsGroupRow(
                    icon = Icons.Rounded.SystemUpdateAlt,
                    title = if (isBangla) "ওটিএ" else "OTA",
                    subtitle = if (isBangla) "আপডেট চেক করুন" else "Check for updates",
                    onClick = {
                        HapticHelper.vibrate(context)
                        onNavigateToUpdate()
                    }
                )
                SettingsGroupDivider()
                SettingsGroupRow(
                    icon = Icons.Rounded.HistoryToggleOff,
                    title = if (isBangla) "আপডেটের ইতিহাস" else "Changelog",
                    subtitle = if (isBangla) "প্রতিটি সংস্করণের পরিবর্তন দেখুন" else "See what's new in each version",
                    onClick = {
                        HapticHelper.vibrate(context)
                        onNavigateToChangelog()
                    }
                )
            }

            SettingsLinkCard(
                icon = { SettingsIconBadge(Icons.Rounded.SettingsSuggest) },
                title = if (isBangla) "টুলস" else "Miscellaneous",
                subtitle = if (isBangla) "অতিরিক্ত ফিচার ও ডেটা টুল" else "Extra features & data tools",
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
}

@Composable
private fun DraftLinkCard(
    isBangla: Boolean,
    hasDrafts: Boolean,
    draftsCount: Int,
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
            // Red dot badge on the icon corner: signals saved drafts without opening the page
            SettingsIconBadge(
                icon = Icons.Rounded.Bookmarks,
                badge = hasDrafts
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBangla) "ড্রাফট" else "Draft",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when {
                        draftsCount == 0 -> if (isBangla) "কোনো ড্রাফট নেই" else "No drafts"
                        isBangla -> "${BanglaDigitConverter.toBangla(draftsCount.toLong())}টি ড্রাফট"
                        else -> "$draftsCount drafts"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SettingsIconBadge(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary,
    badge: Boolean = false
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
        // Red dot badge on the top-right corner, shown only when something needs attention
        if (badge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 5.dp, y = (-5).dp)
                    .size(10.dp)
                    .background(MaterialTheme.colorScheme.error, RoundedCornerShape(50))
            )
        }
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
    onClick: () -> Unit
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
