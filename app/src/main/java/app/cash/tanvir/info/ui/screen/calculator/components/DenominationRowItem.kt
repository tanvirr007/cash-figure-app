package app.cash.tanvir.info.ui.screen.calculator.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.cash.tanvir.info.util.CurrencyFormatter

/**
 * A single denomination input row:
 * [Denomination Label] [Quantity Input] [Row Total] [Clear Button]
 */
@Composable
fun DenominationRowItem(
    denominationValue: Int,
    denominationLabel: String,
    quantityText: String,
    rowTotal: Long,
    isLastRow: Boolean,
    onQuantityChange: (String) -> Unit,
    onClear: () -> Unit,
    isBangla: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val isCompact = LocalConfiguration.current.screenWidthDp < 360

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (isCompact) 8.dp else 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Denomination label
        Text(
            text = denominationLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(if (isCompact) 64.dp else 80.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )

        // Quantity input
        OutlinedTextField(
            value = quantityText,
            onValueChange = { newValue ->
                // Only allow digits, no negatives, no decimals
                val filtered = newValue.filter { it.isDigit() }
                // Cap at a reasonable length (prevent overflow)
                if (filtered.length <= 10) {
                    onQuantityChange(filtered)
                }
            },
            modifier = Modifier.weight(1f),
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = if (isLastRow) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onDone = { focusManager.clearFocus() }
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        // Row total
        Text(
            text = CurrencyFormatter.format(rowTotal, useBengaliDigits = isBangla),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (rowTotal > 0) FontWeight.SemiBold else FontWeight.Normal,
            color = if (rowTotal > 0)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            modifier = Modifier.width(if (isCompact) 96.dp else 115.dp),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

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
