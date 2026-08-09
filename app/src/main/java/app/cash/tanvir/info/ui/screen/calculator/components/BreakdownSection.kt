package app.cash.tanvir.info.ui.screen.calculator.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.cash.tanvir.info.domain.model.DenominationRow
import app.cash.tanvir.info.util.CurrencyFormatter

/**
 * Collapsible cash breakdown section.
 * Only shows denominations with quantity > 0.
 *
 * Example:
 *   ৳1000 × 12 = ৳12,000
 *   ৳500 × 6 = ৳3,000
 */
@Composable
fun BreakdownSection(
    rows: List<DenominationRow>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isBangla: Boolean = false,
    modifier: Modifier = Modifier
) {
    val activeRows = rows.filter { it.quantity > 0 }

    if (activeRows.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Header — clickable to toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "ক্যাশ ব্রেকডাউন" else "Cash Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isBangla) "${app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(activeRows.size)} টি এন্ট্রি" else "${activeRows.size} items",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(end = 8.dp)
                )
                Icon(
                    imageVector = if (isExpanded)
                        Icons.Default.KeyboardArrowUp
                    else
                        Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) {
                        if (isBangla) "সংকুচিত করুন" else "Collapse"
                    } else {
                        if (isBangla) "সম্প্রসারিত করুন" else "Expand"
                    },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Breakdown list — animated expand/collapse
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    activeRows.forEachIndexed { index, row ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            )
                        }
                        BreakdownItem(row, isBangla = isBangla)
                    }

                    // Grand Total
                    val grandTotal = activeRows.sumOf { it.total }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBangla) "সর্বমোট" else "Grand Total",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "= ${CurrencyFormatter.format(grandTotal, useBengaliDigits = isBangla)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownItem(row: DenominationRow, isBangla: Boolean = false) {
    val denomLabel = if (isBangla) row.denomination.labelBn else row.denomination.label
    val qtyStr = if (isBangla) app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(row.quantity) else row.quantity.toString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$denomLabel × $qtyStr",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "= ${CurrencyFormatter.format(row.total, useBengaliDigits = isBangla)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
