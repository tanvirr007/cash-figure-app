package app.cash.tanvir.info.util

import app.cash.tanvir.info.domain.model.ChangelogItem
import app.cash.tanvir.info.domain.model.ReleaseChangelog
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import org.json.JSONObject

/**
 * Parses the CI-generated `changelog.json` body into release changelogs.
 * Pure function (org.json + java.text only) — unit-tested.
 * Malformed input → empty list (never throws).
 */
object ChangelogParser {

    private const val ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'"

    /**
     * @param body raw JSON text from the changelog.json endpoint.
     * @return releases with their commit items; empty list on any parse failure.
     */
    fun parse(body: String): List<ReleaseChangelog> {
        return try {
            val root = JSONObject(body)
            val array = root.optJSONArray("releases") ?: return emptyList()
            val releases = mutableListOf<ReleaseChangelog>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val tagName = obj.optString("tagName", "")
                if (tagName.isBlank()) continue
                val items = parseItems(obj.optJSONArray("items"))
                if (items.isEmpty()) continue
                releases.add(
                    ReleaseChangelog(
                        tagName = tagName,
                        publishedAt = parseDate(obj.optString("publishedAt", "")),
                        items = items
                    )
                )
            }
            releases
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseItems(array: org.json.JSONArray?): List<ChangelogItem> {
        if (array == null) return emptyList()
        val items = mutableListOf<ChangelogItem>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val title = stripCommitHash(obj.optString("title", ""))
            if (title.isBlank()) continue
            val subItems = mutableListOf<String>()
            val subArray = obj.optJSONArray("subItems")
            if (subArray != null) {
                for (j in 0 until subArray.length()) {
                    val sub = subArray.optString(j, "").trim()
                    if (sub.isNotEmpty()) subItems.add(sub)
                }
            }
            items.add(ChangelogItem(title = title, subItems = subItems))
        }
        return items
    }

    /**
     * Strips a trailing commit-short-hash suffix like ` (faa4d87)` from a title.
     * Shared with the OTA update dialog so both surfaces render the same text.
     */
    fun stripCommitHash(text: String): String {
        return text.replace(Regex("\\s*\\([0-9a-fA-F]{7,8}\\)\\s*$"), "").trim()
    }

    /** Parses the API ISO-8601 `published_at` (UTC); 0 on failure. */
    private fun parseDate(value: String): Long {
        return try {
            val format = SimpleDateFormat(ISO_8601, Locale.ENGLISH)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(value)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }
}
