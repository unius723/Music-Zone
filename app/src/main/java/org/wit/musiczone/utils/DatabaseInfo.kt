package org.wit.musiczone.utils

import android.content.Context
import timber.log.Timber
import org.wit.musiczone.database.MusicDBHelper

/**
 * Database Information Utility Class
 * Used to view database location and status
 */
object DatabaseInfo {

    /**
     * Get database file path
     * @param context Context
     * @return Database file path
     */
    fun getDatabasePath(context: Context): String {
        val dbPath = context.getDatabasePath("music_app.db").absolutePath
        Timber.d("Database file path: $dbPath")
        return dbPath
    }

    /**
     * Print database information (for debugging)
     * @param context Context
     */
    fun printDatabaseInfo(context: Context) {
        val dbPath = getDatabasePath(context)
        val packageName = context.packageName
        
        Timber.d("========== Database Information ==========")
        Timber.d("Package name: $packageName")
        Timber.d("Database name: music_app.db")
        Timber.d("Database path: $dbPath")
        Timber.d("Standard path: /data/data/$packageName/databases/music_app.db")
        Timber.d("==========================================")
        
        // Check if database exists
        val dbFile = context.getDatabasePath("music_app.db")
        if (dbFile.exists()) {
            Timber.d("Database file exists, size: ${dbFile.length()} bytes")
        } else {
            Timber.d("Database file does not exist (will be created automatically on first use)")
        }
    }

    /**
     * Get database statistics
     * @param context Context
     */
    fun getDatabaseStats(context: Context): Map<String, Int> {
        val dbHelper = MusicDBHelper(context)
        val songs = dbHelper.getAllSongs()
        val favorites = dbHelper.getAllFavorites()
        val currentUserId = dbHelper.getCurrentUserId()
        
        val stats = mapOf(
            "Total Songs" to songs.size,
            "Favorite Count" to favorites.size,
            "Current User ID" to currentUserId.toInt()
        )
        
        dbHelper.close()
        return stats
    }
}

