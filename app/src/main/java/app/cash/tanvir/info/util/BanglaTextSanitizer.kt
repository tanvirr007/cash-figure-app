package app.cash.tanvir.info.util

/**
 * Sanitizes Bangla text so colons always render as real colons.
 *
 * Bengali typing tools (Avro, Bijoy, some keyboards) convert the ":" key into
 * the visarga sign "ঃ" (U+0983) by design. In typed text that sign is usually
 * meant to be a colon (e.g. "মোটঃ ৫০" should read "মোট: ৫০"), but it can also
 * be a genuine part of a word (দুঃখ, দুঃখিত, নিঃশব্দ).
 *
 * [colonizeVisarga] replaces "ঃ" with ":" only when it is used as punctuation —
 * i.e. when it is NOT followed by a Bengali letter. Genuine in-word visargas
 * are preserved.
 */
object BanglaTextSanitizer {

    private const val BENGALI_VISARGA = '\u0983'

    /**
     * Replaces colon-usage visarga signs with ASCII colons.
     *
     * Example: "মোটঃ ৫০" → "মোট: ৫০", "কথাঃ" → "কথা:"
     * Preserved: "দুঃখ", "দুঃখিত", "নিঃশব্দ"
     */
    fun colonizeVisarga(text: String): String {
        if (text.isEmpty() || text.indexOf(BENGALI_VISARGA) < 0) return text
        val sb = StringBuilder(text.length)
        for (i in text.indices) {
            val ch = text[i]
            if (ch == BENGALI_VISARGA) {
                val next = text.getOrNull(i + 1)
                sb.append(if (next == null || !isBengaliLetter(next)) ':' else ch)
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun isBengaliLetter(ch: Char): Boolean = ch in '\u0980'..'\u09FF'
}
