package app.cash.tanvir.info.data.repository

import android.content.Context
import androidx.core.content.FileProvider
import app.cash.tanvir.info.domain.model.DownloadedUpdate
import app.cash.tanvir.info.domain.model.ReleaseChangelog
import app.cash.tanvir.info.domain.model.UpdateManifest
import app.cash.tanvir.info.domain.repository.UpdateRepository
import app.cash.tanvir.info.util.ChangelogParser
import app.cash.tanvir.info.util.UpdateManifestParser
import app.cash.tanvir.info.util.report.StorageUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * OTA update pipeline over platform `HttpURLConnection` (zero new dependencies):
 * manifest fetch from the raw GitHub CDN + streamed APK download to
 * app-private `filesDir/ota/CashFigure.apk` (wiped on uninstall).
 */
@Singleton
class UpdateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UpdateRepository {

    override suspend fun fetchManifest(): UpdateManifest? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(MANIFEST_URL).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = CONNECT_TIMEOUT_MS
                connection.readTimeout = READ_TIMEOUT_MS
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("User-Agent", USER_AGENT)
                if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                UpdateManifestParser.parse(body)
            } finally {
                connection.disconnect()
            }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun fetchReleaseChangelogs(): List<ReleaseChangelog> =
        withContext(Dispatchers.IO) {
            try {
                val connection = URL(CHANGELOG_URL).openConnection() as HttpURLConnection
                try {
                    connection.requestMethod = "GET"
                    connection.connectTimeout = CONNECT_TIMEOUT_MS
                    connection.readTimeout = READ_TIMEOUT_MS
                    connection.setRequestProperty("Accept", "application/json")
                    connection.setRequestProperty("User-Agent", USER_AGENT)
                    if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext emptyList()
                    val body = connection.inputStream.bufferedReader().use { it.readText() }
                    ChangelogParser.parse(body)
                } finally {
                    connection.disconnect()
                }
            } catch (_: Exception) {
                emptyList()
            }
        }

    override suspend fun downloadApk(
        manifest: UpdateManifest,
        onProgress: (downloaded: Long, total: Long) -> Unit
    ): DownloadedUpdate = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, OTA_SUBFOLDER)
        if (!dir.exists() && !dir.mkdirs()) {
            throw RuntimeException("Cannot create OTA directory")
        }
        val targetFile = File(dir, FILE_NAME)
        if (targetFile.exists()) targetFile.delete()

        var downloaded = 0L
        try {
            val connection = URL(manifest.downloadUrl).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = CONNECT_TIMEOUT_MS
                connection.readTimeout = READ_TIMEOUT_MS
                connection.setRequestProperty("Accept", "application/vnd.android.package-archive")
                connection.setRequestProperty("User-Agent", USER_AGENT)
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw RuntimeException("HTTP ${connection.responseCode}")
                }
                val total = connection.contentLengthLong
                val buffer = ByteArray(BUFFER_SIZE)
                FileOutputStream(targetFile).use { out ->
                    while (true) {
                        val read = connection.inputStream.read(buffer)
                        if (read == -1) break
                        out.write(buffer, 0, read)
                        downloaded += read
                        onProgress(downloaded, total)
                    }
                    out.flush()
                }
                if (downloaded == 0L) throw RuntimeException("Empty download")
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            targetFile.delete()
            throw e
        }

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            targetFile
        )
        DownloadedUpdate(uri = URI.create(contentUri.toString()), file = targetFile)
    }

    /**
     * Best-effort removal of the OTA APK that older versions left in shared
     * Downloads (`Download/CashFigure/ota/CashFigure.apk`). Called on fresh
     * installs so no update residue survives uninstall/reinstall.
     */
    override suspend fun cleanupLegacyOtaApk() = withContext(Dispatchers.IO) {
        try {
            StorageUtil.deleteReportFile(context, FILE_NAME, OTA_SUBFOLDER)
        } catch (_: Exception) {
        }
    }

    private companion object {
        const val MANIFEST_URL =
            "https://raw.githubusercontent.com/tanvirr007/cash-figure-app/main/version.json"
        const val CHANGELOG_URL =
            "https://raw.githubusercontent.com/tanvirr007/cash-figure-app/main/changelog.json"
        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 30_000
        const val USER_AGENT = "CashFigure-OTA/2.3.1"
        const val FILE_NAME = "CashFigure.apk"
        const val OTA_SUBFOLDER = "ota"
        const val BUFFER_SIZE = 8 * 1024
    }
}
