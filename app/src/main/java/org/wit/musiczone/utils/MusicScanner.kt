package org.wit.musiczone.utils

import android.content.ContentResolver
import android.provider.MediaStore
import timber.log.Timber
import org.wit.musiczone.models.Song

/**
 * Music Scanner
 * Scans MP3 music files on the device
 */
class MusicScanner(private val contentResolver: ContentResolver) {

    /**
     * Scan music files on the device
     * @return List of songs
     */
    fun scanMusicFiles(): List<Song> {
        val songs = mutableListOf<Song>()
        
        Timber.d("Starting music scan...")

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn) ?: "Unknown"
                val artist = it.getString(artistColumn) ?: "Unknown Artist"
                val album = it.getString(albumColumn) ?: "Unknown Album"
                val duration = it.getLong(durationColumn)
                val filePath = it.getString(dataColumn) ?: ""
                val fileSize = it.getLong(sizeColumn)
                val dateAdded = it.getLong(dateAddedColumn)
                val albumId = it.getLong(albumIdColumn)

                // Get album art path
                val albumArtPath = getAlbumArtPath(albumId)

                if (filePath.isNotEmpty()) {
                    val song = Song(
                        id = 0, // Database ID, will be auto-generated on insert
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        filePath = filePath,
                        fileSize = fileSize,
                        dateAdded = dateAdded * 1000, // Convert to milliseconds
                        albumArtPath = albumArtPath
                    )
                    songs.add(song)
                }
            }
        }

        Timber.d("Music scan completed. Found ${songs.size} songs")
        return songs
    }

    /**
     * Get album art path
     * @param albumId Album ID
     * @return Album art path
     */
    private fun getAlbumArtPath(albumId: Long): String {
        if (albumId <= 0) return ""

        val albumArtUri = android.content.ContentUris.withAppendedId(
            android.net.Uri.parse("content://media/external/audio/albumart"),
            albumId
        )

        return albumArtUri.toString()
    }
}

