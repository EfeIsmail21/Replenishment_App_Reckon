package edu.ap.be.replenishmachine.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.ap.be.replenishmachine.R

/**
 * SettingsActivity provides configuration options for the app.
 * Follows Vuzix AR design principles for optimal viewing on AR glasses.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var accountEmailTextView: TextView
    private lateinit var brightnessSeekBar: SeekBar
    private lateinit var textSizeSeekBar: SeekBar
    private lateinit var wifiSwitch: Switch
    private lateinit var bluetoothSwitch: Switch

    // Navigation elements - use correct types based on the actual layout
    private lateinit var brightnessLayout: RelativeLayout
    private lateinit var textSizeLayout: RelativeLayout
    private lateinit var wifiLayout: RelativeLayout
    private lateinit var bluetoothLayout: RelativeLayout
    private lateinit var accountLayout: RelativeLayout
    private lateinit var resetAppButton: Button
    private lateinit var backButton: Button
    private lateinit var saveButton: Button

    // Current focus (0-5 for settings options, 6 for back, 7 for save)
    private var currentFocus = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_view)
        
        // Initialize UI components
        initializeUI()
        
        // Set up settings controls
        setupSettingsControls()
        
        // Set up buttons
        setupButtons()

        // Initial focus
        updateFocusHighlight()
    }
    
    /**
     * Initialize UI components and their initial values
     */
    private fun initializeUI() {
        // Text views
        accountEmailTextView = findViewById(R.id.tv_account_email)
        
        // Seek bars
        brightnessSeekBar = findViewById(R.id.seek_brightness)
        textSizeSeekBar = findViewById(R.id.seek_text_size)
        
        // Switches
        wifiSwitch = findViewById(R.id.switch_wifi)
        bluetoothSwitch = findViewById(R.id.switch_bluetooth)

        // Layouts for focus - find RelativeLayouts
        brightnessLayout = findViewById(R.id.brightness_layout)
        textSizeLayout = findViewById(R.id.text_size_layout)
        wifiLayout = findViewById(R.id.wifi_layout)
        bluetoothLayout = findViewById(R.id.bluetooth_layout)
        accountLayout = findViewById(R.id.account_layout)

        // Buttons
        resetAppButton = findViewById(R.id.btn_reset_app)
        backButton = findViewById(R.id.btn_back)
        saveButton = findViewById(R.id.btn_save)

        // Disable touch events for Vuzix compatibility
        brightnessLayout.isClickable = false
        textSizeLayout.isClickable = false
        wifiLayout.isClickable = false
        bluetoothLayout.isClickable = false
        accountLayout.isClickable = false
        resetAppButton.isClickable = false
        backButton.isClickable = false
        saveButton.isClickable = false

        brightnessLayout.isEnabled = false
        textSizeLayout.isEnabled = false
        wifiLayout.isEnabled = false
        bluetoothLayout.isEnabled = false
        accountLayout.isEnabled = false
        resetAppButton.isEnabled = false
        backButton.isEnabled = false
        saveButton.isEnabled = false

        // But keep them focusable for directional navigation
        brightnessLayout.isFocusable = true
        textSizeLayout.isFocusable = true
        wifiLayout.isFocusable = true
        bluetoothLayout.isFocusable = true
        accountLayout.isFocusable = true
        resetAppButton.isFocusable = true
        backButton.isFocusable = true
        saveButton.isFocusable = true

        // Set initial values (in a real app these would be pulled from preferences)
        accountEmailTextView.text = "technician@example.com"
        brightnessSeekBar.progress = 75
        textSizeSeekBar.progress = 50
        wifiSwitch.isChecked = true
        bluetoothSwitch.isChecked = false
    }
    
    /**
     * Set up settings controls and their listeners
     */
    private fun setupSettingsControls() {
        // Brightness seek bar listener
        brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // In a real app, would update screen brightness
                // For the prototype, we just log the value
                if (fromUser) {
                    // Would apply brightness change
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Toast.makeText(this@SettingsActivity, "Brightness adjusted", Toast.LENGTH_SHORT).show()
            }
        })

        brightnessLayout.setOnClickListener {
            // Handle brightness setting click
            Toast.makeText(this, "Brightness control activated", Toast.LENGTH_SHORT).show()
            // In a real app, might show a more usable brightness control
        }

        // Text size seek bar listener
        textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // In a real app, would update text size
                // For the prototype, we just log the value
                if (fromUser) {
                    // Would apply text size change
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Toast.makeText(this@SettingsActivity, "Text size adjusted", Toast.LENGTH_SHORT).show()
            }
        })

        textSizeLayout.setOnClickListener {
            // Handle text size setting click
            Toast.makeText(this, "Text size control activated", Toast.LENGTH_SHORT).show()
        }

        // Wi-Fi switch listener
        wifiSwitch.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Wi-Fi $status", Toast.LENGTH_SHORT).show()
            // In a real app, would enable/disable Wi-Fi
        }

        wifiLayout.setOnClickListener {
            // Toggle Wi-Fi switch
            wifiSwitch.isChecked = !wifiSwitch.isChecked
        }

        // Bluetooth switch listener
        bluetoothSwitch.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Bluetooth $status", Toast.LENGTH_SHORT).show()
            // In a real app, would enable/disable Bluetooth
        }

        bluetoothLayout.setOnClickListener {
            // Toggle Bluetooth switch
            bluetoothSwitch.isChecked = !bluetoothSwitch.isChecked
        }

        // Account layout
        accountLayout.setOnClickListener {
            // Show account details or change options
            Toast.makeText(this, "Account options", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Set up button click listeners
     */
    private fun setupButtons() {
        // Don't need to set click listeners - using hardware button navigation only
        // All buttons are already found in initializeUI()
    }

    /**
     * Update UI to highlight currently focused element
     */
    private fun updateFocusHighlight() {
        // Reset all backgrounds
        brightnessLayout.setBackgroundResource(R.color.cardBackground)
        textSizeLayout.setBackgroundResource(R.color.cardBackground)
        wifiLayout.setBackgroundResource(R.color.cardBackground)
        bluetoothLayout.setBackgroundResource(R.color.cardBackground)
        accountLayout.setBackgroundResource(R.color.cardBackground)
        resetAppButton.setBackgroundResource(R.color.cardBackground)
        backButton.setBackgroundResource(android.R.color.transparent)
        saveButton.setBackgroundResource(android.R.color.transparent)

        // Highlight current focus
        when (currentFocus) {
            0 -> brightnessLayout.setBackgroundResource(R.color.cardFocused)
            1 -> textSizeLayout.setBackgroundResource(R.color.cardFocused)
            2 -> wifiLayout.setBackgroundResource(R.color.cardFocused)
            3 -> bluetoothLayout.setBackgroundResource(R.color.cardFocused)
            4 -> accountLayout.setBackgroundResource(R.color.cardFocused)
            5 -> resetAppButton.setBackgroundResource(R.color.cardFocused)
            6 -> backButton.setBackgroundResource(R.color.cardFocused)
            7 -> saveButton.setBackgroundResource(R.color.cardFocused)
        }
    }

    /**
     * Handle Vuzix hardware navigation keys
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_VOLUME_UP -> {
                currentFocus = (currentFocus - 1 + 8) % 8
                updateFocusHighlight()
                true
            }

            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                currentFocus = (currentFocus + 1) % 8
                updateFocusHighlight()
                true
            }

            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                executeActionForCurrentFocus()
                true
            }

            KeyEvent.KEYCODE_BACK -> {
                finish() // Return to main menu
                true
            }

            else -> super.onKeyDown(keyCode, event)
        }
    }

    /**
     * Reset app settings
     */
    private fun resetApp() {
        Toast.makeText(this, "Reset functionality would be shown here", Toast.LENGTH_SHORT).show()
        // In a real app, would show confirmation dialog before resetting
    }

    /**
     * Execute action for currently focused element
     */
    private fun executeActionForCurrentFocus() {
        when (currentFocus) {
            0 -> updateBrightness() // Brightness
            1 -> updateTextSize() // Text size
            2 -> toggleWifi() // WiFi toggle
            3 -> toggleBluetooth() // Bluetooth toggle
            4 -> showAccountOptions() // Account settings
            5 -> resetApp() // Reset app
            6 -> finish() // Back button
            7 -> saveSettings() // Save button
        }
    }

    /**
     * Update brightness setting
     */
    private fun updateBrightness() {
        val currentValue = brightnessSeekBar.progress
        val newValue = (currentValue + 10).coerceAtMost(100) // Increase by 10%, max 100%
        brightnessSeekBar.progress = newValue
        Toast.makeText(this, "Brightness set to $newValue%", Toast.LENGTH_SHORT).show()
    }

    /**
     * Update text size setting
     */
    private fun updateTextSize() {
        val currentValue = textSizeSeekBar.progress
        val newValue = (currentValue + 10).coerceAtMost(100) // Increase by 10%, max 100%
        textSizeSeekBar.progress = newValue
        Toast.makeText(this, "Text size set to $newValue%", Toast.LENGTH_SHORT).show()
    }

    /**
     * Toggle WiFi setting
     */
    private fun toggleWifi() {
        wifiSwitch.isChecked = !wifiSwitch.isChecked
        val status = if (wifiSwitch.isChecked) "enabled" else "disabled"
        Toast.makeText(this, "Wi-Fi $status", Toast.LENGTH_SHORT).show()
    }

    /**
     * Toggle Bluetooth setting
     */
    private fun toggleBluetooth() {
        bluetoothSwitch.isChecked = !bluetoothSwitch.isChecked
        val status = if (bluetoothSwitch.isChecked) "enabled" else "disabled"
        Toast.makeText(this, "Bluetooth $status", Toast.LENGTH_SHORT).show()
    }

    /**
     * Show account options
     */
    private fun showAccountOptions() {
        Toast.makeText(this, "Account options", Toast.LENGTH_SHORT).show()
        // In a real app, would open account settings dialog or similar
    }

    /**
     * Save all settings
     */
    private fun saveSettings() {
        // In a real app, would save all settings to preferences
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        finish() // Return to previous screen
    }
}