package app.cash.tanvir.info.util

/**
 * Formats numbers using Bangladeshi digit grouping (lakh/crore style).
 *
 * Bangladeshi grouping: first group from right is 3 digits, then every 2 digits.
 * Example: 1250000 → "12,50,000" (not "1,250,000")
 *
 * With taka sign: "৳12,50,000"
 */
object CurrencyFormatter {

    /**
     * Format a number with Bangladeshi grouping.
     *
     * @param amount The number to format
     * @param withSymbol Whether to prefix with ৳
     * @param useBengaliDigits Whether to convert digits to Bengali (০-৯)
     * @return Formatted string, e.g. "৳1,25,650" or "৳১,২৫,৬৫০"
     */
    fun format(
        amount: Long,
        withSymbol: Boolean = true,
        useBengaliDigits: Boolean = false
    ): String {
        val isNegative = amount < 0
        val absAmount = if (isNegative) -amount else amount
        val numStr = absAmount.toString()

        val grouped = when {
            numStr.length <= 3 -> numStr
            else -> {
                val lastThree = numStr.takeLast(3)
                val rest = numStr.dropLast(3)
                val chunks = mutableListOf<String>()

                var i = rest.length
                while (i > 0) {
                    val start = maxOf(0, i - 2)
                    chunks.add(0, rest.substring(start, i))
                    i = start
                }

                chunks.joinToString(",") + "," + lastThree
            }
        }

        val prefix = buildString {
            if (isNegative) append("-")
            if (withSymbol) append("৳")
        }

        val result = "$prefix$grouped"
        return if (useBengaliDigits) BanglaDigitConverter.toBengali(result) else result
    }

    /**
     * Format with no symbol, just the grouped number.
     */
    fun formatNumber(amount: Long, useBengaliDigits: Boolean = false): String {
        return format(amount, withSymbol = false, useBengaliDigits = useBengaliDigits)
    }
}
