package by.bsu.newsly.domain.repository

import android.content.Context
import android.text.format.Formatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SettingsRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    fun getAdBlockingEnabled(): Boolean =
        prefs.getBoolean("ad_blocking_enabled", true)

    fun setAdBlockingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("ad_blocking_enabled", enabled).apply()
    }

    fun getFormattedCacheSize(): String {
        val size = calcDirSize(context.cacheDir)
        return Formatter.formatShortFileSize(context, size)
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        deleteDir(context.cacheDir)
    }

    private fun calcDirSize(dir: File?): Long {
        if (dir == null) return 0
        if (dir.isFile) return dir.length()
        var total = 0L
        dir.listFiles()?.forEach { total += calcDirSize(it) }
        return total
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir == null) return false
        if (dir.isDirectory) dir.listFiles()?.forEach { deleteDir(it) }
        return dir.delete()
    }
}