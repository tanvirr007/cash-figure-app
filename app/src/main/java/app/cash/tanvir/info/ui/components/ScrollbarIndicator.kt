package app.cash.tanvir.info.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

private val ScrollbarWidth = 4.dp
private val MinThumbHeight = 24f

/**
 * Lightweight vertical scroll indicator drawn over scrollable content.
 * Exact for plain scroll states, approximated from layout info for lazy lists.
 */
@Composable
fun VerticalScrollbarIndicator(
    state: ScrollState,
    modifier: Modifier = Modifier
) {
    val thumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
    Canvas(modifier = modifier.fillMaxHeight().width(ScrollbarWidth)) {
        // Only render when the content actually overflows (fits on big screens)
        if (state.maxValue <= 0) return@Canvas
        val trackHeight = size.height
        val contentHeight = trackHeight + state.maxValue
        drawScrollIndicator(
            thumbColor = thumbColor,
            trackColor = trackColor,
            contentHeight = contentHeight,
            scrollPosition = state.value.toFloat(),
            maxScroll = state.maxValue.toFloat()
        )
    }
}

/**
 * Lightweight vertical scroll indicator for lazy lists, using layout info
 * to approximate the visible thumb position and size.
 */
@Composable
fun VerticalScrollbarIndicator(
    state: LazyListState,
    modifier: Modifier = Modifier
) {
    val thumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
    Canvas(modifier = modifier.fillMaxHeight().width(ScrollbarWidth)) {
        // Only render when the list actually overflows (short lists stay clean)
        if (!state.canScrollForward && !state.canScrollBackward) return@Canvas
        val info = state.layoutInfo
        val totalItems = info.totalItemsCount
        val viewportHeight = info.viewportSize.height.toFloat()
        if (totalItems == 0 || viewportHeight <= 0f) return@Canvas
        val visible = info.visibleItemsInfo
        val avgItemHeight = viewportHeight / visible.size.coerceAtLeast(1)
        val first = visible.firstOrNull()
        val contentHeight = totalItems * avgItemHeight + viewportHeight
        val scrollPosition = (first?.index ?: 0) * avgItemHeight + (first?.offset ?: 0)
        drawScrollIndicator(
            thumbColor = thumbColor,
            trackColor = trackColor,
            contentHeight = contentHeight,
            scrollPosition = scrollPosition,
            maxScroll = contentHeight - viewportHeight
        )
    }
}

private fun DrawScope.drawScrollIndicator(
    thumbColor: Color,
    trackColor: Color,
    contentHeight: Float,
    scrollPosition: Float,
    maxScroll: Float
) {
    val trackHeight = size.height
    if (trackHeight <= 0f) return
    val radius = CornerRadius(size.width / 2f)
    drawRoundRect(color = trackColor, size = Size(size.width, trackHeight), cornerRadius = radius)
    if (contentHeight <= trackHeight) return
    val thumbHeight = (trackHeight * trackHeight / contentHeight).coerceIn(MinThumbHeight, trackHeight)
    val maxOffset = trackHeight - thumbHeight
    val progress = if (maxScroll > 0f) (scrollPosition / maxScroll).coerceIn(0f, 1f) else 0f
    drawRoundRect(
        color = thumbColor,
        topLeft = Offset(0f, maxOffset * progress),
        size = Size(size.width, thumbHeight),
        cornerRadius = radius
    )
}
