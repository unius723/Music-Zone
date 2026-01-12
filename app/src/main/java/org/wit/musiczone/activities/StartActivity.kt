package org.wit.musiczone.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.wit.musiczone.R
import org.wit.musiczone.main.MainApp
import timber.log.Timber
import org.wit.musiczone.activities.LoginActivity


class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val gifBg = findViewById<ImageView>(R.id.gifBg)
        val bar = findViewById<ProgressBar>(R.id.loading_bar)
        val goButton = findViewById<ImageButton>(R.id.go_button)

        bar.max = 100
        bar.progress = 0

        Glide.with(this)
            .asGif()
            .load(R.drawable.musiczone_daybg)
            .into(gifBg)

        goButton.setOnClickListener {
            Timber.i("Go button clicked")
            goNext()
        }

        bar.postDelayed(object : Runnable {
            override fun run() {
                if (bar.progress < 100) {
                    bar.progress += 1
                    bar.postDelayed(this, 20)
                } else {
                    onLoadFinished(goButton)
                }
            }
        }, 25)
    }


    private fun onLoadFinished(goButton: ImageButton) {
        goButton.isEnabled = true
        goButton.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    /**
//     * Navigate to next page
//     */
    private fun goNext() {
        Timber.i("Go clicked.")
        fadeOutProgressBar()
        startActivity(Intent(this, LoginActivity::class.java)) // ⬅️ Change to your target Activity
        finish()
    }

    /**
     * Progress bar fade out animation
     */
    private fun fadeOutProgressBar() {
        val bar = findViewById<ProgressBar>(R.id.loading_bar)
        bar.animate()
            .alpha(0f)
            .setDuration(300)
            .start()
    }
}
