package app.cash.tanvir.info.ui.screen.calculator.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.tanvir.info.ui.animation.AppMotion
import app.cash.tanvir.info.ui.animation.contentEnterTransition
import app.cash.tanvir.info.ui.animation.contentExitTransition
import app.cash.tanvir.info.ui.animation.pressScale
import app.cash.tanvir.info.ui.animation.shouldReduceMotion
import app.cash.tanvir.info.ui.components.AutoShrinkText
import app.cash.tanvir.info.util.BanglaDigitConverter
import app.cash.tanvir.info.util.CurrencyFormatter

/**
 * A single denomination row inside the Cash Breakdown card:
 * [Denomination Chip] [Quantity Selector → Picker] [× qty + Subtotal] [Clear Button]
 */
@Composable
fun DenominationRowItem(
    denominationLabel: String,
    quantityText: String,
    rowTotal: Long,
    onOpenPicker: () -> Unit,
    onClear: () -> Unit,
    isBangla: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isCompact = LocalConfiguration.current.screenWidthDp < 360
    // Quantity input comes from the picker sheet only; Bangla mode shows Bangla digits
    val displayQuantity = if (isBangla) BanglaDigitConverter.toBengali(quantityText) else quantityText

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Denomination chip
        Box(
            modifier = Modifier
                .width(if (isCompact) 68.dp else 84.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 4.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = denominationLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
        }

        // Quantity selector — opens the picker sheet on tap. A plain clickable
        // box (not a TextField) so taps are never swallowed by text-field input
        // handling and the value is never clipped by field internals.
        // Filled + state-aware: soft neutral field when empty, teal primary
        // container when it holds a value; colors morph between states.
        val quantityInteractionSource = remember { MutableInteractionSource() }
        val reducedMotion = shouldReduceMotion()
        val hasValue = quantityText.isNotEmpty()
        val boxShape = RoundedCornerShape(16.dp)
        val containerColor by animateColorAsState(
            targetValue = if (hasValue) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
            animationSpec = tween(AppMotion.DurationNormal, easing = AppMotion.EnterEasing),
            label = "qtyBoxContainer"
        )
        val borderColor by animateColorAsState(
            targetValue = if (hasValue) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            },
            animationSpec = tween(AppMotion.DurationNormal, easing = AppMotion.EnterEasing),
            label = "qtyBoxBorder"
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .pressScale(quantityInteractionSource)
                .clip(boxShape)
                .background(containerColor)
                .border(width = 1.dp, color = borderColor, shape = boxShape)
                .clickable(
                    interactionSource = quantityInteractionSource,
                    indication = LocalIndication.current,
                    onClick = { onOpenPicker() }
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = displayQuantity,
                contentAlignment = Alignment.Center,
                transitionSpec = {
                    contentEnterTransition(reducedMotion) togetherWith contentExitTransition(reducedMotion)
                },
                label = "qtyValue"
            ) { current ->
                AutoShrinkText(
                    text = current.ifEmpty { if (isBangla) "০" else "0" },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (current.isEmpty()) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    minFontSize = 12.sp,
                    modifier = Modifier.padding(end = 28.dp)
                )
            }
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = if (isBangla) "সংখ্যা বেছে নিন" else "Pick a quantity",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .size(20.dp),
                tint = if (hasValue) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                }
            )
        }

        // Subtotal
        Column(
            modifier = Modifier.width(if (isCompact) 88.dp else 108.dp),
            horizontalAlignment = Alignment.End
        ) {
            AutoShrinkText(
                text = CurrencyFormatter.format(rowTotal, useBengaliDigits = isBangla),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (rowTotal > 0) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = if (rowTotal > 0)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                textAlign = TextAlign.End,
                minFontSize = 10.sp
            )
        }

        // Clear button
        IconButton(
            onClick = onClear,
            modifier = Modifier.size(32.dp),
            enabled = quantityText.isNotEmpty()
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = if (isBangla) "মুছে ফেলুন" else "Clear",
                modifier = Modifier.size(18.dp),
                tint = if (quantityText.isNotEmpty())
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            )
        }
    }
}
