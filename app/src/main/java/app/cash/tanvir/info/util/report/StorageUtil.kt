package app.cash.tanvir.info.util.report

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
     * Save report data (byte array) to `Download/CashFigure/`.
     * Automatically handles duplicate filenames by appending a suffix if needed.
     */
    fun saveReportFile(
        context: Context,
        fileName: String,
        mimeType: String,
        data: ByteArray
    ): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CashFigure")
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
                "CashFigure"
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
}
