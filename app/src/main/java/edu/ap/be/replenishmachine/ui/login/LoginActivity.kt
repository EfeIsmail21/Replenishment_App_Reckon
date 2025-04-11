package edu.ap.be.replenishmachine.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.autofill.AutofillManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import edu.ap.be.replenishmachine.R
import edu.ap.be.replenishmachine.auth.manager.AuthManager
import edu.ap.be.replenishmachine.auth.provider.ReckonAuthProvider
import edu.ap.be.replenishmachine.auth.token.storage.SharedPrefsTokenStorage
import edu.ap.be.replenishmachine.ui.main_menu.MainMenu
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * LoginActivity is responsible for user authentication and navigation to MainMenuActivity
 * upon successful login or when a user is already logged in.
 */
class LoginActivity : AppCompatActivity() {
    
    private lateinit var authManager: AuthManager
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate: --- LoginActivity Started ---")
        super.onCreate(savedInstanceState)
        try {
            Log.d(TAG, "onCreate: Setting content view...")
            setContentView(R.layout.login_view) 
            Log.d(TAG, "onCreate: Content view set successfully.")

            // Initialize AuthManager with necessary components
            initializeAuthManager()

            // Set up UI elements and their event handlers
            setupUI()

            // Check if user is already logged in
            checkLoginStatus()

        } catch (e: Exception) {
            Log.e(TAG, "onCreate: !!! CRASH DURING INITIALIZATION !!!", e)
        }
        Log.i(TAG, "onCreate: --- Initialization Complete ---")
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
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)

        // Ensure autofill hints are properly set
        emailEditText.setAutofillHints(View.AUTOFILL_HINT_EMAIL_ADDRESS)
        passwordEditText.setAutofillHints(View.AUTOFILL_HINT_PASSWORD)

        // Add text change listeners to detect autofill
        emailEditText.addTextChangedListener {
            checkForAutofill()
            checkJsonInput()
        }
        
        passwordEditText.addTextChangedListener {
            checkForAutofill()
        }

        // Set up login button click handler
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                performLogin(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /**
     * Check if the email field contains a JSON object with credentials and handle it
     */
    private fun checkJsonInput() {
        val emailText = emailEditText.text.toString().trim()
        if (emailText.startsWith("{") && emailText.endsWith("}")) {
            try {
                val jsonObject = JSONObject(emailText)
                if (jsonObject.has("email") && jsonObject.has("password")) {
                    val email = jsonObject.getString("email")
                    val password = jsonObject.getString("password")

                    // Update the UI fields with parsed values
                    emailEditText.setText(email)
                    passwordEditText.setText(password)

                    // Auto-login with the extracted credentials after a short delay
                    passwordEditText.postDelayed({
                        performLogin(email, password)
                    }, 500)
                }
            } catch (e: Exception) {
                // Not valid JSON or missing required fields, do nothing
                Log.e("LoginActivity", "Failed to parse JSON input", e)
            }
        }
    }

    /**
     * Check if both fields are filled by autofill and login automatically
     */
    private fun checkForAutofill() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        
        if (email.isNotEmpty() && password.isNotEmpty()) {
            // If both fields are filled, check if autofill is active
            val autofillManager = getSystemService(AutofillManager::class.java)
            if (autofillManager?.isAutofillSupported == true) {
                // Slight delay to ensure fields are completely filled
                emailEditText.postDelayed({
                    // Attempt auto-login if both fields are filled
                    performLogin(email, password)
                }, 500)
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
        val intent = Intent(this, MainMenu::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: Called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: --- LoginActivity Destroyed ---")
    }
}