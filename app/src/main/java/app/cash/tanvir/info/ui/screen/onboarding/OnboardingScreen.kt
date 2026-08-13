package app.cash.tanvir.info.ui.screen.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.cash.tanvir.info.data.local.preferences.AppFont
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.data.local.preferences.AppTheme
import app.cash.tanvir.info.ui.animation.pageEnterTransition
import app.cash.tanvir.info.ui.animation.pageExitTransition
import app.cash.tanvir.info.ui.animation.pressScale
import app.cash.tanvir.info.ui.animation.shouldReduceMotion
import app.cash.tanvir.info.ui.components.VerticalScrollbarIndicator
import app.cash.tanvir.info.ui.theme.fontFamilyFor
import app.cash.tanvir.info.util.BanglaDigitConverter
import app.cash.tanvir.info.util.HapticHelper

/**
 * First-launch wizard: Language (English recommended) → Font → Theme → Done.
 * Selections apply live so the wizard itself previews each choice.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isBangla = uiState.language == AppLanguage.BANGLA
    val reducedMotion = shouldReduceMotion()

    BackHandler(enabled = uiState.pageIndex > 0) { viewModel.back() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "ক্যাশ ফিগার" else "Cash Figure",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (isBangla) {
                        "${BanglaDigitConverter.toBangla(uiState.pageIndex + 1)} / ${BanglaDigitConverter.toBangla(OnboardingViewModel.MAX_PAGE + 1)}"
                    } else {
                        "${uiState.pageIndex + 1} of ${OnboardingViewModel.MAX_PAGE + 1}"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (uiState.pageIndex + 1) / (OnboardingViewModel.MAX_PAGE + 1).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    AnimatedContent(
                        targetState = uiState.pageIndex,
                        transitionSpec = {
                            pageEnterTransition(reducedMotion) togetherWith pageExitTransition(reducedMotion)
                        },
                        label = "onboardingPage"
                    ) { pageIndex ->
                        when (pageIndex) {
                            0 -> LanguagePage(
                                isBangla = isBangla,
                                selected = uiState.language,
                                onSelect = { lang ->
                                    HapticHelper.vibrate(context)
                                    viewModel.selectLanguage(lang)
                                }
                            )
                            1 -> FontPage(
                                isBangla = isBangla,
                                selected = uiState.font,
                                onSelect = { font ->
                                    HapticHelper.vibrate(context)
                                    viewModel.selectFont(font)
                                }
                            )
                            2 -> ThemePage(
                                isBangla = isBangla,
                                selected = uiState.theme,
                                onSelect = { theme ->
                                    HapticHelper.vibrate(context)
                                    viewModel.selectTheme(theme)
                                }
                            )
                            else -> DonePage(isBangla = isBangla)
                        }
                    }
                }
                VerticalScrollbarIndicator(
                    state = scrollState,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                if (uiState.pageIndex > 0) {
                    val backInteractionSource = remember { MutableInteractionSource() }
                    OutlinedButton(
                        onClick = {
                            HapticHelper.vibrate(context)
                            viewModel.back()
                        },
                        interactionSource = backInteractionSource,
                        modifier = Modifier.pressScale(backInteractionSource),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            if (isBangla) "পেছনে" else "Back",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                if (uiState.pageIndex < OnboardingViewModel.MAX_PAGE) {
                    val nextInteractionSource = remember { MutableInteractionSource() }
                    Button(
                        onClick = {
                            HapticHelper.vibrate(context)
                            viewModel.next()
                        },
                        interactionSource = nextInteractionSource,
                        modifier = Modifier.pressScale(nextInteractionSource),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            if (isBangla) "পরবর্তী" else "Next",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    val doneInteractionSource = remember { MutableInteractionSource() }
                    Button(
                        onClick = {
                            HapticHelper.vibrate(context)
                            viewModel.complete(onDone)
                        },
                        interactionSource = doneInteractionSource,
                        modifier = Modifier.pressScale(doneInteractionSource),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            if (isBangla) "শুরু করুন" else "Done",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PageHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LanguagePage(
    isBangla: Boolean,
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit
) {
    PageHeader(
        title = if (isBangla) "আপনার ভাষা বেছে নিন" else "Choose your language",
        subtitle = if (isBangla) "যেকোনো সময় সেটিংস থেকে পরিবর্তন করতে পারবেন" else "You can change this anytime in Settings"
    )
    Spacer(modifier = Modifier.height(20.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OptionCard(
            title = "English",
            subtitle = if (isBangla) "পুরো অ্যাপে ইংরেজি ব্যবহার করুন" else "Use English throughout the app",
            badge = if (isBangla) "সুপারিশকৃত" else "Recommended",
            selected = selected == AppLanguage.ENGLISH,
            onClick = { onSelect(AppLanguage.ENGLISH) }
        )
        OptionCard(
            title = "বাংলা",
            subtitle = if (isBangla) "পুরো অ্যাপে বাংলা ব্যবহার করুন" else "Use Bangla throughout the app",
            badge = null,
            selected = selected == AppLanguage.BANGLA,
            onClick = { onSelect(AppLanguage.BANGLA) }
        )
    }
}

@Composable
private fun FontPage(
    isBangla: Boolean,
    selected: AppFont,
    onSelect: (AppFont) -> Unit
) {
    PageHeader(
        title = if (isBangla) "ফন্ট বেছে নিন" else "Choose a font",
        subtitle = if (isBangla) "আপনার পছন্দের চেহারা বেছে নিন" else "Pick the look you like best"
    )
    Spacer(modifier = Modifier.height(20.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppFont.entries.forEach { font ->
            OptionCard(
                title = when (font) {
                    AppFont.DEFAULT -> if (isBangla) "ডিফল্ট" else "Default"
                    AppFont.GOOGLE_SANS_ROUNDED -> "Google Sans Rounded"
                    AppFont.GOOGLE_SANS_FLEX -> "Google Sans Flex"
                    AppFont.VOLTE_ROUND -> "Volte Round"
                },
                subtitle = when (font) {
                    AppFont.DEFAULT -> if (isBangla) "ডিভাইসের সিস্টেম ফন্ট ব্যবহার করুন" else "Use your device's system font"
                    AppFont.GOOGLE_SANS_ROUNDED -> if (isBangla) "গোলাকার ও আধুনিক চেহারা" else "Rounded, modern look"
                    AppFont.GOOGLE_SANS_FLEX -> if (isBangla) "নমনীয় ও ভার্সেটাইল স্টাইল" else "Flexible, versatile style"
                    AppFont.VOLTE_ROUND -> if (isBangla) "পরিষ্কার ও গোলাকার ডিজাইন" else "Clean, rounded design"
                },
                badge = null,
                selected = selected == font,
                fontPreview = fontFamilyFor(font, isBangla),
                onClick = { onSelect(font) }
            )
        }
    }
}

@Composable
private fun ThemePage(
    isBangla: Boolean,
    selected: AppTheme,
    onSelect: (AppTheme) -> Unit
) {
    PageHeader(
        title = if (isBangla) "থিম বেছে নিন" else "Choose a theme",
        subtitle = if (isBangla) "যেকোনো সময় সেটিংস থেকে পরিবর্তন করতে পারবেন" else "You can change this anytime in Settings"
    )
    Spacer(modifier = Modifier.height(20.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OptionCard(
            title = if (isBangla) "সিস্টেম থিম" else "Follow System",
            subtitle = if (isBangla) "ডিভাইসের থিম অনুসরণ করুন" else "Match your device's theme",
            badge = null,
            selected = selected == AppTheme.SYSTEM,
            onClick = { onSelect(AppTheme.SYSTEM) }
        )
        OptionCard(
            title = if (isBangla) "লাইট থিম" else "Light Theme",
            subtitle = if (isBangla) "সবসময় লাইট থিম ব্যবহার করুন" else "Always use the light theme",
            badge = null,
            selected = selected == AppTheme.LIGHT,
            onClick = { onSelect(AppTheme.LIGHT) }
        )
        OptionCard(
            title = if (isBangla) "ডার্ক থিম" else "Dark Theme",
            subtitle = if (isBangla) "সবসময় ডার্ক থিম ব্যবহার করুন" else "Always use the dark theme",
            badge = null,
            selected = selected == AppTheme.DARK,
            onClick = { onSelect(AppTheme.DARK) }
        )
    }
}

@Composable
private fun DonePage(isBangla: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "৳",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = if (isBangla) "সব প্রস্তুত!" else "You're all set!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Selectable option card with a check indicator, optional "recommended" badge,
 * and an optional live font preview for the text.
 */
@Composable
private fun OptionCard(
    title: String,
    subtitle: String,
    badge: String?,
    selected: Boolean,
    onClick: () -> Unit,
    fontPreview: FontFamily? = null
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
            .background(containerColor, RoundedCornerShape(16.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = fontPreview
                )
                if (badge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
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
