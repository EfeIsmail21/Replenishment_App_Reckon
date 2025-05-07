package edu.ap.be.replenishmachine.ui.machine.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.zxing.integration.android.IntentIntegrator
import com.vuzix.hud.actionmenu.ActionMenuActivity
import edu.ap.be.replenishmachine.R
import edu.ap.be.replenishmachine.auth.manager.AuthManager
import edu.ap.be.replenishmachine.auth.provider.ReckonAuthProvider
import edu.ap.be.replenishmachine.auth.token.storage.SharedPrefsTokenStorage
import edu.ap.be.replenishmachine.ui.machine.access.MachineAccessActivity
import edu.ap.be.replenishmachine.ui.machine.machine_list.MachineListActivity
import kotlinx.coroutines.launch
import org.json.JSONObject

class MachineMenu : ActionMenuActivity() {
    private lateinit var machineListMenuItem: MenuItem
    private lateinit var titleTextView: TextView
    private lateinit var organizationTextView: TextView
    private lateinit var authManager: AuthManager

    private val TAG = "MachineMenu"
    
    // Register activity launcher for QR code scanning
    private val qrCodeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val scanResult = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, result.resultCode, data)
            if (scanResult != null && scanResult.contents != null) {
                handleQrCodeResult(scanResult.contents)
            } else {
                Log.d(TAG, "QR scan cancelled or failed")
                Toast.makeText(this, "QR scan cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: MachineMenu started")
        setContentView(R.layout.machine_menu_layout)

        titleTextView = findViewById(R.id.machine_title)
        organizationTextView = findViewById(R.id.machine_organization)

        // Initialize AuthManager
        val tokenStorage = SharedPrefsTokenStorage(this)
        val authProvider = ReckonAuthProvider(
            secretEndpoint = "https://buybye-dev.reckon.ai/admin/test",
            userTokenEndpoint = "https://auth-dev.reckon.ai/app/send",
            authTokenEndpoint = "https://auth-dev.reckon.ai/app/send"
        )
        authManager = AuthManager(this, authProvider, tokenStorage)

        // Fetch user data to show organization
        fetchUserDataAndUpdateOrganization()
    }

    private fun fetchUserDataAndUpdateOrganization() {
        lifecycleScope.launch {
            try {
                val userData = authManager.getUserData()
                // Capitalize the organization name
                val capitalizedOrgName = userData.organizationName.capitalize()
                organizationTextView.text = "Organization: $capitalizedOrgName"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get organization data", e)
                organizationTextView.text = "Organization: Unknown"
            }
        }
    }

    override fun onCreateActionMenu(menu: Menu): Boolean {
        super.onCreateActionMenu(menu)
        Log.d(TAG, "onCreateActionMenu: Inflating menu")
        
        menuInflater.inflate(R.menu.machine_action_menu, menu)
        
        return true
    }

    override fun alwaysShowActionMenu(): Boolean {
        return true
    }
    
    override fun getActionMenuGravity(): Int {
        return Gravity.CENTER
    }
    
    override fun getDefaultAction(): Int {
        return 0 
    }

    fun scanQRCode(item: MenuItem?) {
        Log.d(TAG, "scanQRCode: Button clicked")
        Toast.makeText(this, "QR Code launching", Toast.LENGTH_SHORT).show()
        
        // Launch the QR code scanner using the standard Android integration
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan a machine QR code")
        integrator.setCameraId(0)  // Use default camera (usually rear)
        integrator.setBeepEnabled(false)
        integrator.setBarcodeImageEnabled(true)

        // Launch using the activity result launcher
        qrCodeLauncher.launch(integrator.createScanIntent())
    }

    private fun handleQrCodeResult(qrCodeContent: String) {
        Log.d(TAG, "QR code scanned: $qrCodeContent")

        try {
            // The QR code contains JSON data, parse it
            val jsonObject = JSONObject(qrCodeContent)

            // Extract the machine information
            val machineId = jsonObject.getString("_id")
            val machineCode = jsonObject.getString("code")
            val organization = jsonObject.getString("organization")
            // You might also want to extract createdAt if needed

            Log.d(TAG, "Parsed machine data - ID: $machineId, Code: $machineCode, Org: $organization")

            // Navigate to MachineAccessActivity with the parsed machine data
            val intent = Intent(this, MachineAccessActivity::class.java).apply {
                putExtra("machine_id", machineId)
                putExtra("machine_code", machineCode)
                putExtra("machine_organization", organization)
            }
            startActivity(intent)

            Log.d(TAG, "Navigating to MachineAccessActivity for machine: $machineCode")

        } catch (e: Exception) {
            Log.e(TAG, "Error processing QR code JSON: ${e.message}", e)
            Toast.makeText(this, "Error processing QR code: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun showListOfMachinesInOrganisation(item: MenuItem?) {
        Log.d(TAG, "showListOfMachinesInOrganisation: Button clicked")
        Toast.makeText(this, "Loading list of machines", Toast.LENGTH_SHORT).show()

        // Start the MachineListActivity
        startActivity(Intent(this, MachineListActivity::class.java))
    }
}