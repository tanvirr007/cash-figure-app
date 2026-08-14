package app.cash.tanvir.info.util

/**
 * Sanitizes text so colons in Bangla text render as the bisorgo sign "ঃ" (U+0983),
 * the native Bangla punctuation for a colon. A Latin ASCII colon looks misaligned
 * in Bangla typography, so in Bangla mode punctuation colons are converted.
 *
 * [colonToVisarga] converts ":" to "ঃ" only when it punctuates Bangla text —
 * i.e. when the previous non-space character is a Bengali character (U+0980..U+09FF,
 * letters and Bengali digits); any spaces before such a colon are dropped ("মোট : ৫০"
 * → "মোটঃ ৫০"). Times like "রাত ১১:৩০" or "10:45" (a digit before and
 * a digit after the colon) are kept as ASCII colons, as are colons in English text
 * ("Bank: Sonali"). In-word bisorgo (দুঃখ, দুঃখিত, নিঃশব্দ) is never touched.
 * When [isBangla] is false the text is returned unchanged.
 */
object BanglaTextSanitizer {

    private const val BENGALI_VISARGA = '\u0983'

    /**
     * Converts punctuation colons in Bangla text to bisorgo.
     *
     * Example: "মোট: ৫০" → "মোটঃ ৫০", "কথা:" → "কথাঃ"
     * Preserved: "রাত ১১:৩০", "Bank: Sonali", "দুঃখ", "নিঃশব্দ"
     */
    fun colonToVisarga(text: String, isBangla: Boolean): String {
        if (!isBangla || text.isEmpty() || text.indexOf(':') < 0) return text
        val sb = StringBuilder(text.length)
        for (i in text.indices) {
            val ch = text[i]
            if (ch == ':' && isBanglaColonUsage(text, i)) {
                while (sb.isNotEmpty() && sb[sb.length - 1] == ' ') sb.deleteCharAt(sb.length - 1)
                sb.append(BENGALI_VISARGA)
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun isBanglaColonUsage(text: String, colonIndex: Int): Boolean {
        if (isTimeSeparator(text, colonIndex)) return false
        val prev = previousNonSpace(text, colonIndex)
        return prev >= 0 && isBengaliChar(text[prev])
    }

    private fun isTimeSeparator(text: String, colonIndex: Int): Boolean {
        val before = text.getOrNull(colonIndex - 1)
        if (before == null || !isDigit(before)) return false
        var next = colonIndex + 1
        while (next < text.length && text[next] == ' ') next++
        val after = text.getOrNull(next)
        return after != null && isDigit(after)
    }

    private fun previousNonSpace(text: String, index: Int): Int {
        var i = index - 1
        while (i >= 0 && text[i] == ' ') i--
        return i
    }

    private fun isDigit(ch: Char): Boolean = ch in '0'..'9' || ch in '\u09E6'..'\u09EF'

    private fun isBengaliChar(ch: Char): Boolean = ch in '\u0980'..'\u09FF'
}
