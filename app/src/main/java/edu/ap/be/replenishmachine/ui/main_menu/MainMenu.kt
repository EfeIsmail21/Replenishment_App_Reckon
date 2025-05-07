package edu.ap.be.replenishmachine.ui.main_menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.vuzix.hud.actionmenu.ActionMenuActivity
import edu.ap.be.replenishmachine.R
import edu.ap.be.replenishmachine.auth.manager.AuthManager
import edu.ap.be.replenishmachine.auth.provider.ReckonAuthProvider
import edu.ap.be.replenishmachine.auth.token.storage.SharedPrefsTokenStorage
import edu.ap.be.replenishmachine.ui.login.LoginActivity
import edu.ap.be.replenishmachine.ui.machine.main.MachineMenu
import edu.ap.be.replenishmachine.ui.settings.SettingsMenu
import kotlinx.coroutines.launch

class MainMenu : ActionMenuActivity() {
    private lateinit var machineMenuItem: MenuItem
    private lateinit var settingsMenuItem: MenuItem
    private lateinit var logoutMenuItem: MenuItem

    private lateinit var mainTitleTextView: TextView
    private lateinit var mainValueTextView: TextView
    private lateinit var mainIconImageView: ImageView

    private val TAG = "MainMenu"
    private lateinit var authManager: AuthManager

    override fun alwaysShowActionMenu(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.main_menu)

            mainTitleTextView = findViewById(R.id.main_title)
            mainValueTextView = findViewById(R.id.main_value)
            mainIconImageView = findViewById(R.id.icon)

            val tokenStorage = SharedPrefsTokenStorage(this)
            val authProvider = ReckonAuthProvider(
                secretEndpoint = "https://buybye-dev.reckon.ai/admin/test",
                userTokenEndpoint = "https://auth-dev.reckon.ai/app/send",
                authTokenEndpoint = "https://auth-dev.reckon.ai/app/send"
            )
            authManager = AuthManager(this, authProvider, tokenStorage)

            fetchUserDataAndUpdateTitle()

            updateMenuItems()

        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Exception during initialization", e)
            finish()
            return
        }
    }

    private fun fetchUserDataAndUpdateTitle() {
        lifecycleScope.launch {
            try {
                val userData = authManager.getUserData()

                // Capitalize the first letter of username and organization name
                val capitalizedUsername = userData.username.capitalize()
                val capitalizedOrgName = userData.organizationName.capitalize()

                mainTitleTextView.text = getString(
                    R.string.welcome_user_format,
                    "$capitalizedUsername from $capitalizedOrgName"
                )

            } catch (e: Exception) {
                Log.e(TAG, "Failed to get user data for title", e)
                mainTitleTextView.text = getString(R.string.welcome_default)
            }
        }
    }

    override fun onCreateActionMenu(menu: Menu): Boolean {
        super.onCreateActionMenu(menu)
        try {
            menuInflater.inflate(R.menu.main_action_menu, menu)
            machineMenuItem = menu.findItem(R.id.machine_menu_item)
            settingsMenuItem = menu.findItem(R.id.settings_menu_item)
            logoutMenuItem = menu.findItem(R.id.logout_menu_item)

            machineMenuItem.intent = Intent(this, MachineMenu::class.java)
            settingsMenuItem.intent = Intent(this, SettingsMenu::class.java)

        } catch (e: Exception) {
            Log.e(TAG, "onCreateActionMenu: Exception occurred", e)
        }
        return true
    }

    override fun getActionMenuGravity(): Int = Gravity.CENTER

    private fun updateMenuItems() {
        if (::mainIconImageView.isInitialized) {
            mainIconImageView.setImageResource(R.drawable.baseline_info_24)
        } else {
            Log.w(TAG, "updateMenuItems: mainIconImageView not initialized yet")
        }
    }

    fun logoutUser(item: MenuItem?) {
        Toast.makeText(this, R.string.logging_out, Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                authManager.logout()

                val intent = Intent(this@MainMenu, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                Log.e(TAG, "logoutUser: Error during logout", e)
                Toast.makeText(
                    this@MainMenu,
                    getString(R.string.logout_failure, e.localizedMessage ?: "Unknown error"),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}