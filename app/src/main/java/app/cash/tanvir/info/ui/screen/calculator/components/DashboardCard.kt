package app.cash.tanvir.info.ui.screen.calculator.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.tanvir.info.util.CurrencyFormatter

/**
 * Dashboard card showing the grand total, amount in words,
 * total pieces, and active denominations count.
 */
@Composable
fun DashboardCard(
    grandTotal: Long,
    amountInWords: String,
    totalPieces: Long,
    activeDenominations: Int,
    isBangla: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Count-up animation: 0 -> grandTotal over 600ms whenever the total changes
    val countUpProgress = remember { Animatable(1f) }
    LaunchedEffect(grandTotal) {
        countUpProgress.snapTo(0f)
        countUpProgress.animateTo(1f, tween(durationMillis = 600))
    }
    val animatedTotal = (grandTotal.toDouble() * countUpProgress.value).toLong()
    val grandTotalFormatted = CurrencyFormatter.format(animatedTotal, useBengaliDigits = isBangla)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Grand Total — largest, boldest element
            Text(
                text = grandTotalFormatted,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            // Amount in words — only show when there's a non-zero amount
            if (totalPieces > 0) {
                Text(
                    text = amountInWords,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Stats row: pieces + denominations
            if (totalPieces > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        value = if (isBangla) app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(totalPieces) else totalPieces.toString(),
                        label = if (isBangla) "টি নোট" else "pieces"
                    )
                    StatItem(
                        value = if (isBangla) app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(activeDenominations) else activeDenominations.toString(),
                        label = if (isBangla) "টি নোটের ধরণ" else "denominations"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
        )
    }
}
