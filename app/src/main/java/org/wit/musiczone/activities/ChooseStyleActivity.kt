package org.wit.musiczone.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import org.wit.musiczone.R

class ChooseStyleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_style)

        val ivBackground = findViewById<ImageView>(R.id.ivBackground)
        Glide.with(this)
            .asGif()
            .load(R.drawable.musiczone_daybg)
            .into(ivBackground)

        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnRock = findViewById<MaterialButton>(R.id.btnRock)
        btnRock.setOnClickListener {
            val intent = Intent(this, RockStyleActivity::class.java)
            startActivity(intent)
        }
    }
}
