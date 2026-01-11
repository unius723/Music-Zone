package org.wit.musiczone.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import org.wit.musiczone.R
import timber.log.Timber
import android.content.res.Configuration


class StartActivity : AppCompatActivity() {
    private lateinit var gifBg: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        gifBg = findViewById(R.id.gifBg)

        val rootLayout = findViewById<ConstraintLayout>(R.id.start)
        val logo = findViewById<ImageView>(R.id.musiczone_logo)
        val bar = findViewById<ProgressBar>(R.id.loading_bar)
        val goButton = findViewById<ImageButton>(R.id.go_button)

        bar.max = 100
        bar.progress = 0

        val isNight = (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        rootLayout.setBackgroundColor(
            if (isNight)
                getColor(R.color.Dark_purple)
            else
                getColor(android.R.color.white)
        )

        Glide.with(this)
            .asGif()
            .load(
                if (isNight)
                    R.drawable.musiczone_nightbg
                else
                    R.drawable.musiczone_daybg
            )
            .into(gifBg)

        logo.setImageResource(
            if (isNight)
                R.drawable.musiczone_logo_night
            else
                R.drawable.musiczone_logo
        )

        goButton.setOnClickListener {
            Timber.i("Go button clicked")
            goNext()
        }

        bar.postDelayed(object : Runnable {
            override fun run() {
                if (bar.progress < 70) {
                    bar.progress += 3
                    bar.postDelayed(this, 8)
                } else if (bar.progress < 100) {
                    bar.progress += 1
                    bar.postDelayed(this, 25)
                } else {
                    onLoadFinished(goButton)
                }
            }
        }, 10)
    }

    private fun onLoadFinished(goButton: ImageButton) {
        goButton.isEnabled = true
        goButton.animate().alpha(1f).setDuration(300).start()
    }

    private fun goNext() {
        fadeOutProgressBar()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun fadeOutProgressBar() {
        val bar = findViewById<ProgressBar>(R.id.loading_bar)
        bar.animate().alpha(0f).setDuration(300).start()
    }
}
