package app.cash.tanvir.info.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [NumberToWordsConverter] covering all edge cases
 * specified in the prompt: zero, exact lakh/crore boundaries, all-9s numbers,
 * and the maximum supported value (999 Crore).
 */
class NumberToWordsConverterTest {

    @Test
    fun testZero_English() {
        val result = NumberToWordsConverter.toEnglish(0L)
        assertEquals("Zero Taka", result)
    }

    @Test
    fun testZero_Bangla() {
        val result = NumberToWordsConverter.toBangla(0L)
        assertEquals("শূন্য টাকা", result)
    }

    @Test
    fun testOne_English() {
        val result = NumberToWordsConverter.toEnglish(1L)
        assertEquals("One Taka Only", result)
    }

    @Test
    fun testOne_Bangla() {
        val result = NumberToWordsConverter.toBangla(1L)
        assertEquals("এক টাকা মাত্র", result)
    }

    @Test
    fun testNineHundredNinetyNine_English() {
        val result = NumberToWordsConverter.toEnglish(999L)
        assertEquals("Nine Hundred Ninety-Nine Taka Only", result)
    }

    @Test
    fun testNineHundredNinetyNine_Bangla() {
        val result = NumberToWordsConverter.toBangla(999L)
        assertEquals("নয়শ নিরানব্বই টাকা মাত্র", result)
    }

    @Test
    fun testOneThousand_English() {
        val result = NumberToWordsConverter.toEnglish(1000L)
        assertEquals("One Thousand Taka Only", result)
    }

    @Test
    fun testOneThousand_Bangla() {
        val result = NumberToWordsConverter.toBangla(1000L)
        assertEquals("এক হাজার টাকা মাত্র", result)
    }

    @Test
    fun testOneLakh_English() {
        val result = NumberToWordsConverter.toEnglish(100000L)
        assertEquals("One Lakh Taka Only", result)
    }

    @Test
    fun testOneLakh_Bangla() {
        val result = NumberToWordsConverter.toBangla(100000L)
        assertEquals("এক লক্ষ টাকা মাত্র", result)
    }

    @Test
    fun testTenLakh_English() {
        val result = NumberToWordsConverter.toEnglish(1000000L)
        assertEquals("Ten Lakh Taka Only", result)
    }

    @Test
    fun testTenLakh_Bangla() {
        val result = NumberToWordsConverter.toBangla(1000000L)
        assertEquals("দশ লক্ষ টাকা মাত্র", result)
    }

    @Test
    fun testOneCrore_English() {
        val result = NumberToWordsConverter.toEnglish(10000000L)
        assertEquals("One Crore Taka Only", result)
    }

    @Test
    fun testOneCrore_Bangla() {
        val result = NumberToWordsConverter.toBangla(10000000L)
        assertEquals("এক কোটি টাকা মাত্র", result)
    }

    @Test
    fun testTenCrore_English() {
        val result = NumberToWordsConverter.toEnglish(100000000L)
        assertEquals("Ten Crore Taka Only", result)
    }

    @Test
    fun testTenCrore_Bangla() {
        val result = NumberToWordsConverter.toBangla(100000000L)
        assertEquals("দশ কোটি টাকা মাত্র", result)
    }

    @Test
    fun testComplexAmount_English() {
        val result = NumberToWordsConverter.toEnglish(125650L)
        assertEquals("One Lakh Twenty-Five Thousand Six Hundred Fifty Taka Only", result)
    }

    @Test
    fun testComplexAmount_Bangla() {
        val result = NumberToWordsConverter.toBangla(125650L)
        assertEquals("এক লক্ষ পঁচিশ হাজার ছয়শ পঞ্চাশ টাকা মাত্র", result)
    }

    @Test
    fun testAllNinesEightDigits_English() {
        val result = NumberToWordsConverter.toEnglish(99999999L)
        assertEquals("Nine Crore Ninety-Nine Lakh Ninety-Nine Thousand Nine Hundred Ninety-Nine Taka Only", result)
    }

    @Test
    fun testAllNinesEightDigits_Bangla() {
        val result = NumberToWordsConverter.toBangla(99999999L)
        assertEquals("নয় কোটি নিরানব্বই লক্ষ নিরানব্বই হাজার নয়শ নিরানব্বই টাকা মাত্র", result)
    }

    @Test
    fun testMaxSupportedNineHundredNinetyNineCrore_English() {
        val result = NumberToWordsConverter.toEnglish(9999999999L)
        assertEquals("Nine Hundred Ninety-Nine Crore Ninety-Nine Lakh Ninety-Nine Thousand Nine Hundred Ninety-Nine Taka Only", result)
    }

    @Test
    fun testMaxSupportedNineHundredNinetyNineCrore_Bangla() {
        val result = NumberToWordsConverter.toBangla(9999999999L)
        assertEquals("নয়শ নিরানব্বই কোটি নিরানব্বই লক্ষ নিরানব্বই হাজার নয়শ নিরানব্বই টাকা মাত্র", result)
    }
}
