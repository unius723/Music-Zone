package org.wit.musiczone.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import org.wit.musiczone.R
import org.wit.musiczone.adapters.SongAdapter
import org.wit.musiczone.database.MusicDBHelper
import org.wit.musiczone.models.Song
import org.wit.musiczone.utils.MusicScanner

/**
 * Music List Activity
 * Displays all scanned music files
 */
class MusicListActivity : AppCompatActivity() {

    private lateinit var dbHelper: MusicDBHelper
    private lateinit var musicScanner: MusicScanner
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private val songs = mutableListOf<Song>()

    // Permission request
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Timber.d("Storage permission granted")
            scanAndDisplayMusic()
        } else {
            Timber.w("Storage permission denied")
            Toast.makeText(
                this,
                getString(R.string.storage_permission_required),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_list)

        Timber.d("MusicListActivity created")

        // Initialize database
        dbHelper = MusicDBHelper(this)
        musicScanner = MusicScanner(contentResolver)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewSongs)
        recyclerView.layoutManager = LinearLayoutManager(this)
        songAdapter = SongAdapter(songs) { song ->
            // Handle song click (playback functionality to be implemented in later stages)
            Timber.d("Song clicked: ${song.title}")
            Toast.makeText(this, getString(R.string.clicked_song, song.title), Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = songAdapter

        // Check permission and scan music
        checkPermissionAndScan()
    }

    /**
     * Check permission and scan music
     */
    private fun checkPermissionAndScan() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission granted
                scanAndDisplayMusic()
            }
            else -> {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    /**
     * Scan music and display
     */
    private fun scanAndDisplayMusic() {
        Timber.d("Starting music scan and display...")

        // Execute scan in background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Scan music files
                val scannedSongs = musicScanner.scanMusicFiles()
                Timber.d("Scanned ${scannedSongs.size} songs")

                // Clear database
                dbHelper.clearAllSongs()

                // Save to database
                var successCount = 0
                scannedSongs.forEach { song ->
                    val id = dbHelper.insertSong(song)
                    if (id != -1L) {
                        successCount++
                    }
                }
                Timber.d("Inserted $successCount songs into database")

                // Read from database (ensure data consistency)
                val dbSongs = dbHelper.getAllSongs()

                // Switch to main thread to update UI
                withContext(Dispatchers.Main) {
                    songs.clear()
                    songs.addAll(dbSongs)
                    songAdapter.notifyDataSetChanged()

                    Toast.makeText(
                        this@MusicListActivity,
                        getString(R.string.scan_complete, songs.size),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error scanning music")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MusicListActivity,
                        getString(R.string.error_scanning_music, e.message ?: ""),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
        Timber.d("MusicListActivity destroyed")
    }
}

