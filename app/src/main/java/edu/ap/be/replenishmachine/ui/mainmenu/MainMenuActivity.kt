package edu.ap.be.replenishmachine.ui.mainmenu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.ap.be.replenishmachine.R
import edu.ap.be.replenishmachine.auth.manager.AuthManager
import edu.ap.be.replenishmachine.auth.provider.ReckonAuthProvider
import edu.ap.be.replenishmachine.auth.token.storage.SharedPrefsTokenStorage
import edu.ap.be.replenishmachine.ui.login.LoginActivity
import edu.ap.be.replenishmachine.ui.machineaccess.MachineAccessActivity
import edu.ap.be.replenishmachine.ui.settings.SettingsActivity
import kotlinx.coroutines.launch

/**
 * MainMenuActivity serves as the central hub for app navigation
 * using Vuzix HUD action menu for better AR experience.
 */
class MainMenuActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager
    private lateinit var userInfoTextView: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu)

        // Initialize AuthManager
        initializeAuthManager()

        // Initialize header elements
        setupHeaderElements()
        
        // Set up bottom bar with user info
        setupUserInfo()

        // Set up menu icons in the bottom bar
        setupMenuIcons()
    }

    /**
     * Initialize the AuthManager with necessary components
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
     * Set up header elements and status
     */
    private fun setupHeaderElements() {
        val headerStatusTextView = findViewById<TextView>(R.id.tv_header_status)
        headerStatusTextView.setText(R.string.status_connected)
    }

    /**
     * Set up user info text
     */
    private fun setupUserInfo() {
        userInfoTextView = findViewById(R.id.tv_user_info)

        lifecycleScope.launch {
            try {
                val userData = authManager.getUserData()
                userInfoTextView.text = getString(R.string.user_info_format, userData.username.uppercase())
            } catch (e: Exception) {
                Log.e("MainMenuActivity", "Error getting user data", e)
                userInfoTextView.setText(R.string.user_unknown)
            }
        }
    }

    /**
     * Set up menu icons with click actions
     */
    private fun setupMenuIcons() {
        // Machine Access icon
        findViewById<android.view.View>(R.id.btn_machine_access).setOnClickListener {
            Toast.makeText(this, getString(R.string.opening_machine_access), Toast.LENGTH_SHORT)
                .show()
            startActivity(Intent(this, MachineAccessActivity::class.java))
        }

        // Settings icon
        findViewById<android.view.View>(R.id.btn_settings).setOnClickListener {
            Toast.makeText(this, getString(R.string.opening_settings), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Logout icon
        findViewById<android.view.View>(R.id.btn_logout).setOnClickListener {
            performLogout()
        }
    }

    /**
     * Perform logout and navigate back to login screen
     */
    private fun performLogout() {
        lifecycleScope.launch {
            try {
                // Log out user
                authManager.logout()

                // Show success message
                Toast.makeText(
                    this@MainMenuActivity,
                    getString(R.string.logout_success),
                    Toast.LENGTH_SHORT
                ).show()

                // Navigate back to login screen
                navigateToLogin()
            } catch (e: Exception) {
                Log.e("MainMenuActivity", "Error logging out", e)
                Toast.makeText(
                    this@MainMenuActivity,
                    getString(R.string.logout_failure, e.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Navigate back to the login screen
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    companion object {
        // Menu item IDs
        private const val MENU_MACHINE_ACCESS = 1
        private const val MENU_SETTINGS = 3
        private const val MENU_LOGOUT = 4
    }
}