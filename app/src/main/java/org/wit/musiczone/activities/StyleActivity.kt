package org.wit.musiczone.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.VideoView
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import org.wit.musiczone.R

class StyleActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_style)

            // Set background based on intent
            val style = intent.getStringExtra("style") ?: "Rock"
            val rootLayout = findViewById<ConstraintLayout>(R.id.rootLayout)
            val bgRes = when(style) {
                "Rock" -> R.drawable.style_rock
                "Classic" -> R.drawable.style_classic
                "Electronic" -> R.drawable.style_electronic
                "Chinese" -> R.drawable.style_chinese
                else -> R.drawable.style_rock
            }
            rootLayout.setBackgroundResource(bgRes)

            // Setup UI components
            setupTopNavigation()
            setupVideoPlayer()

        } catch (e: Exception) {
            Log.e("StyleActivity", "Error in onCreate: ${e.message}", e)
            e.printStackTrace()
            finish()
        }
    }

    private fun setupVideoPlayer() {
        videoView = findViewById(R.id.video_view_mv)
        val uri = Uri.parse("android.resource://$packageName/${R.raw.mv1}")
        videoView.setVideoURI(uri)

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.setVolume(1.0f, 1.0f)
        }
    }

    override fun onResume() {
        super.onResume()
        videoView.start()
    }

    override fun onPause() {
        super.onPause()
        if (videoView.isPlaying) {
            videoView.pause()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTopNavigation() {
        try {
            findViewById<ImageView>(R.id.icon_back)?.setOnClickListener {
                val intent = Intent(this, ChooseStyleActivity::class.java)
                startActivity(intent)
                finish()
            }
            
            findViewById<ImageView>(R.id.icon_search)?.setOnClickListener {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            }
            
            findViewById<ImageView>(R.id.icon_change_style)?.setOnClickListener {
                val intent = Intent(this, ChooseStyleActivity::class.java)
                startActivity(intent)
                finish()
            }
            
            findViewById<ImageView>(R.id.icon_display_room)?.setOnClickListener {
                // Display room functionality
            }
            
            findViewById<ImageView>(R.id.icon_recommend)?.setOnClickListener {
                val intent = Intent(this, MusicRecommendationActivity::class.java)
                startActivity(intent)
            }

            findViewById<ImageView>(R.id.icon_map)?.setOnClickListener {
                val intent = Intent(this, AmapLocationActivity::class.java)
                startActivity(intent)
            }

            findViewById<ImageView>(R.id.icon_listening)?.setOnClickListener {
                val intent = Intent(this, MusicPlayerActivity::class.java)
                startActivity(intent)
            }

            findViewById<ImageView>(R.id.icon_game)?.setOnClickListener {
                val intent = Intent(this, StartGameActivity::class.java)
                startActivity(intent)
            }
            
            findViewById<ImageView>(R.id.icon_report)?.setOnClickListener {
                val intent = Intent(this, FeedbackActivity::class.java)
                startActivity(intent)
            }

            findViewById<ImageView>(R.id.icon_exit).setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val x = event.x
                    val width = v.width
                    if (x < width / 2) {
                        startActivity(Intent(this, AppInformationActivity::class.java))
                    } else {
                        startActivity(Intent(this, StartActivity::class.java))
                        finish()
                    }
                }
                true
            }


        } catch (e: Exception) {
            Log.e("StyleActivity", "Error in setupTopNavigation: ${e.message}", e)
            e.printStackTrace()
        }
    }
}