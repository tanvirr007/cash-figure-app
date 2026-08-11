package app.cash.tanvir.info.util

/**
 * Compares two version strings semantically (e.g. "v3.4.6" vs "3.4.5").
 * Returns > 0 when [a] is newer, < 0 when [b] is newer, 0 when equal or
 * when either string cannot be parsed as a dotted numeric version.
 */
fun compareVersions(a: String, b: String): Int {
    val partsA = parseVersion(a) ?: return 0
    val partsB = parseVersion(b) ?: return 0
    val maxLen = maxOf(partsA.size, partsB.size)
    for (i in 0 until maxLen) {
        val x = partsA.getOrElse(i) { 0 }
        val y = partsB.getOrElse(i) { 0 }
        if (x != y) return x.compareTo(y)
    }
    return 0
}

private fun parseVersion(text: String): List<Int>? {
    val cleaned = text.trim().removePrefix("v").removePrefix("V").trim()
    if (cleaned.isEmpty()) return null
    val numbers = mutableListOf<Int>()
    for (part in cleaned.split('.')) {
        numbers.add(part.trim().toIntOrNull() ?: return null)
    }
    return numbers
}

/**
 * Decides whether an update should be offered.
 *
 * The visible version name is the source of truth: a newer manifest name
 * always wins, even when the installed version code is higher (version codes
 * are CI run numbers and can regress on workflow re-runs — a code-only check
 * can permanently hide a newer release). Equal names fall back to the code
 * so a genuinely re-released build is still offered.
 *
 * This rule can never offer a downgrade and can never get stuck:
 * - name newer           -> update
 * - name older           -> no update (downgrade guard)
 * - names equal          -> code decides
 * - unparseable names    -> code decides (never worse than the old check)
 */
fun isUpdateAvailable(
    manifestName: String,
    manifestCode: Long,
    installedName: String,
    installedCode: Long
): Boolean {
    val cmp = compareVersions(manifestName, installedName)
    return when {
        cmp > 0 -> true
        cmp < 0 -> false
        else -> manifestCode > installedCode
    }
}
