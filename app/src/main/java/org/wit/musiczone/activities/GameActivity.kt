package org.wit.musiczone.activities

import android.content.Intent
import android.graphics.Point
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import org.wit.musiczone.R
import org.wit.musiczone.views.game.GameView

class GameActivity : AppCompatActivity() {
    private lateinit var gameView: GameView
    private lateinit var pauseMenu: View
    private var mediaPlayer: MediaPlayer? = null
    private val TAG = "GameActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity is being created.")
        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val point = Point()
        windowManager.defaultDisplay.getSize(point)

        val frameLayout = FrameLayout(this)
        gameView = GameView(this, point.x, point.y)

        val gameViewOverlay = layoutInflater.inflate(R.layout.game_view_overlay, null)
        pauseMenu = layoutInflater.inflate(R.layout.pause_menu, null)
        pauseMenu.visibility = View.GONE

        frameLayout.addView(gameView)
        frameLayout.addView(gameViewOverlay)
        frameLayout.addView(pauseMenu)
        setContentView(frameLayout)

        findViewById<ImageButton>(R.id.icon_pause).setOnClickListener {
            Log.d(TAG, "Pause button clicked.")
            pauseGame()
            pauseMenu.visibility = View.VISIBLE
        }

        setupPauseMenu()
        setupMediaPlayer()
    }

    private fun setupMediaPlayer() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.canon)?.apply {
                isLooping = true
                setVolume(1.0f, 1.0f)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while creating MediaPlayer.", e)
            mediaPlayer = null
        }
    }

    private fun setupPauseMenu() {
        pauseMenu.findViewById<ImageButton>(R.id.icon_continue).setOnClickListener {
            pauseMenu.visibility = View.GONE
            resumeGame()
        }
        pauseMenu.findViewById<ImageButton>(R.id.icon_replay).setOnClickListener {
            finish()
            startActivity(Intent(this, GameActivity::class.java))
        }
        pauseMenu.findViewById<ImageButton>(R.id.icon_exit).setOnClickListener {
            finish()
        }
    }

    private fun pauseGame() {
        gameView.pause()
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    private fun resumeGame() {
        if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
            mediaPlayer?.start()
        }
        gameView.resume()
    }

    override fun onPause() {
        super.onPause()
        pauseGame()
    }

    override fun onResume() {
        super.onResume()
        resumeGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun gameOver(score: Int) {
        runOnUiThread {
            if (!isFinishing) {
                val intent = Intent(this, GameOverActivity::class.java)
                intent.putExtra("SCORE", score)
                startActivity(intent)
                finish()
            }
        }
    }
}