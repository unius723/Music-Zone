package org.wit.musiczone.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.wit.musiczone.R

class GameOverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        val backgroundGif: ImageView = findViewById(R.id.background_gif)
        Glide.with(this).asGif().load(R.drawable.musiczone_daybg).into(backgroundGif)

        val score = intent.getIntExtra("SCORE", 0)
        val scoreText: TextView = findViewById(R.id.scoreText)
        scoreText.text = "Score: $score"

        val replayButton: Button = findViewById(R.id.replayButton)
        replayButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
            finish()
        }

        val exitButton: Button = findViewById(R.id.exitButton)
        exitButton.setOnClickListener {
            finish()
        }
    }
}