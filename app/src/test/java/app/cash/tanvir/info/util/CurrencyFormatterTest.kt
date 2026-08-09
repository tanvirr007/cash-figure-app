package app.cash.tanvir.info.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [CurrencyFormatter] and [BanglaDigitConverter].
 */
class CurrencyFormatterTest {

    @Test
    fun testWesternFormatting_SmallNumber() {
        val formatted = CurrencyFormatter.format(500)
        assertEquals("BDT 500", formatted)
    }

    @Test
    fun testWesternFormatting_OneThousand() {
        val formatted = CurrencyFormatter.format(1000)
        assertEquals("BDT 1,000", formatted)
    }

    @Test
    fun testWesternFormatting_BangladeshiGrouping_OneLakh() {
        val formatted = CurrencyFormatter.format(100000)
        assertEquals("BDT 1,00,000", formatted)
    }

    @Test
    fun testWesternFormatting_BangladeshiGrouping_Complex() {
        val formatted = CurrencyFormatter.format(125650)
        assertEquals("BDT 1,25,650", formatted)
    }

    @Test
    fun testWesternFormatting_BangladeshiGrouping_TenLakh() {
        val formatted = CurrencyFormatter.format(1250000)
        assertEquals("BDT 12,50,000", formatted)
    }

    @Test
    fun testWesternFormatting_BangladeshiGrouping_OneCrore() {
        val formatted = CurrencyFormatter.format(10000000)
        assertEquals("BDT 1,00,00,000", formatted)
    }

    @Test
    fun testBengaliFormatting_BangladeshiGrouping_Complex() {
        val formatted = CurrencyFormatter.format(125650, useBengaliDigits = true)
        assertEquals("৳১,২৫,৬৫০/-", formatted)
    }

    @Test
    fun testBengaliDigitConverter_toBengali() {
        val bengali = BanglaDigitConverter.toBengali("৳1,25,650")
        assertEquals("৳১,২৫,৬৫০", bengali)
    }

    @Test
    fun testBengaliDigitConverter_toWestern() {
        val western = BanglaDigitConverter.toWestern("৳১,২৫,৬৫০")
        assertEquals("৳1,25,650", western)
    }
}
