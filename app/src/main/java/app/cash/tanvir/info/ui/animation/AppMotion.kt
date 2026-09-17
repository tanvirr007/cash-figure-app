package app.cash.tanvir.info.ui.animation

import android.provider.Settings
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
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
import androidx.navigation.NavBackStackEntry

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
    val PressDownSpec: TweenSpec<Float> = tween(DurationFast, easing = FastOutSlowInEasing)
    val PressUpSpec: SpringSpec<Float> = spring(
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
        initialOffsetX = { it },
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

private const val SLIDE_OFFSET_FRACTION = 4 // 1/4 = 25% of width

/**
 * Returns the bottom-navigation tab index (0 = Calculator, 1 = History, 2 = Settings),
 * or -1 if the route is not a bottom-nav tab.
 */
fun tabIndex(route: String?): Int = when {
    route?.startsWith("calculator") == true -> 0
    route == "history" -> 1
    route == "settings" -> 2
    else -> -1
}

/**
 * Directional slide + fade transition for entering a bottom-navigation tab.
 * Tabs slide left or right based on their bottom bar position (higher index = slide from right, lower = from left).
 * Falls back to screenPopEnterTransition when returning from a sub-screen.
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.tabEnterTransition(reducedMotion: Boolean): EnterTransition {
    if (reducedMotion) return EnterTransition.None
    val initialTab = tabIndex(initialState.destination.route)
    val targetTab = tabIndex(targetState.destination.route)
    if (initialTab != -1 && targetTab != -1) {
        val forward = targetTab >= initialTab
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> if (forward) fullWidth / SLIDE_OFFSET_FRACTION else -fullWidth / SLIDE_OFFSET_FRACTION },
            animationSpec = tween(AppMotion.DurationMedium, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(AppMotion.DurationMedium, easing = FastOutSlowInEasing))
    }
    return screenPopEnterTransition(reducedMotion)
}

/**
 * Directional slide + fade transition for exiting a bottom-navigation tab.
 * When switching between tabs, slides out to left if moving forward or to right if moving backward.
 * Falls back to screenExitTransition when opening a sub-screen.
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.tabExitTransition(reducedMotion: Boolean): ExitTransition {
    if (reducedMotion) return ExitTransition.None
    val initialTab = tabIndex(initialState.destination.route)
    val targetTab = tabIndex(targetState.destination.route)
    if (initialTab != -1 && targetTab != -1) {
        val forward = targetTab >= initialTab
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> if (forward) -fullWidth / SLIDE_OFFSET_FRACTION else fullWidth / SLIDE_OFFSET_FRACTION },
            animationSpec = tween(AppMotion.DurationMedium, easing = FastOutSlowInEasing)
        ) + fadeOut(tween(AppMotion.DurationMedium, easing = FastOutSlowInEasing))
    }
    return screenExitTransition(reducedMotion)
}

/**
 * Directional slide + fade transition for re-entering a tab via back/pop.
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.tabPopEnterTransition(reducedMotion: Boolean): EnterTransition {
    if (reducedMotion) return EnterTransition.None
    val initialTab = tabIndex(initialState.destination.route)
    val targetTab = tabIndex(targetState.destination.route)
    if (initialTab != -1 && targetTab != -1) {
        val forward = targetTab >= initialTab
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> if (forward) fullWidth / SLIDE_OFFSET_FRACTION else -fullWidth / SLIDE_OFFSET_FRACTION },
            animationSpec = tween(AppMotion.DurationMedium, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(AppMotion.DurationMedium, easing = FastOutSlowInEasing))
    }
    return screenPopEnterTransition(reducedMotion)
}

/**
 * Directional slide + fade transition for leaving a tab via back/pop.
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.tabPopExitTransition(reducedMotion: Boolean): ExitTransition {
    if (reducedMotion) return ExitTransition.None
    val initialTab = tabIndex(initialState.destination.route)
    val targetTab = tabIndex(targetState.destination.route)
    if (initialTab != -1 && targetTab != -1) {
        val forward = targetTab >= initialTab
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> if (forward) -fullWidth / SLIDE_OFFSET_FRACTION else fullWidth / SLIDE_OFFSET_FRACTION },
            animationSpec = tween(AppMotion.DurationMedium, easing = FastOutSlowInEasing)
        ) + fadeOut(tween(AppMotion.DurationMedium, easing = FastOutSlowInEasing))
    }
    return screenPopExitTransition(reducedMotion)
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
