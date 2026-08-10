package app.cash.tanvir.info.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [SizeFormatter] (byte-size formatting).
 */
class SizeFormatterTest {

    @Test
    fun testFormat_Zero() {
        assertEquals("0 B", SizeFormatter.format(0))
    }

    @Test
    fun testFormat_SmallBytes() {
        assertEquals("512 B", SizeFormatter.format(512))
    }

    @Test
    fun testFormat_OneKilobyte() {
        assertEquals("1.0 KB", SizeFormatter.format(1024))
    }

    @Test
    fun testFormat_PointSixMegaBytes() {
        assertEquals("2.60 MB", SizeFormatter.format(2_726_297))
    }

    @Test
    fun testFormat_OneGigabyte() {
        assertEquals("1.00 GB", SizeFormatter.format(1_073_741_824))
    }

    @Test
    fun testFormat_Negative() {
        assertEquals("", SizeFormatter.format(-1))
    }
}
