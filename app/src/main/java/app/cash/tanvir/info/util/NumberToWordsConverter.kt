package app.cash.tanvir.info.util

/**
 * Converts numbers to words in both English and Bangla,
 * using the Bangladeshi numbering system (Hundred, Thousand, Lakh, Crore).
 *
 * Supports values from 0 up to 9,99,99,99,999 (999 Crore).
 */
object NumberToWordsConverter {

    // ── English word tables ──

    private val ONES_EN = arrayOf(
        "", "One", "Two", "Three", "Four", "Five",
        "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen",
        "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    )

    private val TENS_EN = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty",
        "Sixty", "Seventy", "Eighty", "Ninety"
    )

    // ── Bangla word tables ──

    private val ONES_BN = arrayOf(
        "", "এক", "দুই", "তিন", "চার", "পাঁচ",
        "ছয়", "সাত", "আট", "নয়", "দশ",
        "এগারো", "বারো", "তেরো", "চৌদ্দ", "পনেরো",
        "ষোলো", "সতেরো", "আঠারো", "উনিশ"
    )

    private val TENS_BN = arrayOf(
        "", "", "বিশ", "ত্রিশ", "চল্লিশ", "পঞ্চাশ",
        "ষাট", "সত্তর", "আশি", "নব্বই"
    )

    // Special Bangla numbers 20-99 (Bangla has unique words for many of these)
    private val BANGLA_20_TO_99 = mapOf(
        20 to "বিশ", 21 to "একুশ", 22 to "বাইশ", 23 to "তেইশ",
        24 to "চব্বিশ", 25 to "পঁচিশ", 26 to "ছাব্বিশ", 27 to "সাতাশ",
        28 to "আটাশ", 29 to "উনত্রিশ",
        30 to "ত্রিশ", 31 to "একত্রিশ", 32 to "বত্রিশ", 33 to "তেত্রিশ",
        34 to "চৌত্রিশ", 35 to "পঁয়ত্রিশ", 36 to "ছত্রিশ", 37 to "সাতত্রিশ",
        38 to "আটত্রিশ", 39 to "উনচল্লিশ",
        40 to "চল্লিশ", 41 to "একচল্লিশ", 42 to "বিয়াল্লিশ", 43 to "তেতাল্লিশ",
        44 to "চুয়াল্লিশ", 45 to "পঁয়তাল্লিশ", 46 to "ছেচল্লিশ", 47 to "সাতচল্লিশ",
        48 to "আটচল্লিশ", 49 to "উনপঞ্চাশ",
        50 to "পঞ্চাশ", 51 to "একান্ন", 52 to "বায়ান্ন", 53 to "তিপান্ন",
        54 to "চুয়ান্ন", 55 to "পঞ্চান্ন", 56 to "ছাপান্ন", 57 to "সাতান্ন",
        58 to "আটান্ন", 59 to "উনষাট",
        60 to "ষাট", 61 to "একষট্টি", 62 to "বাষট্টি", 63 to "তেষট্টি",
        64 to "চৌষট্টি", 65 to "পঁয়ষট্টি", 66 to "ছেষট্টি", 67 to "সাতষট্টি",
        68 to "আটষট্টি", 69 to "উনসত্তর",
        70 to "সত্তর", 71 to "একাত্তর", 72 to "বাহাত্তর", 73 to "তিয়াত্তর",
        74 to "চুয়াত্তর", 75 to "পঁচাত্তর", 76 to "ছিয়াত্তর", 77 to "সাতাত্তর",
        78 to "আটাত্তর", 79 to "উনআশি",
        80 to "আশি", 81 to "একাশি", 82 to "বিরাশি", 83 to "তিরাশি",
        84 to "চুরাশি", 85 to "পঁচাশি", 86 to "ছিয়াশি", 87 to "সাতাশি",
        88 to "আটাশি", 89 to "উননব্বই",
        90 to "নব্বই", 91 to "একানব্বই", 92 to "বিরানব্বই", 93 to "তিরানব্বই",
        94 to "চুরানব্বই", 95 to "পঁচানব্বই", 96 to "ছিয়ানব্বই", 97 to "সাতানব্বই",
        98 to "আটানব্বই", 99 to "নিরানব্বই"
    )

    private val HUNDRED_EN = "Hundred"
    private val THOUSAND_EN = "Thousand"
    private val LAKH_EN = "Lakh"
    private val CRORE_EN = "Crore"

    private val THOUSAND_BN = "হাজার"
    private val LAKH_BN = "লক্ষ"
    private val CRORE_BN = "কোটি"

    // Bangla hundred prefixes (special forms)
    private val HUNDREDS_BN = arrayOf(
        "", "একশ", "দুইশ", "তিনশ", "চারশ", "পাঁচশ",
        "ছয়শ", "সাতশ", "আটশ", "নয়শ"
    )

    /**
     * Convert a number to words in English using Bangladeshi numbering.
     * Example: 125650 → "One Lakh Twenty-Five Thousand Six Hundred Fifty Taka Only"
     */
    fun toEnglish(amount: Long): String {
        if (amount == 0L) return "Zero Taka"
        if (amount < 0L) return "Negative amount"

        val parts = mutableListOf<String>()

        var remaining = amount

        // Crore (1,00,00,000) — can be up to 999 Crore
        val crore = remaining / 10000000
        if (crore > 0) {
            parts.add("${threeDigitEnglish(crore.toInt())} $CRORE_EN")
            remaining %= 10000000
        }

        // Lakh (1,00,000)
        val lakh = remaining / 100000
        if (lakh > 0) {
            parts.add("${twoDigitEnglish(lakh.toInt())} $LAKH_EN")
            remaining %= 100000
        }

        // Thousand (1,000)
        val thousand = remaining / 1000
        if (thousand > 0) {
            parts.add("${twoDigitEnglish(thousand.toInt())} $THOUSAND_EN")
            remaining %= 1000
        }

        // Hundred
        val hundred = remaining / 100
        if (hundred > 0) {
            parts.add("${ONES_EN[hundred.toInt()]} $HUNDRED_EN")
            remaining %= 100
        }

        // Ones and Tens
        if (remaining > 0) {
            parts.add(twoDigitEnglish(remaining.toInt()))
        }

        return parts.joinToString(" ") + " Taka Only"
    }

    /**
     * Convert a number to words in Bangla using Bangladeshi numbering.
     * Example: 125650 → "এক লক্ষ পঁচিশ হাজার ছয়শ পঞ্চাশ টাকা মাত্র"
     */
    fun toBangla(amount: Long): String {
        if (amount == 0L) return "শূন্য টাকা"
        if (amount < 0L) return "ঋণাত্মক পরিমাণ"

        val parts = mutableListOf<String>()

        var remaining = amount

        // Crore — can be up to 999 Crore
        val crore = remaining / 10000000
        if (crore > 0) {
            parts.add("${threeDigitBangla(crore.toInt())} $CRORE_BN")
            remaining %= 10000000
        }

        // Lakh
        val lakh = remaining / 100000
        if (lakh > 0) {
            parts.add("${twoDigitBangla(lakh.toInt())} $LAKH_BN")
            remaining %= 100000
        }

        // Thousand
        val thousand = remaining / 1000
        if (thousand > 0) {
            parts.add("${twoDigitBangla(thousand.toInt())} $THOUSAND_BN")
            remaining %= 1000
        }

        // Hundred
        val hundred = remaining / 100
        if (hundred > 0) {
            parts.add(HUNDREDS_BN[hundred.toInt()])
            remaining %= 100
        }

        // Ones and Tens
        if (remaining > 0) {
            parts.add(twoDigitBangla(remaining.toInt()))
        }

        return parts.joinToString(" ") + " টাকা মাত্র"
    }

    /** Convert a 1-999 number to English words for Crore */
    private fun threeDigitEnglish(n: Int): String {
        val hundred = n / 100
        val rem = n % 100
        val parts = mutableListOf<String>()
        if (hundred > 0) {
            parts.add("${ONES_EN[hundred]} $HUNDRED_EN")
        }
        if (rem > 0) {
            parts.add(twoDigitEnglish(rem))
        }
        return parts.joinToString(" ")
    }

    /** Convert a 1-999 number to Bangla words for Crore */
    private fun threeDigitBangla(n: Int): String {
        val hundred = n / 100
        val rem = n % 100
        val parts = mutableListOf<String>()
        if (hundred > 0) {
            parts.add(HUNDREDS_BN[hundred])
        }
        if (rem > 0) {
            parts.add(twoDigitBangla(rem))
        }
        return parts.joinToString(" ")
    }

    /** Convert a 1-99 number to English words */
    private fun twoDigitEnglish(n: Int): String {
        return when {
            n < 20 -> ONES_EN[n]
            n % 10 == 0 -> TENS_EN[n / 10]
            else -> "${TENS_EN[n / 10]}-${ONES_EN[n % 10]}"
        }
    }

    /** Convert a 1-99 number to Bangla words */
    private fun twoDigitBangla(n: Int): String {
        return when {
            n < 20 -> ONES_BN[n]
            else -> BANGLA_20_TO_99[n] ?: "${TENS_BN[n / 10]} ${ONES_BN[n % 10]}"
        }
    }
}
