package org.wit.musiczone.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.VideoView
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import org.wit.musiczone.R

class StyleActivity : AppCompatActivity(), StyleContract.View {

    private lateinit var presenter: StyleContract.Presenter
    lateinit var videoView: VideoView
    private var playbackPosition: Int = 0
    private val PLAYBACK_POSITION_KEY = "VIDEO_PLAYBACK_POSITION"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_style)

        // Restore playback position after configuration change (e.g., rotation)
        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.getInt(PLAYBACK_POSITION_KEY, 0)
        }

        presenter = StylePresenter(this)
        videoView = findViewById(R.id.video_view_mv)

        presenter.onViewCreated(intent)
        setupTopNavigationListeners()
    }

    // #region View Interface Implementation
    override fun setBackground(drawableResId: Int) {
        findViewById<ConstraintLayout>(R.id.rootLayout).setBackgroundResource(drawableResId)
    }

    override fun playVideo(videoResId: Int) {
        val uri = Uri.parse("android.resource://$packageName/$videoResId")
        videoView.setVideoURI(uri)

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        // This listener now ONLY prepares the player.
        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.setVolume(1.0f, 1.0f)
            // If we have a saved position, seek to it now that the player is ready.
            if (playbackPosition > 0) {
                videoView.seekTo(playbackPosition)
            } else {
                videoView.start() // Start playback only if it's the very first run.
            }
        }
    }

    override fun adjustVideoPosition(horizontalBias: Float) {
        val params = videoView.layoutParams as ConstraintLayout.LayoutParams
        params.horizontalBias = horizontalBias
        videoView.layoutParams = params
    }

    override fun navigateTo(intent: Intent) {
        startActivity(intent)
    }

    override fun finishActivity() {
        finish()
    }
    // #endregion

    // #region Lifecycle and Listeners
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the current video position to handle configuration changes.
        outState.putInt(PLAYBACK_POSITION_KEY, videoView.currentPosition)
    }

    override fun onPause() {
        super.onPause()
        // Pause the video and save the current position.
        playbackPosition = videoView.currentPosition
        videoView.pause()
    }

    override fun onResume() {
        super.onResume()
        // Resume video playback from the saved position.
        if (playbackPosition > 0) {
            videoView.seekTo(playbackPosition)
        }
        videoView.start() // This is now the primary entry point for starting/resuming playback.
    }

    override fun onDestroy() {
        super.onDestroy()
        videoView.stopPlayback() // Fully release video resources.
        presenter.onDestroy()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTopNavigationListeners() {
        findViewById<ImageView>(R.id.icon_back).setOnClickListener { presenter.onBackClicked() }
        findViewById<ImageView>(R.id.icon_search).setOnClickListener { presenter.onSearchClicked() }
        findViewById<ImageView>(R.id.icon_change_style).setOnClickListener { presenter.onChangeStyleClicked() }
        findViewById<ImageView>(R.id.icon_display_room).setOnClickListener { presenter.onDisplayRoomClicked() }
        findViewById<ImageView>(R.id.icon_recommend).setOnClickListener { presenter.onRecommendClicked() }
        findViewById<ImageView>(R.id.icon_map).setOnClickListener { presenter.onMapClicked() }
        findViewById<ImageView>(R.id.icon_listening).setOnClickListener { presenter.onListeningClicked() }
        findViewById<ImageView>(R.id.icon_game).setOnClickListener { presenter.onGameClicked() }
        findViewById<ImageView>(R.id.icon_report).setOnClickListener { presenter.onReportClicked() }

        findViewById<ImageView>(R.id.icon_exit).setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                presenter.onExitTouched(event.x, v.width)
            }
            true
        }
    }
    // #endregion
}
