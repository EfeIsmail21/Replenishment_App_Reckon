package edu.ap.be.replenishmachine.ui.machinedetails

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.ap.be.replenishmachine.R

/**
 * MachineDetailsActivity shows detailed information about a connected replenishment machine.
 * Follows Vuzix AR design principles for optimal viewing on AR glasses.
 */
class MachineDetailsActivity : AppCompatActivity() {
    
    // UI components
    private lateinit var temperatureCard: RelativeLayout
    private lateinit var pressureCard: RelativeLayout
    private lateinit var part1Card: RelativeLayout
    private lateinit var part2Card: RelativeLayout
    private lateinit var refillButton: Button
    private lateinit var backButton: Button
    private lateinit var arViewButton: Button
    
    // Current focus (0=temp, 1=pressure, 2=part1, 3=part2, 4=refill, 5=back, 6=ar)
    private var currentFocus = 0
    
    // Machine data
    private var machineId: String = "1"
    private var machineName: String = "REPLENISH STATION 1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.machine_details_view)
        
        // Get machine details from intent
        machineId = intent.getStringExtra("MACHINE_ID") ?: "1"
        machineName = intent.getStringExtra("MACHINE_NAME") ?: "REPLENISH STATION 1"
        
        // Initialize UI components
        initializeUI()
        
        // Setup click handlers
        setupClickHandlers()
        
        // Initial focus highlight
        updateFocusHighlight()
    }
    
    /**
     * Initialize UI components and their initial values
     */
    private fun initializeUI() {
        // Update header with machine name
        val headerTitle = findViewById<TextView>(R.id.tv_header_title)
        headerTitle.text = machineName
        
        // Get card references
        temperatureCard = findViewById(R.id.temperature_card)
        pressureCard = findViewById(R.id.pressure_card)
        part1Card = findViewById(R.id.part1_card)
        part2Card = findViewById(R.id.part2_card)
        
        // Buttons
        refillButton = findViewById(R.id.btn_refill)
        backButton = findViewById(R.id.btn_back)
        arViewButton = findViewById(R.id.btn_toggle_ar)

        // Disable touch events for Vuzix compatibility
        temperatureCard.isClickable = false
        pressureCard.isClickable = false
        part1Card.isClickable = false
        part2Card.isClickable = false
        refillButton.isClickable = false
        backButton.isClickable = false
        arViewButton.isClickable = false

        temperatureCard.isEnabled = false
        pressureCard.isEnabled = false
        part1Card.isEnabled = false
        part2Card.isEnabled = false
        refillButton.isEnabled = false
        backButton.isEnabled = false
        arViewButton.isEnabled = false

        // But keep them focusable for directional navigation
        temperatureCard.isFocusable = true
        pressureCard.isFocusable = true
        part1Card.isFocusable = true
        part2Card.isFocusable = true
        refillButton.isFocusable = true
        backButton.isFocusable = true
        arViewButton.isFocusable = true

        // Load machine data (would come from API in a real app)
        loadMachineData()
    }
    
    /**
     * Load data for the selected machine
     */
    private fun loadMachineData() {
        // In a real app, this would fetch data from an API
        // For demo purposes, we're using hardcoded values
        val tempValue = findViewById<TextView>(R.id.tv_temp_value)
        val pressureValue = findViewById<TextView>(R.id.tv_pressure_value)
        val part1Value = findViewById<TextView>(R.id.tv_part1_value)
        val part2Value = findViewById<TextView>(R.id.tv_part2_value)
        
        if (machineId == "1") {
            tempValue.text = "24.5°C"
            pressureValue.text = "5.2 bar"
            part1Value.text = "87%"
            part2Value.text = "12%"
        } else {
            tempValue.text = "23.8°C"
            pressureValue.text = "4.9 bar"
            part1Value.text = "62%"
            part2Value.text = "45%"
        }
    }
    
    /**
     * Set up click handlers for UI elements
     */
    private fun setupClickHandlers() {
        // With Vuzix, button actions will be handled through key events, not touch
    }

    /**
     * Execute action for currently focused element
     */
    private fun executeActionForCurrentFocus() {
        when (currentFocus) {
            0 -> showTemperatureDetails()
            1 -> showPressureDetails()
            2 -> showPart1Details()
            3 -> showPart2Details()
            4 -> startRefill()
            5 -> finish() // Back button
            6 -> toggleArView() // AR View
        }
    }
    
    /**
     * Show temperature history details
     */
    private fun showTemperatureDetails() {
        Toast.makeText(this, "Temperature history will be shown here", Toast.LENGTH_SHORT).show()
    }

    /**
     * Show pressure history details
     */
    private fun showPressureDetails() {
        Toast.makeText(this, "Pressure history will be shown here", Toast.LENGTH_SHORT).show()
    }

    /**
     * Show part 1 details
     */
    private fun showPart1Details() {
        Toast.makeText(this, "Component A-113 details will be shown here", Toast.LENGTH_SHORT)
            .show()
    }

    /**
     * Show part 2 details
     */
    private fun showPart2Details() {
        Toast.makeText(this, "Component B-227 details will be shown here", Toast.LENGTH_SHORT)
            .show()
    }

    /**
     * Start refill process
     */
    private fun startRefill() {
        Toast.makeText(this, "Starting refill process for Component B-227", Toast.LENGTH_SHORT)
            .show()
        // In a real app, would initiate the refill process
    }

    /**
     * Toggle AR view
     */
    private fun toggleArView() {
        Toast.makeText(this, "Activating AR view of machine internals", Toast.LENGTH_SHORT).show()
        // In a real app, would activate AR overlay
    }

    /**
     * Update UI to highlight the currently focused element
     */
    private fun updateFocusHighlight() {
        // Reset background colors
        temperatureCard.setBackgroundResource(R.color.cardBackground)
        pressureCard.setBackgroundResource(R.color.cardBackground)
        part1Card.setBackgroundResource(R.color.cardBackground)
        part2Card.setBackgroundResource(R.color.cardBackground)
        refillButton.setBackgroundResource(R.color.colorPrimary)
        backButton.setBackgroundResource(android.R.color.transparent)
        arViewButton.setBackgroundResource(android.R.color.transparent)
        
        // Highlight current focus
        when (currentFocus) {
            0 -> temperatureCard.setBackgroundResource(R.color.cardFocused)
            1 -> pressureCard.setBackgroundResource(R.color.cardFocused)
            2 -> part1Card.setBackgroundResource(R.color.cardFocused)
            3 -> part2Card.setBackgroundResource(R.color.cardFocused)
            4 -> refillButton.setBackgroundResource(R.color.colorPrimaryDark)
            5 -> backButton.setBackgroundResource(R.color.cardFocused)
            6 -> arViewButton.setBackgroundResource(R.color.cardFocused)
        }
    }
    
    /**
     * Handle Vuzix hardware navigation keys
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_VOLUME_UP -> {
                currentFocus = (currentFocus - 1 + 7) % 7
                updateFocusHighlight()
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                currentFocus = (currentFocus + 1) % 7
                updateFocusHighlight()
                true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                executeActionForCurrentFocus()
                true
            }
            KeyEvent.KEYCODE_BACK -> {
                finish() // Return to machine access screen
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}