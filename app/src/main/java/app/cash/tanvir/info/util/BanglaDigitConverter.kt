package app.cash.tanvir.info.util

/**
 * Converts Western (ASCII) digits to Bengali digits and vice versa.
 *
 * Western: 0 1 2 3 4 5 6 7 8 9
 * Bengali: ০ ১ ২ ৩ ৪ ৫ ৬ ৭ ৮ ৯
 */
object BanglaDigitConverter {

    private val BENGALI_DIGITS = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    /**
     * Convert all Western digits in a string to Bengali digits.
     * Non-digit characters (commas, taka sign, etc.) are preserved.
     *
     * Example: "৳1,25,650" → "৳১,২৫,৬৫০"
     */
    fun toBengali(text: String): String {
        val sb = StringBuilder(text.length)
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(BENGALI_DIGITS[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Convert all Bengali digits in a string to Western digits.
     *
     * Example: "৳১,২৫,৬৫০" → "৳1,25,650"
     */
    fun toWestern(text: String): String {
        val sb = StringBuilder(text.length)
        for (ch in text) {
            val index = BENGALI_DIGITS.indexOf(ch)
            if (index >= 0) {
                sb.append(index)
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
}
