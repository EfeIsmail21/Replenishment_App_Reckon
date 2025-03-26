package edu.ap.be.replenishmachine.auth.manager

import android.content.Context
import android.util.Base64

/**
 * Manages user login credentials storage and retrieval securely.
 * Provides methods to save, retrieve, and clear user email and password.
 */
class CredentialsManager(context: Context) {
    private val prefs = context.getSharedPreferences("UserCredentials", Context.MODE_PRIVATE)

    /**
     * Saves user credentials using Base64 encoding.
     *
     * @param email User's email address
     * @param password User's password
     */
    fun saveCredentials(email: String, password: String) {
        prefs.edit().apply {
            putString("email", Base64.encodeToString(email.toByteArray(), Base64.DEFAULT))
            putString("password", Base64.encodeToString(password.toByteArray(), Base64.DEFAULT))
        }.apply()
    }

    /**
     * Retrieves the saved email address.
     *
     * @return Decoded email address or null if not found
     */
    fun getEmail(): String? {
        val encodedEmail = prefs.getString("email", null)
        return encodedEmail?.let { String(Base64.decode(it, Base64.DEFAULT)) }
    }

    /**
     * Retrieves the saved password.
     *
     * @return Decoded password or null if not found
     */
    fun getPassword(): String? {
        val encodedPassword = prefs.getString("password", null)
        return encodedPassword?.let { String(Base64.decode(it, Base64.DEFAULT)) }
    }

    /**
     * Clears all stored credentials.
     */
    fun clearCredentials() {
        prefs.edit().clear().apply()
    }

    /**
     * Checks if credentials are stored.
     *
     * @return True if both email and password exist, false otherwise
     */
    fun hasCredentials(): Boolean {
        return !prefs.getString("email", null).isNullOrBlank() &&
                !prefs.getString("password", null).isNullOrBlank()
    }
}