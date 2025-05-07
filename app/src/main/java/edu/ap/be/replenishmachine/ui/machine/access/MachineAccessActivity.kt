package edu.ap.be.replenishmachine.ui.machine.access

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.vuzix.hud.actionmenu.ActionMenuActivity
import edu.ap.be.replenishmachine.R
import edu.ap.be.replenishmachine.auth.manager.AuthManager
import edu.ap.be.replenishmachine.auth.provider.ReckonAuthProvider
import edu.ap.be.replenishmachine.auth.token.storage.SharedPrefsTokenStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import okhttp3.RequestBody.Companion.toRequestBody
import edu.ap.be.replenishmachine.ui.machine.interior.MachineInteriorActivity

class MachineAccessActivity : ActionMenuActivity() {
    private lateinit var machineIdTextView: TextView
    private lateinit var loadingView: View
    
    private val TAG = "MachineAccessActivity"
    private lateinit var machineId: String
    private var machineCode: String = ""
    private var machineOrg: String = ""
    private lateinit var authManager: AuthManager
    private var isLoading = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_machine_access)
        
        machineIdTextView = findViewById(R.id.machineIdTextView)
        loadingView = findViewById(R.id.loadingView)
        
        authManager = AuthManager(
            this,
            ReckonAuthProvider(
                secretEndpoint = "https://buybye-dev.reckon.ai/admin/test",
                userTokenEndpoint = "https://auth-dev.reckon.ai/app/send",
                authTokenEndpoint = "https://auth-dev.reckon.ai/app/send"
            ),
            SharedPrefsTokenStorage(this)
        )
        
        machineId = intent.getStringExtra("machine_id") ?: ""
        machineCode = intent.getStringExtra("machine_code") ?: ""
        machineOrg = intent.getStringExtra("machine_organization") ?: ""

        if (machineId.isEmpty()) {
            Log.e(TAG, "No machine ID provided to MachineAccessActivity")
            Toast.makeText(this, "Error: No machine selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        if (machineCode.isNotEmpty() && machineOrg.isNotEmpty()) {
            displayMachineInfo()
        } else {
            loadMachineDetails()
        }
    }
    
    private fun loadMachineDetails() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val machines = authManager.getMachinesForOrganization()
                val machine = machines.find { it.id == machineId }
                
                if (machine != null) {
                    machineCode = machine.code
                    machineOrg = machine.organization
                    displayMachineInfo()
                } else {
                    Log.e(TAG, "Machine with ID $machineId not found")
                    Toast.makeText(this@MachineAccessActivity, "Error: Machine not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading machine details", e)
                Toast.makeText(this@MachineAccessActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun displayMachineInfo() {
        val displayText = getString(R.string.machine_id_format, "$machineCode ($machineOrg)")
        machineIdTextView.text = displayText 
        Log.d(TAG, "Machine details displayed - ID: $machineId, Code: $machineCode, Org: $machineOrg")
    }

    private fun showLoading(isLoadingState: Boolean) {
        this.isLoading = isLoadingState
        if (isLoadingState) {
            loadingView.visibility = View.VISIBLE
            machineIdTextView.visibility = View.GONE
        } else {
            loadingView.visibility = View.GONE
            machineIdTextView.visibility = View.VISIBLE
        }
        invalidateOptionsMenu() // Force menu to redraw
    }

    override fun onCreateActionMenu(menu: Menu): Boolean {
        super.onCreateActionMenu(menu)
        Log.d(TAG, "onCreateActionMenu: Inflating menu")

        menuInflater.inflate(R.menu.machine_access_menu, menu)
        
        return true
    }

    override fun alwaysShowActionMenu(): Boolean {
        return !isLoading
    }
    
    override fun getActionMenuGravity(): Int {
        return Gravity.CENTER
    }
    
    fun lockMachine(item: MenuItem?) {
        Log.d(TAG, "lockMachine: Button clicked for machine $machineCode")
        Toast.makeText(this, "Locking machine $machineCode", Toast.LENGTH_SHORT).show()
    }
    
    fun goBack(item: MenuItem?) {
        Log.d(TAG, "goBack: Button clicked")
        finish()
    }

    fun unlockMachine(item: MenuItem?) {
        Log.d(TAG, "unlockMachine: Button clicked for machine $machineCode (ID: $machineId)")
        Toast.makeText(this, "Unlocking machine $machineCode...", Toast.LENGTH_SHORT).show()
        
        showLoading(true)
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "unlockMachine: Getting authentication credentials")
                val credentials = authManager.getValidCredentials()
                Log.d(TAG, "unlockMachine: Got valid credentials")
                
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

                val mediaType = "text/plain".toMediaType()
                val body = "".toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("https://buybye-dev.reckon.ai/backoffice/v2/OpenBuyByeToReplenish/$machineId")
                    .put(body)
                    .addHeader("authtoken", credentials.authToken)
                    .addHeader("usertoken", credentials.userToken)
                    .addHeader("identity", credentials.userData.entityId)
                    .build()

                Log.d(TAG, "unlockMachine: Request URL: https://buybye-dev.reckon.ai/backoffice/v2/OpenBuyByeToReplenish/$machineId")
                Log.d(TAG, "unlockMachine: authtoken: ${credentials.authToken.take(20)}...")
                Log.d(TAG, "unlockMachine: usertoken: ${credentials.userToken.take(20)}...")
                Log.d(TAG, "unlockMachine: identity: ${credentials.userData.entityId}")

                val response = client.newCall(request).execute()
                val responseCode = response.code
                val responseBody = response.body?.string() ?: ""

                Log.d(TAG, "unlockMachine: Response code: $responseCode")
                Log.d(TAG, "unlockMachine: Response body: $responseBody")

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (responseCode in 200..299) {
                        Log.d(TAG, "unlockMachine: Successfully unlocked machine $machineCode")
                        Toast.makeText(this@MachineAccessActivity, "Machine $machineCode unlocked successfully!", Toast.LENGTH_SHORT).show()

                        navigateToMachineInteriorMenu()
                    } else {
                        Log.e(TAG, "unlockMachine: Failed to unlock machine. Response code: $responseCode")
                        Toast.makeText(
                            this@MachineAccessActivity,
                            "Failed to unlock machine: ${parseErrorResponse(responseBody)}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "unlockMachine: Exception while unlocking machine", e)

                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(
                        this@MachineAccessActivity,
                        "Error unlocking machine: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun parseErrorResponse(responseBody: String): String {
        return try {
            val jsonObject = JSONObject(responseBody)
            if (jsonObject.has("errorMessage")) {
                jsonObject.getString("errorMessage")
            } else {
                "Unknown error"
            }
        } catch (e: Exception) {
            "Unable to parse error response"
        }
    }

    private fun navigateToMachineInteriorMenu() {
        val intent = Intent(this, MachineInteriorActivity::class.java).apply {
            putExtra("machine_id", machineId)
            putExtra("machine_code", machineCode)
            putExtra("machine_organization", machineOrg)
        }
        startActivity(intent)
    }
}