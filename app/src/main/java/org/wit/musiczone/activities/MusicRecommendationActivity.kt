package org.wit.musiczone.activities

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import org.wit.musiczone.R
import org.wit.musiczone.adapters.PlaylistAdapter
import org.wit.musiczone.database.MusicDBHelper
import org.wit.musiczone.models.Song
import java.io.IOException

/**
 * Music Recommendation Activity
 * Displays user's favorite songs and allows playback
 */
class MusicRecommendationActivity : AppCompatActivity() {

    private lateinit var dbHelper: MusicDBHelper
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var adapter: PlaylistAdapter
    private lateinit var btnBack: ImageView
    private lateinit var btnRecommend: ImageView
    private lateinit var tvEmptyMessage: TextView

    // Player controls
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnSkipPrevious: ImageButton
    private lateinit var btnSkipNext: ImageButton
    private lateinit var progressBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvTimeDisplay: TextView

    private val favoriteSongs = mutableListOf<Song>()
    private var currentSongIndex = -1
    private var currentSong: Song? = null

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var isPrepared = false

    private val progressHandler = Handler(Looper.getMainLooper())
    private val progressUpdateRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            progressHandler.postDelayed(this, 100)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_recommendation)

        dbHelper = MusicDBHelper(this)

        initializeViews()
        setupRecyclerView()
        setupPlaybackControls()
        loadFavoriteSongs()
    }

    private fun initializeViews() {
        favoritesRecyclerView = findViewById(R.id.favorites_recycler_view)
        btnBack = findViewById(R.id.btn_back)
        btnRecommend = findViewById(R.id.btn_recommend)
        tvEmptyMessage = findViewById(R.id.tv_empty_message)

        // Player controls
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnSkipPrevious = findViewById(R.id.btn_skip_previous)
        btnSkipNext = findViewById(R.id.btn_skip_next)
        progressBar = findViewById(R.id.progress_bar)
        tvCurrentTime = findViewById(R.id.tv_current_time)
        tvTotalTime = findViewById(R.id.tv_total_time)
        tvTimeDisplay = findViewById(R.id.tv_time_display)

        btnBack.setOnClickListener {
            finish()
        }

        btnRecommend.setOnClickListener {
            // Recommend button - can be used for future functionality
        }
    }

    private fun setupRecyclerView() {
        adapter = PlaylistAdapter(
            songs = favoriteSongs,
            onPlayClick = { song ->
                playSong(song)
            },
            onMoreClick = { song ->
                // More options
            },
            onFavoriteClick = { song, isFavorite ->
                handleFavoriteClick(song, isFavorite)
            },
            isFavorite = { songId ->
                dbHelper.isFavorite(songId)
            }
        )
        favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        favoritesRecyclerView.adapter = adapter
    }

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

    private fun loadFavoriteSongs() {
        val favorites = dbHelper.getAllFavorites()
        favoriteSongs.clear()
        favoriteSongs.addAll(favorites)

        if (favoriteSongs.isEmpty()) {
            tvEmptyMessage.visibility = View.VISIBLE
            favoritesRecyclerView.visibility = View.GONE
        } else {
            tvEmptyMessage.visibility = View.GONE
            favoritesRecyclerView.visibility = View.VISIBLE
            adapter.notifyDataSetChanged()
        }
    }

    private fun handleFavoriteClick(song: Song, isFavorite: Boolean) {
        if (isFavorite) {
            val success = dbHelper.addToFavorite(song.id)
            if (success) {
                // Already in favorites
            }
        } else {
            val count = dbHelper.removeFromFavorite(song.id)
            if (count > 0) {
                // Remove from favorites and reload list
                loadFavoriteSongs()
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun playSong(song: Song) {
        try {
            stopPlayback()

            currentSong = song
            currentSongIndex = favoriteSongs.indexOf(song)
            adapter.currentPlayingIndex = currentSongIndex
            Timber.d("Playing favorite song: ${song.title} at index $currentSongIndex")

            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(song.filePath)
                    prepareAsync()

                    setOnPreparedListener { mp ->
                        this@MusicRecommendationActivity.isPrepared = true
                        mp.start()
                        this@MusicRecommendationActivity.isPlaying = true
                        this@MusicRecommendationActivity.updatePlayPauseButton()
                        this@MusicRecommendationActivity.updateTotalTime()
                        this@MusicRecommendationActivity.startProgressUpdate()
                        this@MusicRecommendationActivity.adapter.currentPlayingIndex = this@MusicRecommendationActivity.currentSongIndex
                        Timber.d("MediaPlayer prepared and started")
                    }

                    setOnCompletionListener {
                        Timber.d("Song completed, playing next")
                        playNextSong()
                    }

                    setOnErrorListener { _, what, extra ->
                        Timber.e("MediaPlayer error: what=$what, extra=$extra")
                        this@MusicRecommendationActivity.isPrepared = false
                        false
                    }
                } catch (e: IOException) {
                    Timber.e(e, "Error setting data source: ${song.filePath}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error playing song")
        }
    }

    private fun togglePlayPause() {
        if (favoriteSongs.isEmpty()) {
            return
        }

        if (currentSongIndex < 0 || currentSong == null) {
            // If no song is playing, play the first one
            if (favoriteSongs.isNotEmpty()) {
                playSong(favoriteSongs[0])
            }
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

    private fun playPreviousSong() {
        if (favoriteSongs.isEmpty()) return

        val newIndex = if (currentSongIndex > 0) {
            currentSongIndex - 1
        } else {
            favoriteSongs.size - 1 // Loop to last song
        }
        playSong(favoriteSongs[newIndex])
    }

    private fun playNextSong() {
        if (favoriteSongs.isEmpty()) return

        val newIndex = if (currentSongIndex < favoriteSongs.size - 1) {
            currentSongIndex + 1
        } else {
            0
        }
        playSong(favoriteSongs[newIndex])
    }

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

    private fun updatePlayPauseButton() {
        if (isPlaying) {
            btnPlayPause.setImageResource(R.drawable.icon_pause)
        } else {
            btnPlayPause.setImageResource(R.drawable.icon_play)
        }
    }

    private fun updateTotalTime() {
        mediaPlayer?.let { mp ->
            if (isPrepared) {
                val duration = mp.duration.toLong()
                tvTotalTime.text = formatTime(duration)
                progressBar.max = 100
            }
        }
    }

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

    private fun formatTime(milliseconds: Long): String {
        val totalSeconds = (milliseconds / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun startProgressUpdate() {
        progressHandler.post(progressUpdateRunnable)
    }

    private fun stopProgressUpdate() {
        progressHandler.removeCallbacks(progressUpdateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
        dbHelper.close()
        Timber.d("MusicRecommendationActivity destroyed")
    }

    override fun onPause() {
        super.onPause()
        // Optionally pause playback when activity is paused
    }
}

