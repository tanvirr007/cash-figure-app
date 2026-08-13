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
     * Deletes a previously saved report file (e.g. a legacy OTA APK) from
     * `Download/CashFigure/<subFolder>/`. Safe to call when nothing exists.
     */
    fun deleteReportFile(context: Context, fileName: String, subFolder: String = ""): Boolean {
        val relativeSubPath = if (subFolder.isBlank()) "CashFigure" else "CashFigure/$subFolder"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                deleteMatchingEntries(context.contentResolver, fileName, relativeSubPath) > 0
            } catch (_: Exception) {
                false
            }
        } else {
            try {
                val targetFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "$relativeSubPath/$fileName"
                )
                targetFile.exists() && targetFile.delete()
            } catch (_: Exception) {
                false
            }
        }
    }

    private fun deleteMatchingEntries(resolver: ContentResolver, fileName: String, relativeSubPath: String): Int {
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
        val selectionArgs = arrayOf(fileName, Environment.DIRECTORY_DOWNLOADS + "/" + relativeSubPath)
        return resolver.delete(MediaStore.Downloads.EXTERNAL_CONTENT_URI, selection, selectionArgs)
    }
}

