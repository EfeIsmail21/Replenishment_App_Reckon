package edu.ap.be.replenishmachine

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.ap.be.replenishmachine.auth.manager.AuthManager
import edu.ap.be.replenishmachine.auth.provider.ReckonAuthProvider
import edu.ap.be.replenishmachine.auth.token.storage.SharedPrefsTokenStorage
import edu.ap.be.replenishmachine.views.LoginViewModel
import kotlinx.coroutines.launch

/**
 * Main activity serving as the entry point for the application.
 * Handles user login and initializes core auth components.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_view)
        
        // Create the necessary components for AuthManager
        val tokenStorage = SharedPrefsTokenStorage(this)
        val authProvider = ReckonAuthProvider(
            secretEndpoint = "https://buybye-dev.reckon.ai/admin/test",
            userTokenEndpoint = "https://auth-dev.reckon.ai/app/send",
            authTokenEndpoint = "https://auth-dev.reckon.ai/app/send"
        )
        val authManager = AuthManager(this, authProvider, tokenStorage)

        // Initialize LoginViewModel
        loginViewModel = LoginViewModel(authManager)

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            
            // Validate inputs
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Launch coroutine for login using ViewModel
            lifecycleScope.launch {
                val result = loginViewModel.performLogin(email, password)
                result.onSuccess { credentials ->
                    // Handle successful login
                    Toast.makeText(
                        this@MainActivity,
                        "Welcome, ${credentials.userData.username}!",
                        Toast.LENGTH_SHORT
                    ).show()
                    // TODO: Navigate to next screen
                }.onFailure { error ->
                    // Handle login failure
                    Toast.makeText(
                        this@MainActivity,
                        "Login failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}