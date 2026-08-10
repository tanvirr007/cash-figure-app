package app.cash.tanvir.info.domain.repository

import app.cash.tanvir.info.domain.model.DownloadedUpdate
import app.cash.tanvir.info.domain.model.ReleaseChangelog
import app.cash.tanvir.info.domain.model.UpdateManifest

/**
 * Contract for the OTA update pipeline.
 * Implementations MUST be network-IO only (called from Dispatchers.IO).
 */
interface UpdateRepository {

    /**
     * Fetches and parses the remote manifest.
     * @return parsed manifest, or null when the fetch/parse failed for any reason.
     */
    suspend fun fetchManifest(): UpdateManifest?

    /**
     * Fetches and parses the remote changelog.json (all releases, newest first).
     * @return release changelogs, or an empty list when the fetch failed.
     */
    suspend fun fetchReleaseChangelogs(): List<ReleaseChangelog>

    /**
     * Downloads the release APK to `Downloads/CashFigure/ota/` (shared Downloads,
     * visible and re-shareable by the user).
     * @param manifest   the manifest whose [UpdateManifest.downloadUrl] is downloaded
     * @param onProgress invoked on the calling context with (bytesDownloaded, totalBytes).
     *                    totalBytes may be -1 when the server omits Content-Length.
     * @return [DownloadedUpdate] with an installable content URI (and physical file on ≤28)
     * @throws Exception on any failure (caller maps to UI error state)
     */
    suspend fun downloadApk(
        manifest: UpdateManifest,
        onProgress: (downloaded: Long, total: Long) -> Unit
    ): DownloadedUpdate
}
