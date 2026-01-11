package org.wit.musiczone.database

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import timber.log.Timber
import org.wit.musiczone.models.Song

/**
 * Music Database Helper Class
 * Manages SQLite database creation, upgrade, and data operations
 */
class MusicDBHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        // Database information
        private const val DATABASE_NAME = "music_app.db"
        private const val DATABASE_VERSION = 4 // Current version: 4 (supports feedback feature)

        // SharedPreferences keys
        private const val PREFS_NAME = "music_app_prefs"
        private const val KEY_CURRENT_USER_ID = "current_user_id"

        // Songs table
        const val TABLE_SONGS = "songs"
        const val COL_ID = "_id"
        const val COL_TITLE = "title"
        const val COL_ARTIST = "artist"
        const val COL_ALBUM = "album"
        const val COL_DURATION = "duration"
        const val COL_FILE_PATH = "file_path"
        const val COL_FILE_SIZE = "file_size"
        const val COL_DATE_ADDED = "date_added"
        const val COL_ALBUM_ART_PATH = "album_art_path"

        // Favorites table (Phase 3)
        const val TABLE_FAVORITES = "favorites"
        const val COL_FAVORITE_ID = "_id"
        const val COL_FAVORITE_SONG_ID = "song_id"
        const val COL_FAVORITE_USER_ID = "user_id" // Added in Phase 4
        const val COL_FAVORITE_DATE_ADDED = "date_added"

        // Users table (Phase 4)
        const val TABLE_USERS = "users"
        const val COL_USER_ID = "_id"
        const val COL_USERNAME = "username"
        const val COL_PASSWORD = "password"
        const val COL_EMAIL = "email"
        const val COL_CREATED_AT = "created_at"

        // Feedback table (Version 4)
        const val TABLE_FEEDBACK = "feedback"
        const val COL_FEEDBACK_ID = "_id"
        const val COL_FEEDBACK_CONTENT = "content"
        const val COL_FEEDBACK_DATE_ADDED = "date_added"

        // SQL statement to create songs table
        private const val CREATE_TABLE_SONGS = """
            CREATE TABLE $TABLE_SONGS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_ARTIST TEXT,
                $COL_ALBUM TEXT,
                $COL_DURATION INTEGER,
                $COL_FILE_PATH TEXT UNIQUE NOT NULL,
                $COL_FILE_SIZE INTEGER,
                $COL_DATE_ADDED INTEGER,
                $COL_ALBUM_ART_PATH TEXT
            )
        """

        // SQL to create favorites table (Version 2 - without user ID)
        private const val CREATE_TABLE_FAVORITES_V2 = """
            CREATE TABLE $TABLE_FAVORITES (
                $COL_FAVORITE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FAVORITE_SONG_ID INTEGER NOT NULL,
                $COL_FAVORITE_DATE_ADDED INTEGER
            )
        """

        // SQL to create favorites table (Version 3 - with user ID)
        private const val CREATE_TABLE_FAVORITES_V3 = """
            CREATE TABLE $TABLE_FAVORITES (
                $COL_FAVORITE_USER_ID INTEGER NOT NULL,
                $COL_FAVORITE_SONG_ID INTEGER NOT NULL,
                $COL_FAVORITE_DATE_ADDED INTEGER,
                PRIMARY KEY ($COL_FAVORITE_USER_ID, $COL_FAVORITE_SONG_ID),
                FOREIGN KEY ($COL_FAVORITE_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID),
                FOREIGN KEY ($COL_FAVORITE_SONG_ID) REFERENCES $TABLE_SONGS($COL_ID)
            )
        """

        // SQL to create users table (Version 3)
        private const val CREATE_TABLE_USERS = """
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME TEXT UNIQUE NOT NULL,
                $COL_PASSWORD TEXT NOT NULL,
                $COL_EMAIL TEXT,
                $COL_CREATED_AT INTEGER
            )
        """

        // SQL to create feedback table (Version 4)
        private const val CREATE_TABLE_FEEDBACK = """
            CREATE TABLE $TABLE_FEEDBACK (
                $COL_FEEDBACK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FEEDBACK_CONTENT TEXT NOT NULL,
                $COL_FEEDBACK_DATE_ADDED INTEGER
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        Timber.d("Creating database tables...")
        db.execSQL(CREATE_TABLE_SONGS)
        db.execSQL(CREATE_TABLE_USERS)
        db.execSQL(CREATE_TABLE_FAVORITES_V3)
        db.execSQL(CREATE_TABLE_FEEDBACK)
        Timber.d("Database tables created successfully")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Timber.d("Upgrading database from version $oldVersion to $newVersion")
        
        var currentVersion = oldVersion
        when (currentVersion) {
            1 -> {
                // Upgrade from version 1 to 2: Add favorites table
                Timber.d("Upgrading from version 1 to 2: Adding favorites table")
                db.execSQL(CREATE_TABLE_FAVORITES_V2)
                currentVersion = 2 // Continue upgrading to version 3
            }
            2 -> {
                // Upgrade from version 2 to 3: Add users table, modify favorites table
                Timber.d("Upgrading from version 2 to 3: Adding users table and updating favorites table")
                upgradeToVersion3(db)
                currentVersion = 3
            }
            3 -> {
                // Upgrade from version 3 to 4: Add feedback table
                Timber.d("Upgrading from version 3 to 4: Adding feedback table")
                db.execSQL(CREATE_TABLE_FEEDBACK)
                currentVersion = 4
            }
        }
        
        if (oldVersion < newVersion) {
            // If there are other versions to upgrade, continue processing
            Timber.d("Database upgrade completed to version $newVersion")
        }
    }

    /**
     * Upgrade to version 3: Add users table, modify favorites table
     */
    private fun upgradeToVersion3(db: SQLiteDatabase) {
        // 1. Create users table
        db.execSQL(CREATE_TABLE_USERS)
        Timber.d("Users table created")

        // 2. Backup old favorites data
        val backupData = mutableListOf<Pair<Long, Long>>() // (song_id, date_added)
        val cursor = db.query(
            TABLE_FAVORITES,
            arrayOf(COL_FAVORITE_SONG_ID, COL_FAVORITE_DATE_ADDED),
            null,
            null,
            null,
            null,
            null
        )
        cursor?.use {
            while (it.moveToNext()) {
                val songId = it.getLong(it.getColumnIndexOrThrow(COL_FAVORITE_SONG_ID))
                val dateAdded = it.getLong(it.getColumnIndexOrThrow(COL_FAVORITE_DATE_ADDED))
                backupData.add(Pair(songId, dateAdded))
            }
        }
        Timber.d("Backed up ${backupData.size} favorite records")

        // 3. Drop old favorites table
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        Timber.d("Old favorites table dropped")

        // 4. Create new favorites table (with user ID)
        db.execSQL(CREATE_TABLE_FAVORITES_V3)
        Timber.d("New favorites table created")

        // 5. Create default user (if there is favorites data)
        if (backupData.isNotEmpty()) {
            val defaultUserId = createDefaultUser(db)
            if (defaultUserId > 0) {
                // Restore favorites data to default user
                backupData.forEach { (songId, dateAdded) ->
                    val values = ContentValues().apply {
                        put(COL_FAVORITE_USER_ID, defaultUserId)
                        put(COL_FAVORITE_SONG_ID, songId)
                        put(COL_FAVORITE_DATE_ADDED, dateAdded)
                    }
                    db.insert(TABLE_FAVORITES, null, values)
                }
                Timber.d("Restored ${backupData.size} favorite records to default user")
            }
        }
    }

    /**
     * Create default user (for data migration)
     */
    private fun createDefaultUser(db: SQLiteDatabase): Long {
        val values = ContentValues().apply {
            put(COL_USERNAME, "default_user")
            put(COL_PASSWORD, "default") // Temporary password
            put(COL_EMAIL, "")
            put(COL_CREATED_AT, System.currentTimeMillis())
        }
        return db.insert(TABLE_USERS, null, values)
    }

    /**
     * Insert a song
     * @param song Song object
     * @return ID of newly inserted row, -1 on failure
     */
    fun insertSong(song: Song): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, song.title)
            put(COL_ARTIST, song.artist)
            put(COL_ALBUM, song.album)
            put(COL_DURATION, song.duration)
            put(COL_FILE_PATH, song.filePath)
            put(COL_FILE_SIZE, song.fileSize)
            put(COL_DATE_ADDED, song.dateAdded)
            put(COL_ALBUM_ART_PATH, song.albumArtPath)
        }

        return try {
            val id = db.insertWithOnConflict(
                TABLE_SONGS,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )
            if (id != -1L) {
                Timber.d("Song inserted: ${song.title} (ID: $id)")
            } else {
                Timber.w("Failed to insert song: ${song.title} (may already exist)")
            }
            id
        } catch (e: Exception) {
            Timber.e(e, "Error inserting song: ${song.title}")
            -1L
        }
    }

    /**
     * Get all songs
     * @return List of songs
     */
    fun getAllSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SONGS,
            null,
            null,
            null,
            null,
            null,
            "$COL_TITLE ASC" // Sort by title ascending
        )

        cursor?.use {
            while (it.moveToNext()) {
                songs.add(Song.fromCursor(it))
            }
        }

        Timber.d("Retrieved ${songs.size} songs from database")
        return songs
    }

    /**
     * Clear all songs
     * @return Number of deleted rows
     */
    fun clearAllSongs(): Int {
        val db = writableDatabase
        val count = db.delete(TABLE_SONGS, null, null)
        Timber.d("Cleared $count songs from database")
        return count
    }

    /**
     * Remove duplicate songs (based on title and artist combination)
     * Keep the record with the smallest _id, delete other duplicate records
     * @return Number of duplicate records deleted
     */
    fun removeDuplicateSongs(): Int {
        val db = writableDatabase
        var deletedCount = 0

        try {
            // Method 1: Use temporary table approach (more reliable)
            // Create temporary table to store records to keep (minimum _id for each title+artist combination)
            db.execSQL("""
                CREATE TEMP TABLE IF NOT EXISTS temp_keep_songs AS
                SELECT MIN($COL_ID) AS $COL_ID
                FROM $TABLE_SONGS
                GROUP BY $COL_TITLE, $COL_ARTIST
            """.trimIndent())

            // Delete records not in temporary table (i.e., duplicate records)
            val deleteSql = """
                DELETE FROM $TABLE_SONGS
                WHERE $COL_ID NOT IN (SELECT $COL_ID FROM temp_keep_songs)
            """.trimIndent()

            db.execSQL(deleteSql)

            // Get number of deleted rows
            val cursor = db.rawQuery("SELECT changes()", null)
            cursor?.use {
                if (it.moveToFirst()) {
                    deletedCount = it.getInt(0)
                }
            }

            // Drop temporary table
            db.execSQL("DROP TABLE IF EXISTS temp_keep_songs")

            Timber.d("Removed $deletedCount duplicate songs from database")
        } catch (e: Exception) {
            Timber.e(e, "Error removing duplicate songs")
            // If error occurs, try to clean up temporary table
            try {
                db.execSQL("DROP TABLE IF EXISTS temp_keep_songs")
            } catch (e2: Exception) {
                Timber.e(e2, "Error cleaning up temp table")
            }
        }

        return deletedCount
    }

    // ==================== Phase 3: Favorites Feature ====================

    /**
     * Add to favorites
     * @param songId Song ID
     * @return Whether successful
     */
    fun addToFavorite(songId: Long): Boolean {
        val userId = getCurrentUserId()
        if (userId <= 0) {
            Timber.w("Cannot add favorite: No user logged in")
            return false
        }

        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_FAVORITE_USER_ID, userId)
            put(COL_FAVORITE_SONG_ID, songId)
            put(COL_FAVORITE_DATE_ADDED, System.currentTimeMillis())
        }

        return try {
            val id = db.insertWithOnConflict(
                TABLE_FAVORITES,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )
            if (id != -1L) {
                Timber.d("Added to favorites: songId=$songId, userId=$userId")
                true
            } else {
                Timber.d("Song already in favorites: songId=$songId")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding to favorites: songId=$songId")
            false
        }
    }

    /**
     * Remove from favorites
     * @param songId Song ID
     * @return Number of deleted rows
     */
    fun removeFromFavorite(songId: Long): Int {
        val userId = getCurrentUserId()
        if (userId <= 0) {
            Timber.w("Cannot remove favorite: No user logged in")
            return 0
        }

        val db = writableDatabase
        val count = db.delete(
            TABLE_FAVORITES,
            "$COL_FAVORITE_USER_ID = ? AND $COL_FAVORITE_SONG_ID = ?",
            arrayOf(userId.toString(), songId.toString())
        )
        Timber.d("Removed from favorites: songId=$songId, count=$count")
        return count
    }

    /**
     * Check if favorited
     * @param songId Song ID
     * @return Whether favorited
     */
    fun isFavorite(songId: Long): Boolean {
        val userId = getCurrentUserId()
        if (userId <= 0) {
            return false
        }

        val db = readableDatabase
        val cursor = db.query(
            TABLE_FAVORITES,
            arrayOf(COL_FAVORITE_SONG_ID),
            "$COL_FAVORITE_USER_ID = ? AND $COL_FAVORITE_SONG_ID = ?",
            arrayOf(userId.toString(), songId.toString()),
            null,
            null,
            null,
            "1"
        )

        val isFav = cursor?.count ?: 0 > 0
        cursor?.close()
        return isFav
    }

    /**
     * Get all favorite songs
     * @return List of favorite songs
     */
    fun getAllFavorites(): List<Song> {
        val userId = getCurrentUserId()
        if (userId <= 0) {
            Timber.w("Cannot get favorites: No user logged in")
            return emptyList()
        }

        val songs = mutableListOf<Song>()
        val db = readableDatabase
        
        // Use JOIN to query favorite songs
        val query = """
            SELECT s.* FROM $TABLE_SONGS s
            INNER JOIN $TABLE_FAVORITES f ON s.$COL_ID = f.$COL_FAVORITE_SONG_ID
            WHERE f.$COL_FAVORITE_USER_ID = ?
            ORDER BY f.$COL_FAVORITE_DATE_ADDED DESC
        """
        
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        cursor?.use {
            while (it.moveToNext()) {
                songs.add(Song.fromCursor(it))
            }
        }

        Timber.d("Retrieved ${songs.size} favorite songs for user $userId")
        return songs
    }

    // ==================== Phase 3: Search Feature ====================

    /**
     * Search songs (supports searching title, artist, and album name)
     * @param keyword Search keyword (case-insensitive)
     * @return List of matching songs
     */
    fun searchSongs(keyword: String): List<Song> {
        if (keyword.isBlank()) {
            return getAllSongs()
        }

        val songs = mutableListOf<Song>()
        val db = readableDatabase
        val searchPattern = "%$keyword%"
        
        // Search song title, artist, and album name (case-insensitive)
        // SQLite LIKE is case-insensitive by default (depends on collation), but use LOWER function to ensure
        val cursor = db.rawQuery(
            """
            SELECT * FROM $TABLE_SONGS 
            WHERE LOWER($COL_TITLE) LIKE LOWER(?) 
               OR LOWER($COL_ARTIST) LIKE LOWER(?) 
               OR LOWER($COL_ALBUM) LIKE LOWER(?)
            ORDER BY $COL_TITLE ASC
            """.trimIndent(),
            arrayOf(searchPattern, searchPattern, searchPattern)
        )

        cursor?.use {
            while (it.moveToNext()) {
                songs.add(Song.fromCursor(it))
            }
        }

        Timber.d("Search '$keyword' found ${songs.size} songs")
        return songs
    }

    /**
     * Get all unique artist list
     * @param keyword Optional keyword for searching artists (case-insensitive)
     * @return List of artist names
     */
    fun getAllArtists(keyword: String = ""): List<String> {
        val artists = mutableSetOf<String>()
        val db = readableDatabase
        
        val cursor = if (keyword.isNotBlank()) {
            val searchPattern = "%$keyword%"
            db.rawQuery(
                """
                SELECT DISTINCT $COL_ARTIST FROM $TABLE_SONGS 
                WHERE LOWER($COL_ARTIST) LIKE LOWER(?) 
                  AND $COL_ARTIST IS NOT NULL 
                  AND $COL_ARTIST != ''
                ORDER BY $COL_ARTIST ASC
                """.trimIndent(),
                arrayOf(searchPattern)
            )
        } else {
            db.rawQuery(
                """
                SELECT DISTINCT $COL_ARTIST FROM $TABLE_SONGS 
                WHERE $COL_ARTIST IS NOT NULL 
                  AND $COL_ARTIST != ''
                ORDER BY $COL_ARTIST ASC
                """.trimIndent(),
                null
            )
        }

        cursor?.use {
            while (it.moveToNext()) {
                val artist = it.getString(it.getColumnIndexOrThrow(COL_ARTIST))
                if (artist.isNotBlank()) {
                    artists.add(artist)
                }
            }
        }

        Timber.d("Found ${artists.size} artists (keyword: '$keyword')")
        return artists.sorted()
    }

    /**
     * Get all unique album list
     * @param keyword Optional keyword for searching albums (case-insensitive)
     * @return List of album name and artist name pairs
     */
    fun getAllAlbums(keyword: String = ""): List<Pair<String, String>> {
        val albums = mutableSetOf<Pair<String, String>>()
        val db = readableDatabase
        
        val cursor = if (keyword.isNotBlank()) {
            val searchPattern = "%$keyword%"
            db.rawQuery(
                """
                SELECT DISTINCT $COL_ALBUM, $COL_ARTIST FROM $TABLE_SONGS 
                WHERE (LOWER($COL_ALBUM) LIKE LOWER(?) OR LOWER($COL_ARTIST) LIKE LOWER(?)) 
                  AND $COL_ALBUM IS NOT NULL 
                  AND $COL_ALBUM != ''
                ORDER BY $COL_ALBUM ASC
                """.trimIndent(),
                arrayOf(searchPattern, searchPattern)
            )
        } else {
            db.rawQuery(
                """
                SELECT DISTINCT $COL_ALBUM, $COL_ARTIST FROM $TABLE_SONGS 
                WHERE $COL_ALBUM IS NOT NULL 
                  AND $COL_ALBUM != ''
                ORDER BY $COL_ALBUM ASC
                """.trimIndent(),
                null
            )
        }

        cursor?.use {
            while (it.moveToNext()) {
                val album = it.getString(it.getColumnIndexOrThrow(COL_ALBUM))
                val artist = it.getString(it.getColumnIndexOrThrow(COL_ARTIST)) ?: ""
                if (album.isNotBlank()) {
                    albums.add(Pair(album, artist))
                }
            }
        }

        Timber.d("Found ${albums.size} albums (keyword: '$keyword')")
        return albums.sortedBy { it.first }
    }

    // ==================== Phase 4: User Feature ====================

    /**
     * User registration
     * @param username Username
     * @param password Password
     * @return Whether successful
     */
    fun registerUser(username: String, password: String): Boolean {
        if (username.isBlank() || password.isBlank()) {
            Timber.w("Registration failed: Username or password is empty")
            return false
        }

        if (checkUsernameExists(username)) {
            Timber.w("Registration failed: Username already exists")
            return false
        }

        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USERNAME, username)
            put(COL_PASSWORD, password) // Note: Should be encrypted in production
            put(COL_EMAIL, "")
            put(COL_CREATED_AT, System.currentTimeMillis())
        }

        return try {
            val id = db.insert(TABLE_USERS, null, values)
            if (id != -1L) {
                Timber.d("User registered: username=$username, id=$id")
                true
            } else {
                Timber.w("Registration failed: Insert returned -1")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error registering user: username=$username")
            false
        }
    }

    /**
     * User login
     * @param username Username
     * @param password Password
     * @return User ID, -1 on failure
     */
    fun loginUser(username: String, password: String): Long {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_USER_ID, COL_PASSWORD),
            "$COL_USERNAME = ?",
            arrayOf(username),
            null,
            null,
            null
        )

        var userId = -1L
        cursor?.use {
            if (it.moveToFirst()) {
                val storedPassword = it.getString(it.getColumnIndexOrThrow(COL_PASSWORD))
                if (storedPassword == password) {
                    userId = it.getLong(it.getColumnIndexOrThrow(COL_USER_ID))
                    // Save current user ID
                    setCurrentUserId(userId)
                    Timber.d("User logged in: username=$username, id=$userId")
                } else {
                    Timber.w("Login failed: Incorrect password for username=$username")
                }
            } else {
                Timber.w("Login failed: User not found: username=$username")
            }
        }

        return userId
    }

    /**
     * Check if username exists
     * @param username Username
     * @return Whether exists
     */
    fun checkUsernameExists(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_USER_ID),
            "$COL_USERNAME = ?",
            arrayOf(username),
            null,
            null,
            null,
            "1"
        )

        val exists = cursor?.count ?: 0 > 0
        cursor?.close()
        return exists
    }

    /**
     * Get current logged-in user ID
     * @return User ID, -1 if not logged in
     */
    fun getCurrentUserId(): Long {
        return sharedPreferences.getLong(KEY_CURRENT_USER_ID, -1L)
    }

    /**
     * Set current logged-in user ID
     * @param userId User ID
     */
    private fun setCurrentUserId(userId: Long) {
        sharedPreferences.edit()
            .putLong(KEY_CURRENT_USER_ID, userId)
            .apply()
        Timber.d("Current user ID set to: $userId")
    }

    /**
     * Logout
     */
    fun logout() {
        sharedPreferences.edit()
            .remove(KEY_CURRENT_USER_ID)
            .apply()
        Timber.d("User logged out")
    }

    // ==================== Version 4: Feedback Feature ====================

    /**
     * Insert feedback
     * @param content Feedback content
     * @return Whether successful
     */
    fun insertFeedback(content: String): Boolean {
        if (content.isBlank()) {
            Timber.w("Feedback insertion failed: Content is empty")
            return false
        }

        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_FEEDBACK_CONTENT, content)
            put(COL_FEEDBACK_DATE_ADDED, System.currentTimeMillis())
        }

        return try {
            val id = db.insert(TABLE_FEEDBACK, null, values)
            if (id != -1L) {
                Timber.d("Feedback inserted: id=$id")
                true
            } else {
                Timber.w("Feedback insertion failed: Insert returned -1")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error inserting feedback")
            false
        }
    }
}

