package app.cash.tanvir.info.util.report

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for saving report files to `Download/CashFigure/`
 * using MediaStore API on Android 10+ (API >= 29) and File API on older versions.
 */
object StorageUtil {

    fun generateFileName(extension: String): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "CashFigure_$timeStamp.$extension"
    }

    /**
     * Save report data (byte array) to `Download/CashFigure/<subFolder>/`.
     * Automatically handles duplicate filenames by appending a suffix if needed.
     */
    fun saveReportFile(
        context: Context,
        fileName: String,
        mimeType: String,
        data: ByteArray,
        subFolder: String = ""
    ): Uri? {
        val relativeSubPath = if (subFolder.isBlank()) "CashFigure" else "CashFigure/$subFolder"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/" + relativeSubPath)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { os ->
                    os.write(data)
                    os.flush()
                }
            }
            uri
        } else {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                relativeSubPath
            )
            if (!dir.exists()) dir.mkdirs()

            var targetFile = File(dir, fileName)
            if (targetFile.exists()) {
                val nameWithoutExt = fileName.substringBeforeLast(".")
                val ext = fileName.substringAfterLast(".")
                targetFile = File(dir, "${nameWithoutExt}_1.$ext")
            }

            FileOutputStream(targetFile).use { os ->
                os.write(data)
                os.flush()
            }
            Uri.fromFile(targetFile)
        }
    }

    /**
     * Opens a fresh, empty destination in Downloads/CashFigure/<subFolder>/
     * for streaming. Deletes any existing file with the same name first so a
     * single canonical file per name is kept. Returns null when storage is
     * unavailable or creation failed.
     */
    fun openReportFile(
        context: Context,
        fileName: String,
        mimeType: String,
        subFolder: String = ""
    ): OpenFileHandle? {
        val relativeSubPath = if (subFolder.isBlank()) "CashFigure" else "CashFigure/$subFolder"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val resolver = context.contentResolver
                deleteMatchingEntries(resolver, fileName, relativeSubPath)

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/" + relativeSubPath)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) ?: return null
                val os = resolver.openOutputStream(uri) ?: run {
                    resolver.delete(uri, null, null)
                    return null
                }
                OpenFileHandle(os, uri, file = null, resolver)
            } catch (_: Exception) {
                null
            }
        } else {
            try {
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    relativeSubPath
                )
                if (!dir.exists() && !dir.mkdirs()) return null

                val targetFile = File(dir, fileName)
                if (targetFile.exists()) targetFile.delete()

                val fos = FileOutputStream(targetFile)
                OpenFileHandle(fos, Uri.fromFile(targetFile), targetFile, context.contentResolver)
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun deleteMatchingEntries(resolver: ContentResolver, fileName: String, relativeSubPath: String) {
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
        val selectionArgs = arrayOf(fileName, Environment.DIRECTORY_DOWNLOADS + "/" + relativeSubPath)
        resolver.delete(MediaStore.Downloads.EXTERNAL_CONTENT_URI, selection, selectionArgs)
    }
}

/**
 * Open handle for streaming a file into Downloads/CashFigure/<subFolder>/.
 *
 * @param outputStream destination stream (commit happens on close via `.use { }`)
 * @param uri          MediaStore content:// URI (API 29+) or file:// URI (≤ 28)
 * @param file         physical File on API ≤ 28 only; null on API 29+ (MediaStore-backed)
 */
class OpenFileHandle(
    val outputStream: OutputStream,
    val uri: Uri,
    val file: File?,
    private val resolver: ContentResolver
) {
    private var rolledBack = false

    /**
     * Deletes the partial file / MediaStore entry. Call on download failure.
     * Never throws.
     */
    fun rollback() {
        if (rolledBack) return
        rolledBack = true
        try {
            if (file != null) {
                file.delete()
            } else {
                resolver.delete(uri, null, null)
            }
        } catch (_: Exception) {
        }
    }
}
