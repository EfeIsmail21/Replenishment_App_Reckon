package edu.ap.be.replenishmachine.ui.machine

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.vuzix.hud.actionmenu.ActionMenuActivity
import edu.ap.be.replenishmachine.R

class MachineMenu : ActionMenuActivity() {
    private val TAG = "MachineMenu"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_machine_menu)
        
        Log.d(TAG, "onCreate: MachineMenu started")
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

    }
}