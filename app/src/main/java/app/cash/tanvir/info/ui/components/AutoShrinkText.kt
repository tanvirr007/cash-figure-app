package app.cash.tanvir.info.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Single-line text that shrinks its font size to fit the available width.
 * Uses an average-glyph-width estimate (digits/commas ≈ 0.62 × font size) so
 * long numbers never fall back to an ellipsis; clamps at [minFontSize].
 */
@Composable
fun AutoShrinkText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = 11.sp,
    textAlign: TextAlign = TextAlign.Center,
    color: Color = Color.Unspecified
) {
    if (text.isEmpty()) return
    val density = LocalDensity.current
    BoxWithConstraints(modifier = modifier) {
        val availableWidthPx = with(density) { maxWidth.toPx() }
        val baseSizePx = with(density) {
            if (style.fontSize.isSpecified) style.fontSize.toPx() else 16.sp.toPx()
        }
        val minSizePx = with(density) { minFontSize.toPx() }
        val estimatedWidthPx = text.length * baseSizePx * 0.62f
        val fittedSizePx = if (availableWidthPx > 0f && estimatedWidthPx > availableWidthPx) {
            (baseSizePx * availableWidthPx / estimatedWidthPx).coerceAtLeast(minSizePx)
        } else {
            baseSizePx
        }
        Text(
            text = text,
            style = style.copy(fontSize = with(density) { fittedSizePx.toSp() }),
            color = color,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            textAlign = textAlign
        )
    }
}
