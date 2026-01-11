package org.wit.musiczone.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import org.wit.musiczone.R

class StartGameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_game)

        val backgroundGif: ImageView = findViewById(R.id.background_gif)
        Glide.with(this).asGif().load(R.drawable.musiczone_daybg).into(backgroundGif)

        val titleText: TextView = findViewById(R.id.titleText)
        val typeface: Typeface? = ResourcesCompat.getFont(this, R.font.press_start_2p_regular)
        titleText.typeface = typeface

        val startButton: Button = findViewById(R.id.startButton)
        val exitButton: Button = findViewById(R.id.exitButton)

        startButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        exitButton.setOnClickListener {
            finish()
        }
    }
}