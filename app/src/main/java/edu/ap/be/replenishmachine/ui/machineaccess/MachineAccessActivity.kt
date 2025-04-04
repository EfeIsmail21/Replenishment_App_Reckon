package edu.ap.be.replenishmachine.ui.machineaccess

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.ap.be.replenishmachine.R
import edu.ap.be.replenishmachine.ui.mainmenu.MainMenuActivity
import edu.ap.be.replenishmachine.ui.machinedetails.MachineDetailsActivity

/**
 * MachineAccessActivity displays a list of available machines for the user to connect to.
 * Following Vuzix AR design principles for optimal viewing on AR glasses.
 */
class MachineAccessActivity : AppCompatActivity() {

    private lateinit var machine1Layout: FrameLayout
    private lateinit var machine2Layout: FrameLayout
    private lateinit var machine3Layout: FrameLayout
    private lateinit var backButton: FrameLayout
    private lateinit var refreshButton: FrameLayout

    // Current focus index (0-2 for machines, 3 for back button, 4 for refresh button)
    private var currentFocus = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.machine_access_view)
        
        setupMachineConnections()
        setupNavigation()

        // Initial focus highlight
        updateFocusHighlight()
    }
    
    /**
     * Set up machine connection buttons and their behaviors
     */
    private fun setupMachineConnections() {
        // Get layout references
        machine1Layout = findViewById(R.id.machine_item_1)
        machine2Layout = findViewById(R.id.machine_item_2)
        machine3Layout = findViewById(R.id.machine_item_3)

        // Disable touch events for all elements for Vuzix compatibility
        machine1Layout.isClickable = false
        machine2Layout.isClickable = false
        machine3Layout.isClickable = false
        machine1Layout.isEnabled = false
        machine2Layout.isEnabled = false
        machine3Layout.isEnabled = false

        // But keep them focusable for directional navigation
        machine1Layout.isFocusable = true
        machine2Layout.isFocusable = true
        machine3Layout.isFocusable = true

        // Log element IDs for debugging
        Log.d("MachineAccessActivity", "Machine1Layout ID: ${machine1Layout.id}")
        Log.d("MachineAccessActivity", "Machine2Layout ID: ${machine2Layout.id}")
        Log.d("MachineAccessActivity", "Machine3Layout ID: ${machine3Layout.id}")
    }
    
    /**
     * Set up navigation buttons (back and refresh)
     */
    private fun setupNavigation() {
        backButton = findViewById(R.id.btn_back_frame)
        refreshButton = findViewById(R.id.btn_refresh_frame)

        // Disable touch events for Vuzix compatibility
        backButton.isClickable = false
        refreshButton.isClickable = false
        backButton.isEnabled = false
        refreshButton.isEnabled = false

        // But keep them focusable for directional navigation
        backButton.isFocusable = true
        refreshButton.isFocusable = true

        // Log element IDs for debugging
        Log.d("MachineAccessActivity", "BackButton ID: ${backButton.id}")
        Log.d("MachineAccessActivity", "RefreshButton ID: ${refreshButton.id}")
    }

    /**
     * Update UI to highlight the currently focused element
     */
    private fun updateFocusHighlight() {
        // Reset background colors
        machine1Layout.setBackgroundResource(R.color.cardBackground)
        machine2Layout.setBackgroundResource(R.color.cardBackground)
        machine3Layout.setBackgroundResource(R.color.cardBackground)
        backButton.setBackgroundResource(android.R.color.transparent)
        refreshButton.setBackgroundResource(android.R.color.transparent)

        // Find all text elements
        val machine1Title = machine1Layout.findViewById<TextView>(R.id.machine_name_1)
        val machine2Title = machine2Layout.findViewById<TextView>(R.id.machine_name_2)
        val machine3Title = machine3Layout.findViewById<TextView>(R.id.machine_name_3)
        val backText = backButton.findViewById<TextView>(R.id.back_text)
        val refreshText = refreshButton.findViewById<TextView>(R.id.refresh_text)

        // Reset all text colors
        machine1Title?.setTextColor(getColor(R.color.colorPrimary))
        machine2Title?.setTextColor(getColor(R.color.colorPrimary))
        machine3Title?.setTextColor(getColor(R.color.colorPrimary))
        backText?.setTextColor(getColor(android.R.color.white))
        refreshText?.setTextColor(getColor(R.color.colorPrimary))

        // Highlight current focus
        when (currentFocus) {
            0 -> {
                machine1Layout.setBackgroundResource(R.color.cardFocused)
                machine1Title?.setTextColor(getColor(R.color.colorPrimary))
            }

            1 -> {
                machine2Layout.setBackgroundResource(R.color.cardFocused)
                machine2Title?.setTextColor(getColor(R.color.colorPrimary))
            }

            2 -> {
                machine3Layout.setBackgroundResource(R.color.cardFocused)
                machine3Title?.setTextColor(getColor(R.color.colorPrimary))
            }

            3 -> {
                backButton.setBackgroundResource(R.color.cardFocused)
                backText?.setTextColor(getColor(R.color.colorPrimary))
            }

            4 -> {
                refreshButton.setBackgroundResource(R.color.cardFocused)
                refreshText?.setTextColor(getColor(R.color.colorPrimary))
            }
        }
    }

    /**
     * Execute action for currently focused element
     */
    private fun executeActionForCurrentFocus() {
        when (currentFocus) {
            0 -> connectToMachine(1)
            1 -> connectToMachine(2)
            2 -> connectToMachine(3) // Disabled in UI but handled for completeness
            3 -> finish() // Back button
            4 -> refreshMachineList() // Refresh button
        }
    }

    /**
     * Connect to a specific machine
     */
    private fun connectToMachine(machineId: Int) {
        if (machineId == 3) {
            // Machine 3 is disabled for this demo
            Toast.makeText(this, "Machine 3 is currently unavailable", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Connecting to Replenish Station $machineId...", Toast.LENGTH_SHORT)
            .show()
        val intent = Intent(this, MachineDetailsActivity::class.java)
        intent.putExtra("MACHINE_ID", machineId.toString())
        intent.putExtra("MACHINE_NAME", "REPLENISH STATION $machineId")
        startActivity(intent)
    }

    /**
     * Refresh the machine list
     */
    private fun refreshMachineList() {
        Toast.makeText(this, "Refreshing machine list...", Toast.LENGTH_SHORT).show()
        // In a real app, would rescan for available machines
    }

    /**
     * Handle Vuzix hardware key navigation
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_VOLUME_UP -> {
                currentFocus = (currentFocus - 1 + 5) % 5
                updateFocusHighlight()
                true
            }

            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                currentFocus = (currentFocus + 1) % 5
                updateFocusHighlight()
                true
            }

            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                executeActionForCurrentFocus()
                true
            }

            KeyEvent.KEYCODE_BACK -> {
                finish() // Go back to main menu
                true
            }

            else -> super.onKeyDown(keyCode, event)
        }
    }
}