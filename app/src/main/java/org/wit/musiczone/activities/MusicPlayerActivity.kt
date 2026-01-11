package org.wit.musiczone.activities

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.TextView
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
import org.wit.musiczone.adapters.PlaylistAdapter
import org.wit.musiczone.database.MusicDBHelper
import org.wit.musiczone.models.Song
import org.wit.musiczone.utils.MusicScanner
import java.io.IOException

/**
 * Music Player Activity
 * Combines music list and playback functionality
 * - Scans music files from SD card
 * - Saves to database
 * - Displays music list
 * - Play, pause, switch music
 */
class MusicPlayerActivity : AppCompatActivity() {

    // Database and scanner
    private lateinit var dbHelper: MusicDBHelper
    private lateinit var musicScanner: MusicScanner

    // UI components
    private lateinit var playlistRecyclerView: RecyclerView
    private lateinit var adapter: PlaylistAdapter
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnSkipPrevious: ImageButton
    private lateinit var btnSkipNext: ImageButton
    private lateinit var btnMenu: ImageButton
    private lateinit var progressBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvTimeDisplay: TextView
    private lateinit var tvPlaylistTitle: TextView
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnBack: ImageView
    private lateinit var btnListening: ImageView

    // Data
    private val songs = mutableListOf<Song>()
    private var currentSongIndex = -1
    private var currentSong: Song? = null

    // Player
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var isPrepared = false

    // Progress update
    private val progressHandler = Handler(Looper.getMainLooper())
    private val progressUpdateRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            progressHandler.postDelayed(this, 100) // Update every 100ms
        }
    }

    // Permission request
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Timber.d("Storage permission granted")
            scanAndLoadMusic()
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
        setContentView(R.layout.activity_music_player)

        // Initialize database and scanner
        dbHelper = MusicDBHelper(this)
        musicScanner = MusicScanner(contentResolver)

        // Initialize UI
        initializeViews()
        setupPlaylist()
        setupPlaybackControls()
        setupMenu()
        setupRefreshButton()
        setupBackButton()
        setupListeningButton()

        // Check permission and load music
        checkPermissionAndLoadMusic()
    }

    /**
     * Initialize views
     */
    private fun initializeViews() {
        playlistRecyclerView = findViewById(R.id.playlist_recycler_view)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnSkipPrevious = findViewById(R.id.btn_skip_previous)
        btnSkipNext = findViewById(R.id.btn_skip_next)
        btnMenu = findViewById(R.id.btn_menu)
        progressBar = findViewById(R.id.progress_bar)
        tvCurrentTime = findViewById(R.id.tv_current_time)
        tvTotalTime = findViewById(R.id.tv_total_time)
        tvTimeDisplay = findViewById(R.id.tv_time_display)
        tvPlaylistTitle = findViewById(R.id.tv_playlist_title)
        btnRefresh = findViewById(R.id.btn_refresh)
        btnBack = findViewById(R.id.btn_back)
        btnListening = findViewById(R.id.btn_listening)
    }

    /**
     * Setup playlist
     */
    private fun setupPlaylist() {
        adapter = PlaylistAdapter(
            songs = songs,
            onPlayClick = { song ->
                playSong(song)
            },
            onMoreClick = { song ->
                // Show more options
                Toast.makeText(this, getString(R.string.more_options_song, song.title), Toast.LENGTH_SHORT).show()
            },
            onFavoriteClick = { song, isFavorite ->
                handleFavoriteClick(song, isFavorite)
            },
            isFavorite = { songId ->
                dbHelper.isFavorite(songId)
            }
        )
        playlistRecyclerView.layoutManager = LinearLayoutManager(this)
        playlistRecyclerView.adapter = adapter
    }
    
    /**
     * Handle favorite button click
     */
    private fun handleFavoriteClick(song: Song, isFavorite: Boolean) {
        if (isFavorite) {
            val success = dbHelper.addToFavorite(song.id)
            if (success) {
                Toast.makeText(this, "Added to favorites: ${song.title}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Already in favorites: ${song.title}", Toast.LENGTH_SHORT).show()
            }
        } else {
            val count = dbHelper.removeFromFavorite(song.id)
            if (count > 0) {
                Toast.makeText(this, "Removed from favorites: ${song.title}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show()
            }
        }
        // Refresh adapter to update UI
        adapter.notifyDataSetChanged()
    }

    /**
     * Setup playback controls
     */
    private fun setupPlaybackControls() {
        btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        btnSkipPrevious.setOnClickListener {
            playPreviousSong()
        }

        btnSkipNext.setOnClickListener {
            playNextSong()
        }

        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && isPrepared) {
                    mediaPlayer?.let { mp ->
                        val seekPosition = (progress * mp.duration) / 100
                        mp.seekTo(seekPosition)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Pause progress update
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Resume progress update
            }
        })
    }

    /**
     * Setup menu
     */
    private fun setupMenu() {
        btnMenu.setOnClickListener { view ->
            showMenu(view)
        }
    }

    /**
     * Setup refresh button
     */
    private fun setupRefreshButton() {
        btnRefresh.setOnClickListener {
            rescanMusic()
        }
    }

    /**
     * Setup back button
     */
    private fun setupBackButton() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    /**
     * Setup Listening button (locate to currently playing song)
     */
    private fun setupListeningButton() {
        btnListening.setOnClickListener {
            // Scroll to currently playing song position
            if (currentSongIndex >= 0 && currentSongIndex < songs.size) {
                playlistRecyclerView.smoothScrollToPosition(currentSongIndex)
                Toast.makeText(this, getString(R.string.located_current_song), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.no_song_playing), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Rescan music (force refresh)
     */
    private fun rescanMusic() {
        if (!hasPermission()) {
            Toast.makeText(this, getString(R.string.storage_permission_required), Toast.LENGTH_SHORT).show()
            checkPermissionAndLoadMusic()
            return
        }
        Toast.makeText(this, getString(R.string.rescanning_music), Toast.LENGTH_SHORT).show()
        scanAndLoadMusic(forceRescan = true)
    }

    /**
     * Check if storage permission is granted
     */
    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Show menu
     */
    private fun showMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_player, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_repeat -> {
                    Toast.makeText(this, getString(R.string.repeat_playback), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_shuffle -> {
                    Toast.makeText(this, getString(R.string.shuffle_playback), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_add_to_playlist -> {
                    Toast.makeText(this, getString(R.string.add_to_playlist), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_share -> {
                    Toast.makeText(this, getString(R.string.share), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_settings -> {
                    Toast.makeText(this, getString(R.string.settings), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_refresh -> {
                    rescanMusic()
                    true
                }
                R.id.menu_exit -> {
                    finish()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    /**
     * Check permission and load music
     */
    private fun checkPermissionAndLoadMusic() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission granted
                scanAndLoadMusic()
            }
            else -> {
                // Request permission (prefer READ_MEDIA_AUDIO for Android 13+)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    /**
     * Scan and load music
     * @param forceRescan Whether to force rescan (ignore database cache)
     */
    private fun scanAndLoadMusic(forceRescan: Boolean = false) {
        Timber.d("Starting music scan and load... (forceRescan=$forceRescan)")

        // Show loading message
        tvPlaylistTitle.text = getString(R.string.scanning_music)

        // Execute scan in background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var dbSongs: List<Song>

                // If force rescan or database is empty, rescan
                if (forceRescan || dbHelper.getAllSongs().isEmpty()) {
                    if (forceRescan) {
                        Timber.d("Force rescan: clearing database...")
                        dbHelper.clearAllSongs()
                    }
                    
                    Timber.d("Scanning music files...")
                    val scannedSongs = musicScanner.scanMusicFiles()
                    Timber.d("Scanned ${scannedSongs.size} songs")

                    // Save to database
                    scannedSongs.forEach { song ->
                        dbHelper.insertSong(song)
                    }

                    // Remove duplicate songs (based on title and artist)
                    val duplicateCount = dbHelper.removeDuplicateSongs()
                    if (duplicateCount > 0) {
                        Timber.d("Removed $duplicateCount duplicate songs")
                    }

                    // Re-read from database
                    dbSongs = dbHelper.getAllSongs()
                    Timber.d("Loaded ${dbSongs.size} songs from database")
                } else {
                    // Read from database
                    dbSongs = dbHelper.getAllSongs()
                    Timber.d("Loaded ${dbSongs.size} songs from database (cached)")
                }

                // Switch to main thread to update UI
                withContext(Dispatchers.Main) {
                    songs.clear()
                    songs.addAll(dbSongs)
                    adapter.notifyDataSetChanged()
                    tvPlaylistTitle.text = "Playing List (${songs.size})"

                    if (songs.isNotEmpty()) {
                        Toast.makeText(
                            this@MusicPlayerActivity,
                            getString(R.string.loading_complete, songs.size),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MusicPlayerActivity,
                            getString(R.string.no_music_found),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error scanning music")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MusicPlayerActivity,
                        getString(R.string.error_scanning_music, e.message ?: ""),
                        Toast.LENGTH_LONG
                    ).show()
                    tvPlaylistTitle.text = "Playing List (0)"
                }
            }
        }
    }

    /**
     * Play song
     */
    private fun playSong(song: Song) {
        try {
            // Stop current playback
            stopPlayback()

            currentSong = song
            currentSongIndex = songs.indexOf(song)
            adapter.currentPlayingIndex = currentSongIndex
            Timber.d("Playing song: ${song.title} at index $currentSongIndex")

            // Create MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(song.filePath)
                    prepareAsync()
                    
                    setOnPreparedListener { mp ->
                        this@MusicPlayerActivity.isPrepared = true
                        mp.start()
                        this@MusicPlayerActivity.isPlaying = true
                        this@MusicPlayerActivity.updatePlayPauseButton()
                        this@MusicPlayerActivity.updateTotalTime()
                        this@MusicPlayerActivity.startProgressUpdate()
                        this@MusicPlayerActivity.adapter.currentPlayingIndex = this@MusicPlayerActivity.currentSongIndex
                        Timber.d("MediaPlayer prepared and started")
                    }

                    setOnCompletionListener {
                        // Playback completed, play next
                        Timber.d("Song completed, playing next")
                        playNextSong()
                    }

                    setOnErrorListener { _, what, extra ->
                        Timber.e("MediaPlayer error: what=$what, extra=$extra")
                        Toast.makeText(
                            this@MusicPlayerActivity,
                            getString(R.string.playback_error, song.title),
                            Toast.LENGTH_SHORT
                        ).show()
                        isPrepared = false
                        false
                    }
                } catch (e: IOException) {
                    Timber.e(e, "Error setting data source: ${song.filePath}")
                    Toast.makeText(
                        this@MusicPlayerActivity,
                        getString(R.string.cannot_play, song.title),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error playing song")
            Toast.makeText(this, getString(R.string.playback_error, e.message ?: ""), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Toggle play/pause
     */
    private fun togglePlayPause() {
        if (songs.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_music_available), Toast.LENGTH_SHORT).show()
            return
        }

        if (currentSongIndex < 0 || currentSong == null) {
            // If no song is playing, play the first one
            playSong(songs[0])
            return
        }

        mediaPlayer?.let { mp ->
            if (isPrepared) {
                if (isPlaying) {
                    mp.pause()
                    isPlaying = false
                    stopProgressUpdate()
                } else {
                    mp.start()
                    isPlaying = true
                    startProgressUpdate()
                }
                updatePlayPauseButton()
            }
        } ?: run {
            // MediaPlayer not initialized, replay
            playSong(currentSong!!)
        }
    }

    /**
     * Play previous song
     */
    private fun playPreviousSong() {
        if (songs.isEmpty()) return

        val newIndex = if (currentSongIndex > 0) {
            currentSongIndex - 1
        } else {
            songs.size - 1 // Loop to last song
        }
        playSong(songs[newIndex])
    }

    /**
     * Play next song
     */
    private fun playNextSong() {
        if (songs.isEmpty()) return

        val newIndex = if (currentSongIndex < songs.size - 1) {
            currentSongIndex + 1
        } else {
            0 // Loop to first song
        }
        playSong(songs[newIndex])
    }

    /**
     * Stop playback
     */
    private fun stopPlayback() {
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            } catch (e: Exception) {
                Timber.e(e, "Error stopping MediaPlayer")
            }
        }
        mediaPlayer = null
        isPlaying = false
        isPrepared = false
        stopProgressUpdate()
    }

    /**
     * Update play/pause button
     */
    private fun updatePlayPauseButton() {
        if (isPlaying) {
            btnPlayPause.setImageResource(R.drawable.icon_pause)
        } else {
            btnPlayPause.setImageResource(R.drawable.icon_play)
        }
    }

    /**
     * Update total time
     */
    private fun updateTotalTime() {
        mediaPlayer?.let { mp ->
            if (isPrepared) {
                val duration = mp.duration.toLong()
                tvTotalTime.text = formatTime(duration)
                progressBar.max = 100
            }
        }
    }

    /**
     * Update progress
     */
    private fun updateProgress() {
        mediaPlayer?.let { mp ->
            if (isPrepared && mp.isPlaying) {
                val currentPosition = mp.currentPosition
                val duration = mp.duration
                
                if (duration > 0) {
                    val progress = (currentPosition * 100 / duration).toInt()
                    progressBar.progress = progress
                    
                    tvCurrentTime.text = formatTime(currentPosition.toLong())
                    val remaining = duration - currentPosition
                    tvTimeDisplay.text = formatTime(remaining.toLong())
                }
            }
        }
    }

    /**
     * Start progress update
     */
    private fun startProgressUpdate() {
        progressHandler.post(progressUpdateRunnable)
    }

    /**
     * Stop progress update
     */
    private fun stopProgressUpdate() {
        progressHandler.removeCallbacks(progressUpdateRunnable)
    }

    /**
     * Format time (milliseconds to mm:ss)
     */
    private fun formatTime(milliseconds: Long): String {
        val totalSeconds = (milliseconds / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
        dbHelper.close()
        Timber.d("MusicPlayerActivity destroyed")
    }

    override fun onPause() {
        super.onPause()
        // Pause playback (optional, depends on requirements)
        // if (isPlaying) {
        //     togglePlayPause()
        // }
    }
}
