package app.cash.tanvir.info.ui.screen.calculator.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.cash.tanvir.info.util.BanglaDigitConverter
import app.cash.tanvir.info.util.CurrencyFormatter

/**
 * A single denomination row inside the Cash Breakdown card:
 * [Denomination Chip] [Read-only Quantity Field → Picker] [× qty + Subtotal] [Clear Button]
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

        // Quantity field — read-only, opens the picker sheet on tap
        OutlinedTextField(
            value = displayQuantity,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .weight(1f)
                .clickable { onOpenPicker() },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            ),
            placeholder = {
                Text(
                    text = if (isBangla) "০" else "0",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isBangla) "সংখ্যা বেছে নিন" else "Pick a quantity",
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Subtotal with explicit × quantity relation
        Column(
            modifier = Modifier.width(if (isCompact) 88.dp else 108.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = CurrencyFormatter.format(rowTotal, useBengaliDigits = isBangla),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (rowTotal > 0) FontWeight.SemiBold else FontWeight.Normal,
                color = if (rowTotal > 0)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (quantityText.isNotEmpty()) {
                Text(
                    text = if (isBangla) "× ${BanglaDigitConverter.toBengali(quantityText)}" else "× $quantityText",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    textAlign = TextAlign.End
                )
            }
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
