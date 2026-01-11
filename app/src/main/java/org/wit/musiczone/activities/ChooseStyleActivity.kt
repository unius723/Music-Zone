package org.wit.musiczone.activities

import android.content.res.Configuration
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import org.wit.musiczone.R

class ChooseStyleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_style)

        val rootLayout = findViewById<ConstraintLayout>(R.id.root)
        val ivBackground = findViewById<ImageView>(R.id.ivBackground)
        val isNight = (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        rootLayout.setBackgroundColor(
            if (isNight) getColor(R.color.Dark_purple)
            else getColor(android.R.color.white)
        )

        val bgRes = if (isNight) R.drawable.musiczone_nightbg else R.drawable.musiczone_daybg
        Glide.with(this)
            .asGif()
            .load(bgRes)
            .into(ivBackground)

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<MaterialButton>(R.id.btnRock).setOnClickListener {
            startStyleActivity("Rock")
        }
        findViewById<MaterialButton>(R.id.btnClassic).setOnClickListener {
            startStyleActivity("Classic")
        }
        findViewById<MaterialButton>(R.id.btnElectronic).setOnClickListener {
            startStyleActivity("Electronic")
        }
        findViewById<MaterialButton>(R.id.btnChinese).setOnClickListener {
            startStyleActivity("Chinese")
        }
    }

    private fun startStyleActivity(style: String) {
        val intent = Intent(this, StyleActivity::class.java)
        intent.putExtra("style", style)
        startActivity(intent)
    }
}
