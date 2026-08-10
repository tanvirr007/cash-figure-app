package app.cash.tanvir.info.domain.model

import java.io.File
import java.net.URI

/**
 * OTA manifest mirrored from version.json (raw GitHub CDN).
 * Pure Kotlin — no Android dependencies. Unit-testable.
 */
data class UpdateManifest(
    val versionCode: Long,
    val versionName: String,
    val downloadUrl: String,
    val changelog: String
)

/**
 * Completed APK download, ready to install.
 * Pure JVM types: [uri] is the installable content URI and converts to an
 * Android Uri via `Uri.parse(uri.toString())` at the UI layer.
 *
 * @param uri  content URI for ACTION_VIEW:
 *             • MediaStore `content://media/…` on API 29+ (app owns the entry)
 *             • FileProvider `content://…fileprovider/…` on API ≤ 28
 * @param file physical APK File on API ≤ 28 only (null on API 29+, MediaStore-backed)
 */
data class DownloadedUpdate(
    val uri: URI,
    val file: File?
)
