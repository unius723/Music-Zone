package org.wit.musiczone.activities

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.wit.musiczone.R

class AppInformationActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_information) // 注意这里要对应 XML 文件名

        setupBackground()
        setupClickListeners()

        val infoCard = findViewById<FrameLayout>(R.id.infoCard)

        val floatAnim = AnimationUtils.loadAnimation(this, R.anim.float_up_down)
        infoCard.startAnimation(floatAnim)

    }

    private fun setupBackground() {
        val backgroundImageView = findViewById<ImageView>(R.id.backgroundGif)
        Glide.with(this)
            .asGif()
            .load(R.drawable.musiczone_daybg)
            .into(backgroundImageView)
    }

    private fun setupClickListeners() {
        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish() // 点击返回按钮关闭页面
        }
    }
}
