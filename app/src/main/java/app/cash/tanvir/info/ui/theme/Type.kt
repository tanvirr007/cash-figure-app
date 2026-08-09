package app.cash.tanvir.info.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.cash.tanvir.info.R

val TiroBanglaFontFamily = FontFamily(
    Font(R.font.tiro_bangla, FontWeight.Normal),
    Font(R.font.tiro_bangla, FontWeight.Medium),
    Font(R.font.tiro_bangla, FontWeight.SemiBold),
    Font(R.font.tiro_bangla, FontWeight.Bold)
)

private val defaultTypography = Typography()

val Typography = Typography(
    // Display styles
    displayLarge = defaultTypography.displayLarge.copy(
        fontFamily = TiroBanglaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = defaultTypography.displayMedium.copy(
        fontFamily = TiroBanglaFontFamily
    ),
    displaySmall = defaultTypography.displaySmall.copy(
        fontFamily = TiroBanglaFontFamily
    ),

    // Headline styles
    headlineLarge = defaultTypography.headlineLarge.copy(
        fontFamily = TiroBanglaFontFamily
    ),
    headlineMedium = defaultTypography.headlineMedium.copy(
        fontFamily = TiroBanglaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = defaultTypography.headlineSmall.copy(
        fontFamily = TiroBanglaFontFamily
    ),

    // Title styles
    titleLarge = defaultTypography.titleLarge.copy(
        fontFamily = TiroBanglaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = defaultTypography.titleMedium.copy(
        fontFamily = TiroBanglaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = defaultTypography.titleSmall.copy(
        fontFamily = TiroBanglaFontFamily
    ),

    // Body styles
    bodyLarge = defaultTypography.bodyLarge.copy(
        fontFamily = TiroBanglaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = defaultTypography.bodyMedium.copy(
        fontFamily = TiroBanglaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = defaultTypography.bodySmall.copy(
        fontFamily = TiroBanglaFontFamily
    ),

    // Label styles
    labelLarge = defaultTypography.labelLarge.copy(
        fontFamily = TiroBanglaFontFamily
    ),
    labelMedium = defaultTypography.labelMedium.copy(
        fontFamily = TiroBanglaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = defaultTypography.labelSmall.copy(
        fontFamily = TiroBanglaFontFamily
    )
)
