package app.cash.tanvir.info.ui.screen.about

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.NewReleases
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import app.cash.tanvir.info.R
import app.cash.tanvir.info.util.HapticHelper
import app.cash.tanvir.info.util.getInstalledVersion
import java.util.Calendar

/**
 * Full-page About screen: author profile, app purpose, and contact links.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val (installedName, installedCode) = remember(context) { getInstalledVersion(context) }

    // Android-style hidden easter egg: tap the Version row 7 times quickly.
    // Each reveal picks one random message from the pool.
    val easterEggMessages = remember {
        listOf(
            "You looked. I respect that.",
            "This seemed like a good place to hide something.",
            "You can leave now. Nothing happens here.",
            "Please pretend you never saw this.",
            "You found the secret."
        )
    }
    var versionTapCount by remember { mutableIntStateOf(0) }
    var lastVersionTap by remember { mutableLongStateOf(0L) }
    var easterEggMessage by remember { mutableStateOf<String?>(null) }

    fun openUrl(url: String) {
        HapticHelper.vibrate(context)
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "Couldn't open the link",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun openEmail() {
        HapticHelper.vibrate(context)
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:tanvirhasan2005@proton.me"))
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "No email app found",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Author") },
                navigationIcon = {
                    IconButton(onClick = {
                        HapticHelper.vibrate(context)
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile_avatar),
                    contentDescription = "Tanvir Hasan",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tanvir Hasan",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Google Play Protect may flag \"Cash Figure\" as unverified since it's not on the Play Store. The app is safe and open source — tap \"Install anyway\" if warned.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    AboutLinkRow(
                        iconRes = R.drawable.ic_globe,
                        title = "Website",
                        subtitle = "tanvir.info",
                        onClick = { openUrl("https://tanvir.info") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    AboutLinkRow(
                        iconRes = R.drawable.ic_github,
                        title = "GitHub",
                        subtitle = "tanvirr007",
                        onClick = { openUrl("https://github.com/tanvirr007") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    AboutLinkRow(
                        iconRes = R.drawable.ic_telegram,
                        title = "Telegram",
                        subtitle = "tanvirr007",
                        onClick = { openUrl("https://t.me/tanvirr007") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    AboutLinkRow(
                        iconRes = R.drawable.ic_email,
                        title = "Email",
                        subtitle = "tanvirhasan2005@proton.me",
                        onClick = { openEmail() }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    AboutLinkRow(
                        iconRes = R.drawable.ic_code,
                        title = "Source Code",
                        subtitle = "GitHub",
                        onClick = { openUrl("https://github.com/tanvirr007/cash-figure-app") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    AboutLinkRow(
                        iconRes = R.drawable.ic_code,
                        iconVector = Icons.Rounded.NewReleases,
                        title = "Version",
                        subtitle = "v$installedName (Build $installedCode)",
                        onClick = {
                            HapticHelper.vibrate(context)
                            val now = System.currentTimeMillis()
                            if (now - lastVersionTap > 3000) {
                                versionTapCount = 0
                            }
                            lastVersionTap = now
                            versionTapCount++
                            if (versionTapCount >= 7) {
                                versionTapCount = 0
                                easterEggMessage = easterEggMessages.random()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "© ${Calendar.getInstance().get(Calendar.YEAR)} Tanvir Hasan · MIT License",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Hidden easter egg — 7 quick taps on the Version row, one random message per reveal
    easterEggMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { easterEggMessage = null },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    HapticHelper.vibrate(context)
                    easterEggMessage = null
                }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun AboutLinkRow(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconVector: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}
