package app.cash.tanvir.info.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [UpdateManifestParser] (OTA version.json parsing).
 */
class UpdateManifestParserTest {

    @Test
    fun testParse_ValidManifest() {
        val json = """
            {
              "versionCode": 15,
              "versionName": "v2.0.5",
              "downloadUrl": "https://github.com/tanvirr007/cash-figure-app/releases/download/v2.0.5/CashFigure.apk",
              "changelog": "* **style: polish UI animations** (0822cb6)\n  - Add slide transitions."
            }
        """.trimIndent()
        val manifest = UpdateManifestParser.parse(json)
        assertEquals(15L, manifest?.versionCode)
        assertEquals("v2.0.5", manifest?.versionName)
        assertEquals("https://github.com/tanvirr007/cash-figure-app/releases/download/v2.0.5/CashFigure.apk", manifest?.downloadUrl)
        assertTrue(manifest?.changelog?.contains("0822cb6") == true)
        assertNull(manifest?.fileSize)
    }

    @Test
    fun testParse_FileSize_Parsed() {
        val json = """
            {
              "versionCode": 18,
              "versionName": "v2.2.0",
              "downloadUrl": "https://example.com/CashFigure.apk",
              "changelog": "x",
              "fileSize": 25165824
            }
        """.trimIndent()
        val manifest = UpdateManifestParser.parse(json)
        assertEquals(18L, manifest?.versionCode)
        assertEquals(25165824L, manifest?.fileSize)
    }

    @Test
    fun testParse_ZeroFileSize_Ignored() {
        val json = """
            {
              "versionCode": 19,
              "versionName": "v2.2.1",
              "downloadUrl": "https://example.com/CashFigure.apk",
              "changelog": "x",
              "fileSize": 0
            }
        """.trimIndent()
        assertNull(UpdateManifestParser.parse(json)?.fileSize)
    }

    @Test
    fun testParse_MissingVersionCode_FailsValidation() {
        val json = """
            {
              "versionName": "v2.0.5",
              "downloadUrl": "https://example.com/CashFigure.apk",
              "changelog": "x"
            }
        """.trimIndent()
        assertNull(UpdateManifestParser.parse(json))
    }

    @Test
    fun testParse_ZeroVersionCode_FailsValidation() {
        val json = """
            {
              "versionCode": 0,
              "versionName": "v2.0.5",
              "downloadUrl": "https://example.com/CashFigure.apk"
            }
        """.trimIndent()
        assertNull(UpdateManifestParser.parse(json))
    }

    @Test
    fun testParse_BlankDownloadUrl_FailsValidation() {
        val json = """
            {
              "versionCode": 15,
              "versionName": "v2.0.5",
              "downloadUrl": "",
              "changelog": "x"
            }
        """.trimIndent()
        assertNull(UpdateManifestParser.parse(json))
    }

    @Test
    fun testParse_MalformedJson_ReturnsNull() {
        assertNull(UpdateManifestParser.parse("{not valid json"))
        assertNull(UpdateManifestParser.parse(""))
    }

    @Test
    fun testParse_ExtraUnknownFields_Ignored() {
        val json = """
            {
              "versionCode": 16,
              "versionName": "v2.0.6",
              "downloadUrl": "https://example.com/CashFigure.apk",
              "changelog": "",
              "sha256": "abc123",
              "futureField": {"nested": true}
            }
        """.trimIndent()
        val manifest = UpdateManifestParser.parse(json)
        assertEquals(16L, manifest?.versionCode)
        assertEquals("", manifest?.changelog)
    }

    @Test
    fun testParse_VersionNameWithVPrefix_Preserved() {
        val json = """
            {
              "versionCode": 17,
              "versionName": "v2.1.0",
              "downloadUrl": "https://example.com/CashFigure.apk",
              "changelog": "x"
            }
        """.trimIndent()
        assertEquals("v2.1.0", UpdateManifestParser.parse(json)?.versionName)
    }
}
