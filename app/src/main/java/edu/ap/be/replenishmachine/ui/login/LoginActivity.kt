package edu.ap.be.replenishmachine.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.ap.be.replenishmachine.R
import edu.ap.be.replenishmachine.ui.mainmenu.MainMenuActivity
import edu.ap.be.replenishmachine.auth.manager.AuthManager
import edu.ap.be.replenishmachine.auth.provider.ReckonAuthProvider
import edu.ap.be.replenishmachine.auth.token.storage.SharedPrefsTokenStorage
import kotlinx.coroutines.launch

/**
 * LoginActivity is responsible for user authentication and navigation to MainMenuActivity
 * upon successful login or when a user is already logged in.
 */
class LoginActivity : AppCompatActivity() {
    
    private lateinit var authManager: AuthManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_view)

        // Initialize AuthManager with necessary components
        initializeAuthManager()

        // Set up UI elements and their event handlers
        setupUI()

        // Check if user is already logged in
        checkLoginStatus()
    }

    /**
     * Initialize the AuthManager with token storage and authentication provider
     */
    private fun initializeAuthManager() {
        val tokenStorage = SharedPrefsTokenStorage(this)
        val authProvider = ReckonAuthProvider(
            secretEndpoint = "https://buybye-dev.reckon.ai/admin/test",
            userTokenEndpoint = "https://auth-dev.reckon.ai/app/send",
            authTokenEndpoint = "https://auth-dev.reckon.ai/app/send"
        )
        authManager = AuthManager(this, authProvider, tokenStorage)
    }

    /**
     * Set up UI elements and their click listeners
     */
    private fun setupUI() {
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Perform login
                performLogin(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Perform login using the provided credentials
     */
    private fun performLogin(email: String, password: String) {
        lifecycleScope.launch {
            try {
                // Show loading state
                Toast.makeText(this@LoginActivity, "Logging in...", Toast.LENGTH_SHORT).show()

                // Call AuthManager to perform login
                val credentials = authManager.login(email, password)

                // Show success message and navigate to main menu
                Toast.makeText(
                    this@LoginActivity,
                    "Welcome ${credentials.userData.username}!",
                    Toast.LENGTH_SHORT
                ).show()

                navigateToMainMenu()

            } catch (e: Exception) {
                // Show error message if login fails
                Toast.makeText(
                    this@LoginActivity,
                    "Login failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Check if user is already logged in and navigate to main menu if true
     */
    private fun checkLoginStatus() {
        lifecycleScope.launch {
            try {
                if (authManager.isLoggedIn()) {
                    navigateToMainMenu()
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Failed to check login status", e)
            }
        }
    }

    /**
     * Navigate to MainMenuActivity
     */
    private fun navigateToMainMenu() {
        val intent = Intent(this, MainMenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}