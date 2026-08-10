package app.cash.tanvir.info.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [ChangelogParser] (changelog.json parsing).
 */
class ChangelogParserTest {

    @Test
    fun testParse_ValidBody_MultipleReleases() {
        val json = """
            {
              "releases": [
                {
                  "tagName": "v2.3.1",
                  "publishedAt": "2026-08-10T09:00:00Z",
                  "items": [
                    { "title": "feat: add changelog", "subItems": ["Add changelog card.", "Strip hashes."] },
                    { "title": "fix: portrait lock", "subItems": [] }
                  ]
                },
                {
                  "tagName": "v2.3.0",
                  "publishedAt": "2026-08-10T00:40:40Z",
                  "items": [
                    { "title": "docs: agent guides", "subItems": ["Add AGENTS.md."] }
                  ]
                }
              ]
            }
        """.trimIndent()
        val releases = ChangelogParser.parse(json)
        assertEquals(2, releases.size)
        val latest = releases[0]
        assertEquals("v2.3.1", latest.tagName)
        assertEquals(2, latest.items.size)
        assertEquals("feat: add changelog", latest.items[0].title)
        assertEquals(listOf("Add changelog card.", "Strip hashes."), latest.items[0].subItems)
        assertTrue(latest.items[1].subItems.isEmpty())
        assertEquals(1786352400000L, latest.publishedAt)
    }

    @Test
    fun testParse_NewestFirstOrder_Preserved() {
        val json = """
            {
              "releases": [
                { "tagName": "v2.0.5", "publishedAt": "2026-08-10T00:14:13Z",
                  "items": [ { "title": "style: polish UI", "subItems": [] } ] },
                { "tagName": "v2.0.4", "publishedAt": "2026-08-09T23:20:42Z",
                  "items": [ { "title": "fix: imports", "subItems": [] } ] }
              ]
            }
        """.trimIndent()
        val releases = ChangelogParser.parse(json)
        assertEquals(listOf("v2.0.5", "v2.0.4"), releases.map { it.tagName })
    }

    @Test
    fun testStripCommitHash_RemovesTrailingHash() {
        assertEquals("feat: add agent skills", ChangelogParser.stripCommitHash("feat: add agent skills (faa4d87)"))
        assertEquals("feat: add agent skills", ChangelogParser.stripCommitHash("* feat: add agent skills (faa4d87)"))
        assertEquals("fix: portrait", ChangelogParser.stripCommitHash("fix: portrait (eb98450) "))
        assertEquals("no hash", ChangelogParser.stripCommitHash("no hash"))
        assertEquals("keep real parens", ChangelogParser.stripCommitHash("keep real parens (note here)"))
    }

    @Test
    fun testParse_MalformedJson_ReturnsEmptyList() {
        assertTrue(ChangelogParser.parse("{not valid json").isEmpty())
        assertTrue(ChangelogParser.parse("").isEmpty())
        assertTrue(ChangelogParser.parse("null").isEmpty())
    }

    @Test
    fun testParse_MissingReleasesArray_ReturnsEmptyList() {
        assertTrue(ChangelogParser.parse("""{"updatedAt": "2026-08-10T09:00:00Z"}""".trimIndent()).isEmpty())
    }

    @Test
    fun testParse_ReleaseWithoutItems_Skipped() {
        val json = """
            {
              "releases": [
                { "tagName": "v2.3.0", "publishedAt": "2026-08-10T00:40:40Z",
                  "items": [ { "title": "  ", "subItems": [] } ] },
                { "tagName": "v2.0.5", "publishedAt": "2026-08-10T00:14:13Z",
                  "items": [ { "title": "real title", "subItems": ["sub"] } ] }
              ]
            }
        """.trimIndent()
        val releases = ChangelogParser.parse(json)
        assertEquals(1, releases.size)
        assertEquals("v2.0.5", releases[0].tagName)
    }

    @Test
    fun testParse_BlankTagName_Skipped() {
        val json = """
            {
              "releases": [
                { "tagName": "", "publishedAt": "2026-08-10T00:40:40Z",
                  "items": [ { "title": "x", "subItems": [] } ] }
              ]
            }
        """.trimIndent()
        assertTrue(ChangelogParser.parse(json).isEmpty())
    }
}
