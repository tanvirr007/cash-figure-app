package app.cash.tanvir.info.util

import app.cash.tanvir.info.domain.model.UpdateManifest
import org.json.JSONObject

/**
 * Parses the OTA `version.json` body into an [UpdateManifest].
 * Pure function (org.json only) — unit-tested. Malformed input → null.
 */
object UpdateManifestParser {

    /**
     * @param body raw JSON text from the manifest endpoint.
     * @return parsed [UpdateManifest] when `versionCode > 0` and `downloadUrl` is
     *         non-blank; null on malformed JSON or missing/invalid required fields.
     */
    fun parse(body: String): UpdateManifest? {
        return try {
            val json = JSONObject(body)
            UpdateManifest(
                versionCode = json.optLong("versionCode", 0L),
                versionName = json.optString("versionName", ""),
                downloadUrl = json.optString("downloadUrl", ""),
                changelog = json.optString("changelog", ""),
                fileSize = json.optLong("fileSize", 0L).takeIf { it > 0L }
            ).takeIf { it.versionCode > 0L && it.downloadUrl.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}
