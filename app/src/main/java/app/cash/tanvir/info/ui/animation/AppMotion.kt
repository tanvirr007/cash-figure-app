package app.cash.tanvir.info.ui.animation

import android.provider.Settings
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext

/**
 * Single source of truth for app-wide motion: durations, easings, and the
 * reusable transition/effect specs used across screens. Keeps motion
 * consistent and lets the "remove animations" system setting disable
 * the heavier effects in one place.
 */
object AppMotion {

    const val DurationFast = 150
    const val DurationNormal = 250
    const val DurationMedium = 300
    const val DurationSlow = 450

    /** Elements entering the screen: slow start, fast finish. */
    val EnterEasing = LinearOutSlowInEasing

    /** Elements leaving the screen: fast start, slow finish. */
    val ExitEasing = FastOutSlowInEasing

    /** Quick press-down (ripple-fast), springy release. */
    val PressDownSpec = tween(DurationFast, easing = FastOutSlowInEasing)
    val PressUpSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
}

/**
 * True when the system has animations disabled (Settings "remove animations"
 * → animator duration scale 0). Compute once per screen and pass the value
 * into the transition specs below.
 */
@Composable
fun shouldReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
}

/** Sub-screen push: slide in from the right edge + fade. */
fun screenEnterTransition(reducedMotion: Boolean): EnterTransition {
    if (reducedMotion) return EnterTransition.None
    return slideInHorizontally(
        animationSpec = tween(AppMotion.DurationMedium, easing = AppMotion.EnterEasing)
    ) + fadeIn(tween(AppMotion.DurationMedium, easing = AppMotion.EnterEasing))
}

/** Leaving the current sub-screen (being covered by a push). */
fun screenExitTransition(reducedMotion: Boolean): ExitTransition {
    if (reducedMotion) return ExitTransition.None
    return fadeOut(tween(AppMotion.DurationNormal, easing = AppMotion.ExitEasing))
}

/** Returning to a previous sub-screen via back: fade in only. */
fun screenPopEnterTransition(reducedMotion: Boolean): EnterTransition {
    if (reducedMotion) return EnterTransition.None
    return fadeIn(tween(AppMotion.DurationNormal, easing = AppMotion.EnterEasing))
}

/** Leaving a sub-screen via back: slide out to the right + fade. */
fun screenPopExitTransition(reducedMotion: Boolean): ExitTransition {
    if (reducedMotion) return ExitTransition.None
    return slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(AppMotion.DurationMedium, easing = AppMotion.ExitEasing)
    ) + fadeOut(tween(AppMotion.DurationNormal, easing = AppMotion.ExitEasing))
}

/** Bottom-tab switch: subtle scale + cross-fade (tabs are peers, no slide). */
fun tabEnterTransition(reducedMotion: Boolean): EnterTransition {
    if (reducedMotion) return EnterTransition.None
    return scaleIn(
        initialScale = 0.98f,
        animationSpec = tween(AppMotion.DurationNormal, easing = AppMotion.EnterEasing)
    ) + fadeIn(tween(AppMotion.DurationNormal, easing = AppMotion.EnterEasing))
}

fun tabExitTransition(reducedMotion: Boolean): ExitTransition {
    if (reducedMotion) return ExitTransition.None
    return fadeOut(tween(AppMotion.DurationFast, easing = AppMotion.ExitEasing))
}

fun tabPopEnterTransition(reducedMotion: Boolean): EnterTransition {
    if (reducedMotion) return EnterTransition.None
    return fadeIn(tween(AppMotion.DurationNormal, easing = AppMotion.EnterEasing))
}

fun tabPopExitTransition(reducedMotion: Boolean): ExitTransition {
    if (reducedMotion) return ExitTransition.None
    return scaleOut(
        targetScale = 0.98f,
        animationSpec = tween(AppMotion.DurationFast, easing = AppMotion.ExitEasing)
    ) + fadeOut(tween(AppMotion.DurationFast, easing = AppMotion.ExitEasing))
}

/** Generic in-place content swap: fade + slight scale. */
fun contentEnterTransition(reducedMotion: Boolean): EnterTransition {
    if (reducedMotion) return EnterTransition.None
    return fadeIn(
        animationSpec = tween(AppMotion.DurationNormal, easing = AppMotion.EnterEasing)
    ) + scaleIn(
        initialScale = 0.96f,
        animationSpec = tween(AppMotion.DurationNormal, easing = AppMotion.EnterEasing)
    )
}

fun contentExitTransition(reducedMotion: Boolean): ExitTransition {
    if (reducedMotion) return ExitTransition.None
    return fadeOut(
        animationSpec = tween(AppMotion.DurationFast, easing = AppMotion.ExitEasing)
    ) + scaleOut(
        targetScale = 0.96f,
        animationSpec = tween(AppMotion.DurationFast, easing = AppMotion.ExitEasing)
    )
}

/** Wizard page change: subtle vertical slide + fade. */
fun pageEnterTransition(reducedMotion: Boolean): EnterTransition {
    if (reducedMotion) return EnterTransition.None
    return slideInVertically(
        initialOffsetY = { it / 8 },
        animationSpec = tween(AppMotion.DurationMedium, easing = AppMotion.EnterEasing)
    ) + fadeIn(tween(AppMotion.DurationMedium, easing = AppMotion.EnterEasing))
}

fun pageExitTransition(reducedMotion: Boolean): ExitTransition {
    if (reducedMotion) return ExitTransition.None
    return slideOutVertically(
        targetOffsetY = { -it / 8 },
        animationSpec = tween(AppMotion.DurationMedium, easing = AppMotion.ExitEasing)
    ) + fadeOut(tween(AppMotion.DurationFast, easing = AppMotion.ExitEasing))
}

/**
 * Subtle press micro-interaction: the element shrinks to [scale] while
 * pressed and springs back on release. Pass the SAME [MutableInteractionSource]
 * you hand to the element's clickable/toggleable so ripple and scale share
 * one press signal.
 */
@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    scale: Float = 0.97f,
): Modifier {
    if (shouldReduceMotion()) return this
    val pressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (pressed) scale else 1f,
        animationSpec = if (pressed) AppMotion.PressDownSpec else AppMotion.PressUpSpec,
        label = "pressScale"
    )
    return graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
    }
}
