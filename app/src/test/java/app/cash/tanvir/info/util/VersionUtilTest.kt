package app.cash.tanvir.info.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [compareVersions] and [isUpdateAvailable] (OTA update decision).
 */
class VersionUtilTest {

    @Test
    fun testCompareVersions_SimpleBumps() {
        assertTrue(compareVersions("3.4.6", "3.4.5") > 0)
        assertTrue(compareVersions("3.4.5", "3.4.6") < 0)
        assertEquals(0, compareVersions("3.4.6", "3.4.6"))
    }

    @Test
    fun testCompareVersions_StripsVPrefix() {
        assertTrue(compareVersions("v3.4.6", "3.4.5") > 0)
        assertEquals(0, compareVersions("v3.4.6", "3.4.6"))
        assertEquals(0, compareVersions("V3.4.6", "3.4.6"))
    }

    @Test
    fun testCompareVersions_MultiDigitSegments() {
        assertTrue(compareVersions("3.10.0", "3.9.9") > 0)
        assertTrue(compareVersions("3.4.10", "3.4.9") > 0)
    }

    @Test
    fun testCompareVersions_UnequalSegmentCounts() {
        assertTrue(compareVersions("3.4", "3.3.9") > 0)
        assertTrue(compareVersions("3.4.0", "3.4") > 0)
    }

    @Test
    fun testCompareVersions_GarbageFallsBackToZero() {
        assertEquals(0, compareVersions("garbage", "3.4.5"))
        assertEquals(0, compareVersions("", "3.4.5"))
        assertEquals(0, compareVersions("1.x.3", "3.4.5"))
    }

    @Test
    fun testIsUpdateAvailable_NormalCase() {
        assertTrue(isUpdateAvailable("v3.4.6", 38L, "3.4.5", 36L))
    }

    @Test
    fun testIsUpdateAvailable_StuckInstallWithHigherCode() {
        // Re-run build: installed name older but version code higher than manifest.
        assertTrue(isUpdateAvailable("v3.4.6", 38L, "3.4.4", 39L))
    }

    @Test
    fun testIsUpdateAvailable_InstalledNameNewerNeverDowngrades() {
        assertFalse(isUpdateAvailable("v3.4.6", 38L, "3.4.7", 30L))
    }

    @Test
    fun testIsUpdateAvailable_EqualNamesUseCode() {
        assertFalse(isUpdateAvailable("v3.4.6", 38L, "3.4.6", 39L))
        assertTrue(isUpdateAvailable("v3.4.6", 40L, "3.4.6", 39L))
    }

    @Test
    fun testIsUpdateAvailable_UnparseableNameFallsBackToCode() {
        assertTrue(isUpdateAvailable("", 40L, "3.4.6", 39L))
        assertFalse(isUpdateAvailable("", 38L, "3.4.6", 39L))
        assertTrue(isUpdateAvailable("v3.4.6", 40L, "garbage", 39L))
    }
}
