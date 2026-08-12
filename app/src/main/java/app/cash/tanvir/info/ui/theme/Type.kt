package app.cash.tanvir.info.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.cash.tanvir.info.R

private val defaultTypography = Typography()

val TiroBanglaFontFamily = FontFamily(
    Font(R.font.tiro_bangla, FontWeight.Normal),
    Font(R.font.tiro_bangla, FontWeight.Medium),
    Font(R.font.tiro_bangla, FontWeight.SemiBold),
    Font(R.font.tiro_bangla, FontWeight.Bold)
)

/**
 * Bangla UI: Tiro Bangla everywhere (Latin + Bengali glyphs).
 */
val BanglaTypography = appTypography(TiroBanglaFontFamily)

/**
 * English UI: the device's system sans-serif (Roboto on most phones,
 * Google Sans on Pixel, OEM fonts elsewhere).
 */
val EnglishTypography = appTypography(FontFamily.Default)

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
