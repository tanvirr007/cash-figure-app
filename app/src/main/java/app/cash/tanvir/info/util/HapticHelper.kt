package app.cash.tanvir.info.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Helper to trigger haptic feedback vibrations with custom intensity scaling.
 */
object HapticHelper {
    var isEnabled: Boolean = false
    var intensity: Float = 0.5f

    fun vibrate(context: Context) {
        if (!isEnabled) return

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (vibrator == null || !vibrator.hasVibrator()) return

        // Scale intensity 0.0f..1.0f to amplitude 1..255
        val amplitude = (intensity * 254 + 1).toInt().coerceIn(1, 255)
        val durationMs = 30L // Short click duration

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(durationMs, amplitude)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // Fallback for devices where custom amplitude fails or permission is missing
            @Suppress("DEPRECATION")
            try {
                vibrator.vibrate(durationMs)
            } catch (ignored: Exception) {
                // Never crash haptics (e.g. SecurityException when VIBRATE permission denied)
            }
        }
    }
}
