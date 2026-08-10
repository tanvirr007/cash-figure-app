package app.cash.tanvir.info.util

import java.util.Locale

/**
 * Formats byte counts into human-readable sizes (B / KB / MB / GB).
 * Pure function — unit-tested.
 */
object SizeFormatter {

    private const val KB = 1024.0
    private const val MB = KB * 1024
    private const val GB = MB * 1024

    /**
     * @return e.g. "512 B", "1.0 KB", "2.60 MB", "1.00 GB"; empty string for negative input.
     */
    fun format(bytes: Long): String {
        if (bytes < 0) return ""
        return when {
            bytes >= GB -> String.format(Locale.US, "%.2f GB", bytes / GB)
            bytes >= MB -> String.format(Locale.US, "%.2f MB", bytes / MB)
            bytes >= KB -> String.format(Locale.US, "%.1f KB", bytes / KB)
            else -> "$bytes B"
        }
    }
}
