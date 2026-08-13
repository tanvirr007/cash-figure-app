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
    val changelog: String,
    val fileSize: Long? = null
)

/**
 * Completed APK download, ready to install.
 * Pure JVM types: [uri] is the installable FileProvider content URI and
 * converts to an Android Uri via `Uri.parse(uri.toString())` at the UI layer.
 *
 * @param uri  FileProvider `content://…fileprovider/ota/…` URI for ACTION_VIEW
 * @param file physical APK File (app-private `filesDir/ota/`, wiped on uninstall)
 */
data class DownloadedUpdate(
    val uri: URI,
    val file: File
)
