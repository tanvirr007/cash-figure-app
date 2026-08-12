package app.cash.tanvir.info.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.cash.tanvir.info.R
import app.cash.tanvir.info.data.local.preferences.AppFont

private val defaultTypography = Typography()

private val tiroBanglaFonts: List<Font> = listOf(
    Font(R.font.tiro_bangla, FontWeight.Normal),
    Font(R.font.tiro_bangla, FontWeight.Medium),
    Font(R.font.tiro_bangla, FontWeight.SemiBold),
    Font(R.font.tiro_bangla, FontWeight.Bold)
)

val TiroBanglaFontFamily = FontFamily(tiroBanglaFonts)

private fun customFonts(fontResId: Int): List<Font> = listOf(
    Font(fontResId, FontWeight.Normal),
    Font(fontResId, FontWeight.Medium),
    Font(fontResId, FontWeight.SemiBold),
    Font(fontResId, FontWeight.Bold)
)

private val GoogleSansRoundedFonts = customFonts(R.font.google_sans_rounded_regular)
private val GoogleSansFlexRoundedFonts = customFonts(R.font.google_sans_flex_rounded)
private val VolteRoundFonts = customFonts(R.font.volte_round_regular)

private val GoogleSansRoundedFamily = FontFamily(GoogleSansRoundedFonts)
private val GoogleSansFlexRoundedFamily = FontFamily(GoogleSansFlexRoundedFonts)
private val VolteRoundFamily = FontFamily(VolteRoundFonts)

/**
 * Bangla UI with a custom Latin font: the custom font renders Latin glyphs,
 * Bengali glyphs fall back to Tiro Bangla (the custom fonts are Latin-only).
 */
private fun banglaFallbackFamily(latinFonts: List<Font>): FontFamily =
    FontFamily(latinFonts + tiroBanglaFonts)

private val GoogleSansRoundedBanglaFamily = banglaFallbackFamily(GoogleSansRoundedFonts)
private val GoogleSansFlexRoundedBanglaFamily = banglaFallbackFamily(GoogleSansFlexRoundedFonts)
private val VolteRoundBanglaFamily = banglaFallbackFamily(VolteRoundFonts)

/**
 * Resolves the FontFamily for the selected [AppFont] and UI language.
 * Bangla mode always chains the custom font over Tiro Bangla so Bengali
 * glyphs keep their dedicated font.
 */
fun fontFamilyFor(font: AppFont, isBangla: Boolean): FontFamily = when (font) {
    AppFont.DEFAULT -> if (isBangla) TiroBanglaFontFamily else FontFamily.Default
    AppFont.GOOGLE_SANS_ROUNDED -> if (isBangla) GoogleSansRoundedBanglaFamily else GoogleSansRoundedFamily
    AppFont.GOOGLE_SANS_FLEX -> if (isBangla) GoogleSansFlexRoundedBanglaFamily else GoogleSansFlexRoundedFamily
    AppFont.VOLTE_ROUND -> if (isBangla) VolteRoundBanglaFamily else VolteRoundFamily
}

/**
 * Bangla UI: Tiro Bangla everywhere (Latin + Bengali glyphs).
 */
val BanglaTypography = appTypography(TiroBanglaFontFamily)

/**
 * English UI: the device's system sans-serif (Roboto on most phones,
 * Google Sans on Pixel, OEM fonts elsewhere).
 */
val EnglishTypography = appTypography(FontFamily.Default)

/**
 * Typography for the selected app font and language.
 */
fun typographyFor(font: AppFont, isBangla: Boolean): Typography =
    appTypography(fontFamilyFor(font, isBangla))

private fun appTypography(fontFamily: FontFamily): Typography = Typography(
    // Display styles
    displayLarge = defaultTypography.displayLarge.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = defaultTypography.displayMedium.copy(
        fontFamily = fontFamily
    ),
    displaySmall = defaultTypography.displaySmall.copy(
        fontFamily = fontFamily
    ),

    // Headline styles
    headlineLarge = defaultTypography.headlineLarge.copy(
        fontFamily = fontFamily
    ),
    headlineMedium = defaultTypography.headlineMedium.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = defaultTypography.headlineSmall.copy(
        fontFamily = fontFamily
    ),

    // Title styles
    titleLarge = defaultTypography.titleLarge.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = defaultTypography.titleMedium.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = defaultTypography.titleSmall.copy(
        fontFamily = fontFamily
    ),

    // Body styles
    bodyLarge = defaultTypography.bodyLarge.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = defaultTypography.bodyMedium.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = defaultTypography.bodySmall.copy(
        fontFamily = fontFamily
    ),

    // Label styles
    labelLarge = defaultTypography.labelLarge.copy(
        fontFamily = fontFamily
    ),
    labelMedium = defaultTypography.labelMedium.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = defaultTypography.labelSmall.copy(
        fontFamily = fontFamily
    )
)
