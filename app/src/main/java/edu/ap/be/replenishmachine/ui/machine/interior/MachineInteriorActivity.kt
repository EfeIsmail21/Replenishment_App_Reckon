package edu.ap.be.replenishmachine.ui.machine.interior

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.vuzix.hud.actionmenu.ActionMenuActivity
import edu.ap.be.replenishmachine.R

/**
 * Activity for interacting with a machine after it has been unlocked.
 * Provides options to scan products, search products, and go back.
 */
class MachineInteriorActivity : ActionMenuActivity() {
    private lateinit var machineIdTextView: TextView
    private lateinit var statusTextView: TextView
    
    private val TAG = "MachineInteriorActivity"
    private lateinit var machineId: String
    private lateinit var machineCode: String
    private lateinit var machineOrg: String
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_machine_unlocked) // Using our new layout file
            
            machineIdTextView = findViewById(R.id.machineIdTextView)
            statusTextView = findViewById(R.id.statusTextView)
            
            // Get machine details from intent
            machineId = intent.getStringExtra("machine_id") ?: ""
            machineCode = intent.getStringExtra("machine_code") ?: ""
            machineOrg = intent.getStringExtra("machine_organization") ?: ""
            
            Log.d(TAG, "onCreate: Got machine details - ID: $machineId, Code: $machineCode, Org: $machineOrg")
            
            if (machineId.isEmpty() || machineCode.isEmpty()) {
                Log.e(TAG, "No machine information provided")
                Toast.makeText(this, "Error: Machine information missing", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            
            // Display machine information
            machineIdTextView.text = "INSIDE: $machineCode ($machineOrg)"
            Log.d(TAG, "MachineInteriorActivity created for machine: $machineCode (ID: $machineId)")
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onCreate", e)
            Toast.makeText(this, "Error initializing view: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    override fun onCreateActionMenu(menu: Menu): Boolean {
        try {
            Log.d(TAG, "onCreateActionMenu: Starting to inflate menu")
            menuInflater.inflate(R.menu.machine_interior_menu, menu)
            Log.d(TAG, "onCreateActionMenu: Menu inflated successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onCreateActionMenu", e)
            return false
        }
    }
    
    override fun alwaysShowActionMenu(): Boolean {
        return true
    }
    
    override fun getActionMenuGravity(): Int {
        return Gravity.BOTTOM
    }
    
    fun scanProduct(item: MenuItem?) {
        Log.d(TAG, "scanProduct: Button clicked")
        Toast.makeText(this, "Scan Product functionality coming soon", Toast.LENGTH_SHORT).show()
        // Will implement later
    }
    
    fun searchProduct(item: MenuItem?) {
        Log.d(TAG, "searchProduct: Button clicked")
        Toast.makeText(this, "Search Product functionality coming soon", Toast.LENGTH_SHORT).show()
        // Will implement later
    }
    
    fun goBack(item: MenuItem?) {
        Log.d(TAG, "goBack: Button clicked")
        finish()
    }
}