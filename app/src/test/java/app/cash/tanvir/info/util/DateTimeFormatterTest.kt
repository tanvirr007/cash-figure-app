package app.cash.tanvir.info.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DateTimeFormatterTest {

    private fun getTimestampFor(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    @Test
    fun testEnglishFormatting() {
        // Test 8:15 PM on Aug 9, 2026
        val timestamp = getTimestampFor(2026, Calendar.AUGUST, 9, 20, 15)
        val formatted = DateTimeFormatter.format(timestamp, isBangla = false)
        assertEquals("09 Aug 2026, 08:15 PM", formatted)
    }

    @Test
    fun testBanglaFormatting_Vhor() {
        // ভোর — early morning, ~4–6 AM
        val t1 = getTimestampFor(2026, Calendar.AUGUST, 9, 4, 0)
        assertEquals("০৯ আগস্ট ২০২৬, ভোর ০৪:০০", DateTimeFormatter.format(t1, isBangla = true))

        val t2 = getTimestampFor(2026, Calendar.AUGUST, 9, 5, 59)
        assertEquals("০৯ আগস্ট ২০২৬, ভোর ০৫:৫৯", DateTimeFormatter.format(t2, isBangla = true))
    }

    @Test
    fun testBanglaFormatting_Sokal() {
        // সকাল — morning, ~6–12 PM (6 AM - 12 PM)
        val t1 = getTimestampFor(2026, Calendar.AUGUST, 9, 6, 0)
        assertEquals("০৯ আগস্ট ২০২৬, সকাল ০৬:০০", DateTimeFormatter.format(t1, isBangla = true))

        val t2 = getTimestampFor(2026, Calendar.AUGUST, 9, 11, 59)
        assertEquals("০৯ আগস্ট ২০২৬, সকাল ১১:৫৯", DateTimeFormatter.format(t2, isBangla = true))
    }

    @Test
    fun testBanglaFormatting_Dupur() {
        // দুপুর — noon/early afternoon, ~12–3 PM
        val t1 = getTimestampFor(2026, Calendar.AUGUST, 9, 12, 0)
        assertEquals("০৯ আগস্ট ২০২৬, দুপুর ১২:০০", DateTimeFormatter.format(t1, isBangla = true))

        val t2 = getTimestampFor(2026, Calendar.AUGUST, 9, 14, 59)
        assertEquals("০৯ আগস্ট ২০২৬, দুপুর ০২:৫৯", DateTimeFormatter.format(t2, isBangla = true))
    }

    @Test
    fun testBanglaFormatting_Bikal() {
        // বিকাল — afternoon, ~3–6 PM
        val t1 = getTimestampFor(2026, Calendar.AUGUST, 9, 15, 0)
        assertEquals("০৯ আগস্ট ২০২৬, বিকাল ০৩:০০", DateTimeFormatter.format(t1, isBangla = true))

        val t2 = getTimestampFor(2026, Calendar.AUGUST, 9, 17, 59)
        assertEquals("০৯ আগস্ট ২০২৬, বিকাল ০৫:৫৯", DateTimeFormatter.format(t2, isBangla = true))
    }

    @Test
    fun testBanglaFormatting_Shondhya() {
        // সন্ধ্যা — evening, ~6–8 PM
        val t1 = getTimestampFor(2026, Calendar.AUGUST, 9, 18, 0)
        assertEquals("০৯ আগস্ট ২০২৬, সন্ধ্যা ০৬:০০", DateTimeFormatter.format(t1, isBangla = true))

        val t2 = getTimestampFor(2026, Calendar.AUGUST, 9, 19, 59)
        assertEquals("০৯ আগস্ট ২০২৬, সন্ধ্যা ০৭:৫৯", DateTimeFormatter.format(t2, isBangla = true))
    }

    @Test
    fun testBanglaFormatting_Ratri() {
        // রাত্রি — night, ~8 PM–4 AM
        val t1 = getTimestampFor(2026, Calendar.AUGUST, 9, 20, 0)
        assertEquals("০৯ আগস্ট ২০২৬, রাত্রি ০৮:০০", DateTimeFormatter.format(t1, isBangla = true))

        val t2 = getTimestampFor(2026, Calendar.AUGUST, 9, 23, 59)
        assertEquals("০৯ আগস্ট ২০২৬, রাত্রি ১১:৫৯", DateTimeFormatter.format(t2, isBangla = true))

        val t3 = getTimestampFor(2026, Calendar.AUGUST, 9, 0, 0) // midnight
        assertEquals("০৯ আগস্ট ২০২৬, রাত্রি ১২:০০", DateTimeFormatter.format(t3, isBangla = true))

        val t4 = getTimestampFor(2026, Calendar.AUGUST, 9, 3, 59)
        assertEquals("০৯ আগস্ট ২০২৬, রাত্রি ০৩:৫৯", DateTimeFormatter.format(t4, isBangla = true))
    }

    @Test
    fun testBanglaFormatting_MonthTranslations() {
        val months = listOf(
            Calendar.JANUARY to "জানুয়ারি",
            Calendar.FEBRUARY to "ফেব্রুয়ারি",
            Calendar.MARCH to "মার্চ",
            Calendar.APRIL to "এপ্রিল",
            Calendar.MAY to "মে",
            Calendar.JUNE to "জুন",
            Calendar.JULY to "জুলাই",
            Calendar.AUGUST to "আগস্ট",
            Calendar.SEPTEMBER to "সেপ্টেম্বর",
            Calendar.OCTOBER to "অক্টোবর",
            Calendar.NOVEMBER to "নভেম্বর",
            Calendar.DECEMBER to "ডিসেম্বর"
        )
        for ((monthConst, expectedMonthName) in months) {
            val timestamp = getTimestampFor(2026, monthConst, 15, 12, 0)
            val formatted = DateTimeFormatter.format(timestamp, isBangla = true)
            // Example output: "১৫ জানুয়ারি ২০২৬, দুপুর ১২:০০"
            assertEquals("১৫ $expectedMonthName ২০২৬, দুপুর ১২:০০", formatted)
        }
    }

    @Test
    fun testFormatUpdatedOn_English() {
        // 8:15 PM on Aug 9, 2026 → time first, full month name
        val timestamp = getTimestampFor(2026, Calendar.AUGUST, 9, 20, 15)
        assertEquals("08:15 PM, 09 August 2026", DateTimeFormatter.formatUpdatedOn(timestamp, isBangla = false))
    }

    @Test
    fun testFormatUpdatedOn_Bangla() {
        val t1 = getTimestampFor(2026, Calendar.JULY, 12, 10, 45)
        assertEquals("সকাল ১০:৪৫, ১২ জুলাই ২০২৬", DateTimeFormatter.formatUpdatedOn(t1, isBangla = true))

        val t2 = getTimestampFor(2026, Calendar.AUGUST, 9, 20, 15)
        assertEquals("রাত্রি ০৮:১৫, ০৯ আগস্ট ২০২৬", DateTimeFormatter.formatUpdatedOn(t2, isBangla = true))
    }

    @Test
    fun testFormatTime_English() {
        val timestamp = getTimestampFor(2026, Calendar.AUGUST, 9, 20, 15)
        assertEquals("08:15 PM", DateTimeFormatter.formatTime(timestamp, isBangla = false))

        val morning = getTimestampFor(2026, Calendar.AUGUST, 9, 10, 45)
        assertEquals("10:45 AM", DateTimeFormatter.formatTime(morning, isBangla = false))
    }

    @Test
    fun testFormatTime_Bangla() {
        val t1 = getTimestampFor(2026, Calendar.JULY, 12, 10, 45)
        assertEquals("সকাল ১০:৪৫", DateTimeFormatter.formatTime(t1, isBangla = true))

        val t2 = getTimestampFor(2026, Calendar.AUGUST, 9, 20, 15)
        assertEquals("রাত্রি ০৮:১৫", DateTimeFormatter.formatTime(t2, isBangla = true))

        val t3 = getTimestampFor(2026, Calendar.AUGUST, 9, 12, 0)
        assertEquals("দুপুর ১২:০০", DateTimeFormatter.formatTime(t3, isBangla = true))
    }
}
