package org.wit.musiczone.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber
import org.wit.musiczone.R
import org.wit.musiczone.database.MusicDBHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: TextView
    private lateinit var forgotButton: TextView
    private lateinit var dbHelper: MusicDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize database
        dbHelper = MusicDBHelper(this)
        
        // Print database path (for debugging)
        val dbPath = getDatabasePath("music_app.db").absolutePath
        Timber.d("Database path: $dbPath")
        Timber.d("Database file location: /data/data/${packageName}/databases/music_app.db")

        // Initialize views
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        forgotButton = findViewById(R.id.forgotButton)

        // Set up click listeners
        loginButton.setOnClickListener {
            handleLogin()
        }

        registerButton.setOnClickListener {
            showRegisterDialog()
        }

        forgotButton.setOnClickListener {
            handleForgotPassword()
        }
    }

    private fun handleLogin() {
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (username.isEmpty()) {
            usernameInput.error = getString(R.string.enter_username)
            usernameInput.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordInput.error = getString(R.string.enter_password)
            passwordInput.requestFocus()
            return
        }

        // Use database for login
        val userId = dbHelper.loginUser(username, password)
        
        if (userId > 0) {
            Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
            Timber.d("User login successful: $username (ID: $userId)")
            
            // Navigate to main interface
            val intent = Intent(this, ChooseStyleActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_LONG).show()
            Timber.w("Login failed: $username")
        }
    }

    private fun showRegisterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_register, null)
        val registerUsernameInput = dialogView.findViewById<EditText>(R.id.registerUsernameInput)
        val registerPasswordInput = dialogView.findViewById<EditText>(R.id.registerPasswordInput)
        val registerPasswordConfirmInput = dialogView.findViewById<EditText>(R.id.registerPasswordConfirmInput)
        val registerEmailInput = dialogView.findViewById<EditText>(R.id.registerEmailInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.register_new_user))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.register)) { _, _ ->
                handleRegister(
                    registerUsernameInput.text.toString().trim(),
                    registerPasswordInput.text.toString().trim(),
                    registerPasswordConfirmInput.text.toString().trim(),
                    registerEmailInput.text.toString().trim()
                )
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.show()
    }

    private fun handleRegister(
        username: String,
        password: String,
        passwordConfirm: String,
        email: String
    ) {
        // Validate input
        if (username.isEmpty()) {
            Toast.makeText(this, getString(R.string.username_empty), Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, getString(R.string.password_empty), Toast.LENGTH_SHORT).show()
            return
        }

        if (password != passwordConfirm) {
            Toast.makeText(this, getString(R.string.password_mismatch), Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 3) {
            Toast.makeText(this, getString(R.string.password_too_short), Toast.LENGTH_SHORT).show()
            return
        }

        // Check if username already exists
        if (dbHelper.checkUsernameExists(username)) {
            Toast.makeText(this, getString(R.string.username_exists), Toast.LENGTH_LONG).show()
            return
        }

        // Register user
        val success = dbHelper.registerUser(username, password)
        
        if (success) {
            Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
            Timber.d("User registration successful: $username")
            // Auto-fill username
            usernameInput.setText(username)
            passwordInput.requestFocus()
        } else {
            Toast.makeText(this, getString(R.string.register_failed), Toast.LENGTH_SHORT).show()
            Timber.w("User registration failed: $username")
        }
    }

    private fun handleForgotPassword() {
        Toast.makeText(this, getString(R.string.forgot_password_not_implemented), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}
