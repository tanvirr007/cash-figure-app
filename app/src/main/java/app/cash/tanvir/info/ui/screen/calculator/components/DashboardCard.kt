package app.cash.tanvir.info.ui.screen.calculator.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.tanvir.info.ui.animation.AppMotion
import app.cash.tanvir.info.ui.components.AutoShrinkText
import app.cash.tanvir.info.util.CurrencyFormatter
import app.cash.tanvir.info.util.HapticHelper

/**
 * Dashboard card showing the grand total, amount in words,
 * total pieces, and active denominations count.
 * Optional corner Clear All action.
 */
@Composable
fun DashboardCard(
    grandTotal: Long,
    amountInWords: String,
    totalPieces: Long,
    activeDenominations: Int,
    isBangla: Boolean = false,
    onClearAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Count-up animation: 0 -> grandTotal whenever the total changes
    val countUpProgress = remember { Animatable(1f) }
    LaunchedEffect(grandTotal) {
        countUpProgress.snapTo(0f)
        countUpProgress.animateTo(1f, tween(durationMillis = 600, easing = AppMotion.EnterEasing))
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
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Grand Total — largest, boldest element
                AutoShrinkText(
                    text = grandTotalFormatted,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    minFontSize = 18.sp
                )

                // Amount in words + stats row — fade/expand in when pieces appear
                AnimatedVisibility(
                    visible = totalPieces > 0,
                    enter = fadeIn(tween(AppMotion.DurationMedium)) +
                        expandVertically(animationSpec = tween(AppMotion.DurationMedium)),
                    exit = shrinkVertically(animationSpec = tween(AppMotion.DurationMedium)) +
                        fadeOut(tween(AppMotion.DurationMedium)),
                    label = "dashboardStats"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = amountInWords,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                value = if (isBangla) app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(totalPieces) else totalPieces.toString(),
                                label = when {
                                    isBangla -> "টি নোট"
                                    totalPieces == 1L -> "piece"
                                    else -> "pieces"
                                }
                            )
                            StatItem(
                                value = if (isBangla) app.cash.tanvir.info.util.BanglaDigitConverter.toBangla(activeDenominations) else activeDenominations.toString(),
                                label = if (isBangla) "ধরণের নোট" else "denominations"
                            )
                        }
                    }
                }
            }

            // Corner Clear All action
            if (onClearAll != null) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = if (isBangla) "সব মুছুন" else "Clear All",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            HapticHelper.vibrate(context)
                            onClearAll()
                        }
                        .padding(8.dp),
                    tint = if (totalPieces > 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.25f)
                    }
                )
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
