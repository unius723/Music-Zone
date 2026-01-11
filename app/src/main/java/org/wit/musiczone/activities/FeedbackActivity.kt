package org.wit.musiczone.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.wit.musiczone.R
import org.wit.musiczone.database.MusicDBHelper
import timber.log.Timber

class FeedbackActivity : AppCompatActivity() {
    
    private lateinit var dbHelper: MusicDBHelper
    private lateinit var backButton: ImageView
    private lateinit var feedbackEditText: EditText
    private lateinit var submitButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        
        // Initialize database
        dbHelper = MusicDBHelper(this)
        
        // Setup background GIF
        setupBackground()
        
        // Initialize views
        initializeViews()
        
        // Setup click listeners
        setupClickListeners()
    }
    
    private fun setupBackground() {
        val backgroundImageView = findViewById<ImageView>(R.id.backgroundGif)
        Glide.with(this)
            .asGif()
            .load(R.drawable.musiczone_daybg)
            .into(backgroundImageView)
    }
    
    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        feedbackEditText = findViewById(R.id.feedbackEditText)
        submitButton = findViewById(R.id.submitButton)
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }
        
        submitButton.setOnClickListener {
            submitFeedback()
        }
    }
    
    private fun submitFeedback() {
        val feedbackText = feedbackEditText.text.toString().trim()
        
        if (feedbackText.isEmpty()) {
            Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Save feedback to database
        val success = dbHelper.insertFeedback(feedbackText)
        
        if (success) {
            Toast.makeText(this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show()
            feedbackEditText.setText("")
            finish()
        } else {
            Toast.makeText(this, "Failed to submit feedback. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}

