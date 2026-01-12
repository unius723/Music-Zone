package org.wit.musiczone.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.wit.musiczone.R

class RockStyleActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_rock_style)
            setupTopNavigation()
        } catch (e: Exception) {
            Log.e("RockStyleActivity", "Error in onCreate: ${e.message}", e)
            e.printStackTrace()
            finish()
        }
    }
    
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
            
            // Music/Listening button - jump to MusicPlayerActivity
            findViewById<ImageView>(R.id.icon_listening)?.setOnClickListener {
                val intent = Intent(this, MusicPlayerActivity::class.java)
                startActivity(intent)
            }
            
            findViewById<ImageView>(R.id.icon_map)?.setOnClickListener {
                // Map functionality
            }
            
            findViewById<ImageView>(R.id.icon_game)?.setOnClickListener {
                // Game functionality
            }
            
            findViewById<ImageView>(R.id.icon_report)?.setOnClickListener {
                val intent = Intent(this, FeedbackActivity::class.java)
                startActivity(intent)
            }
            
            findViewById<ImageView>(R.id.icon_exit)?.setOnClickListener {
                finish()
            }
        } catch (e: Exception) {
            Log.e("RockStyleActivity", "Error in setupTopNavigation: ${e.message}", e)
            e.printStackTrace()
        }
    }
}
