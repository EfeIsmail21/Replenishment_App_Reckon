package edu.ap.be.replenishmachine.util

import android.app.Activity
import android.content.Context
import android.util.Log

/**
 * VuzixMenuHelper is a utility class that safely integrates with
 * Vuzix HUD components when available, and gracefully degrades
 * when they are not available.
 */
class VuzixMenuHelper(private val activity: Activity) {

    /**
     * Attempts to add an action to the Vuzix action menu.
     * Falls back gracefully if Vuzix components aren't available.
     *
     * @param actionName The name of the action to display
     * @param action The lambda to execute when the action is selected
     * @return True if the action was successfully added to the Vuzix menu, false otherwise
     */
    fun tryAddVuzixAction(actionName: String, action: () -> Unit): Boolean {
        return try {
            // Use reflection to avoid direct dependency which would fail compilation
            // on non-Vuzix devices or when the library isn't available
            val actionMenuClass = Class.forName("com.vuzix.hud.actionmenu.ActionMenu")
            val getInstance = actionMenuClass.getMethod("getInstance", Context::class.java)
            val actionMenu = getInstance.invoke(null, activity)
            
            val addAction = actionMenuClass.getMethod("addAction", String::class.java, kotlin.jvm.functions.Function0::class.java)
            addAction.invoke(actionMenu, actionName, action)
            
            Log.d("VuzixMenuHelper", "Successfully added action to Vuzix menu: $actionName")
            true
        } catch (e: Exception) {
            // Either the Vuzix library isn't available, or there was an error adding the action
            Log.d("VuzixMenuHelper", "Could not add action to Vuzix menu (normal if not on Vuzix device): ${e.message}")
            false
        }
    }
    
    /**
     * Check if the Vuzix HUD API is available on this device
     *
     * @return True if Vuzix HUD API is available, false otherwise
     */
    fun isVuzixHUDAvailable(): Boolean {
        return try {
            Class.forName("com.vuzix.hud.actionmenu.ActionMenu")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
    
    companion object {
        /**
         * Quick way to check if Vuzix HUD API is available without creating an instance
         *
         * @return True if Vuzix HUD API is available, false otherwise
         */
        fun isVuzixAvailable(): Boolean {
            return try {
                Class.forName("com.vuzix.hud.actionmenu.ActionMenu")
                true
            } catch (e: ClassNotFoundException) {
                false
            }
        }
    }
}