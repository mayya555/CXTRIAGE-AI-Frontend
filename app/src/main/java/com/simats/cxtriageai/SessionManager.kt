package com.simats.cxtriageai

import android.content.Context
import java.io.File

object SessionManager {

    /**
     * Clears all local user data including SharedPreferences and local files.
     * Call this during logout, successful login, or account creation.
     */
    fun clearUserData(context: Context) {
        // 1. Clear all SharedPreferences files
        try {
            val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            if (prefsDir.exists() && prefsDir.isDirectory) {
                prefsDir.listFiles()?.forEach { file ->
                    val fileName = file.name.removeSuffix(".xml")
                    context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
                        .edit()
                        .clear()
                        .commit() // Use commit for synchronous wipe
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Clear all identifiable storage directories
        val dirsToClear = listOf(
            context.cacheDir,
            context.codeCacheDir,
            context.filesDir,
            context.noBackupFilesDir,
            File(context.applicationInfo.dataDir, "databases")
        )

        dirsToClear.forEach { dir ->
            try {
                if (dir != null && dir.exists()) {
                    dir.deleteRecursively()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
