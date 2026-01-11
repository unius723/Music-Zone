package org.wit.musiczone.models

import android.database.Cursor

data class Song(
    val id: Long = 0, // Database primary key _id
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val duration: Long = 0, // Duration (milliseconds)
    val filePath: String = "",
    val fileSize: Long = 0, // File size (bytes)
    val dateAdded: Long = 0, // Added time (timestamp)
    val albumArtPath: String = "" // Album art path
) {
    companion object {
        /**
         * Create Song object from Cursor
         */
        fun fromCursor(cursor: Cursor): Song {
            val idIndex = cursor.getColumnIndex(COL_ID)
            val titleIndex = cursor.getColumnIndex(COL_TITLE)
            val artistIndex = cursor.getColumnIndex(COL_ARTIST)
            val albumIndex = cursor.getColumnIndex(COL_ALBUM)
            val durationIndex = cursor.getColumnIndex(COL_DURATION)
            val filePathIndex = cursor.getColumnIndex(COL_FILE_PATH)
            val fileSizeIndex = cursor.getColumnIndex(COL_FILE_SIZE)
            val dateAddedIndex = cursor.getColumnIndex(COL_DATE_ADDED)
            val albumArtPathIndex = cursor.getColumnIndex(COL_ALBUM_ART_PATH)

            return Song(
                id = if (idIndex >= 0) cursor.getLong(idIndex) else 0,
                title = if (titleIndex >= 0) cursor.getString(titleIndex) ?: "" else "",
                artist = if (artistIndex >= 0) cursor.getString(artistIndex) ?: "" else "",
                album = if (albumIndex >= 0) cursor.getString(albumIndex) ?: "" else "",
                duration = if (durationIndex >= 0) cursor.getLong(durationIndex) else 0,
                filePath = if (filePathIndex >= 0) cursor.getString(filePathIndex) ?: "" else "",
                fileSize = if (fileSizeIndex >= 0) cursor.getLong(fileSizeIndex) else 0,
                dateAdded = if (dateAddedIndex >= 0) cursor.getLong(dateAddedIndex) else 0,
                albumArtPath = if (albumArtPathIndex >= 0) cursor.getString(albumArtPathIndex) ?: "" else ""
            )
        }

        // Column name constants (defined in MusicDBHelper)
        const val COL_ID = "_id"
        const val COL_TITLE = "title"
        const val COL_ARTIST = "artist"
        const val COL_ALBUM = "album"
        const val COL_DURATION = "duration"
        const val COL_FILE_PATH = "file_path"
        const val COL_FILE_SIZE = "file_size"
        const val COL_DATE_ADDED = "date_added"
        const val COL_ALBUM_ART_PATH = "album_art_path"
    }
}

