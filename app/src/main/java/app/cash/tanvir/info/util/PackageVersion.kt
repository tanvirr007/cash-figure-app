package app.cash.tanvir.info.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Reads the installed app version pair (name, code) with the API-33+
 * [PackageManager.PackageInfoFlags] / API-28+ [android.content.pm.PackageInfo.longVersionCode]
 * branches. Returns (name, code) = ("1.0.0", 1) on any failure.
 */
fun getInstalledVersion(context: Context): Pair<String, Long> {
    return try {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        val vName = packageInfo.versionName ?: "1.0.0"
        val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
        Pair(vName, vCode)
    } catch (e: Exception) {
        Pair("1.0.0", 1L)
    }
}

/**
 * Returns the timestamp (epoch millis) of the last app update/install
 * ([android.content.pm.PackageInfo.lastUpdateTime]), or 0L on failure.
 */
fun getInstalledUpdatedAt(context: Context): Long {
    return try {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        packageInfo.lastUpdateTime
    } catch (e: Exception) {
        0L
    }
}
