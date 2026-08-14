package app.cash.tanvir.info.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private val ScrollbarWidth = 4.dp
private val MinThumbHeight = 24f
private const val ScrollbarHideDelayMillis = 900L
private const val ScrollbarFadeMillis = 200

/**
 * Lightweight vertical scroll indicator drawn over scrollable content.
 * Exact for plain scroll states, approximated from layout info for lazy lists.
 * Only appears while the content is being scrolled (drag or fling) and fades
 * out shortly after it stops.
 *
 * Callers must pass a positioning-only modifier (e.g. `Modifier.align(...)`).
 * The track width and height are applied internally; size-overriding
 * modifiers such as `matchParentSize()` stretch the canvas and must not
 * be used.
 */
@Composable
fun VerticalScrollbarIndicator(
    state: ScrollState,
    modifier: Modifier = Modifier
) {
    val visible = rememberScrollbarVisibility(state.isScrollInProgress)
    val thumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(ScrollbarFadeMillis),
        label = "scrollbarAlpha"
    )
    Canvas(
        modifier = modifier
            .fillMaxHeight()
            .width(ScrollbarWidth)
            .graphicsLayer { this.alpha = alpha }
    ) {
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
 *
 * Callers must pass a positioning-only modifier (e.g. `Modifier.align(...)`).
 * The track width and height are applied internally; size-overriding
 * modifiers such as `matchParentSize()` stretch the canvas and must not
 * be used.
 */
@Composable
fun VerticalScrollbarIndicator(
    state: LazyListState,
    modifier: Modifier = Modifier
) {
    val visible = rememberScrollbarVisibility(state.isScrollInProgress)
    val thumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(ScrollbarFadeMillis),
        label = "scrollbarAlpha"
    )
    Canvas(
        modifier = modifier
            .fillMaxHeight()
            .width(ScrollbarWidth)
            .graphicsLayer { this.alpha = alpha }
    ) {
        // Only render when the list actually overflows (short lists stay clean)
        if (!state.canScrollForward && !state.canScrollBackward) return@Canvas
        val info = state.layoutInfo
        val totalItems = info.totalItemsCount
        val viewportHeight = info.viewportSize.height.toFloat()
        if (totalItems == 0 || viewportHeight <= 0f) return@Canvas
        val visibleItems = info.visibleItemsInfo
        if (visibleItems.isEmpty()) return@Canvas
        // Average the REAL item sizes across the visible span (first..last visible
        // item offsets) instead of guessing from the viewport height. Variable-height
        // items then shift the average continuously while scrolling, so the thumb
        // neither resizes nor jumps as items enter/leave the viewport.
        val first = visibleItems.first()
        val last = visibleItems.last()
        val spanCount = (last.index - first.index + 1).coerceAtLeast(1)
        val spanHeight = (last.offset + last.size - first.offset).toFloat().coerceAtLeast(1f)
        val avgItemHeight = spanHeight / spanCount
        val contentHeight = totalItems * avgItemHeight + viewportHeight
        val scrollPosition = (first.index * avgItemHeight + first.offset).coerceAtLeast(0f)
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
    // Vertical-track invariant: a canvas wider than tall is a broken size
    // (e.g. a caller modifier that stretched the track); never paint a shape
    // spanning it, since the rounded corners would degenerate into an oval.
    if (size.width <= 0f || size.width > trackHeight) return
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

/**
 * Tracks scroll activity: shows the indicator immediately while scrolling
 * (drag or fling) and hides it after a short idle delay once scrolling stops.
 */
@Composable
private fun rememberScrollbarVisibility(isScrollInProgress: Boolean): Boolean {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(isScrollInProgress) {
        if (isScrollInProgress) {
            visible = true
        } else {
            delay(ScrollbarHideDelayMillis)
            visible = false
        }
    }
    return visible
}
