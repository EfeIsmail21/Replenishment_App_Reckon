package edu.ap.be.replenishmachine.auth.manager

import android.content.Context
import android.util.Log
import edu.ap.be.replenishmachine.model.Machine
import edu.ap.be.replenishmachine.model.UserData
import edu.ap.be.replenishmachine.auth.provider.AuthenticationProvider
import edu.ap.be.replenishmachine.auth.provider.ReckonAuthProvider
import edu.ap.be.replenishmachine.auth.token.AuthenticationCredentials
import edu.ap.be.replenishmachine.auth.token.storage.TokenStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Central manager class for authentication operations.
 * Handles user login, token generation, and authentication credential management.
 */
class AuthManager(
    private val context: Context,
    private val authProvider: AuthenticationProvider,
    private val tokenStorage: TokenStorage
) {
    private val credentialsManager = CredentialsManager(context)

    /**
     * Authenticates a user with email and password.
     * Stores credentials, obtains authentication tokens and user data.
     *
     * @param email User's email address
     * @param password User's password
     * @return Authentication credentials containing tokens and user information
     */
    suspend fun login(email: String, password: String): AuthenticationCredentials {
        // Store user credentials securely for future use
        Log.d("AuthManager", "Storing credentials for email: $email")
        credentialsManager.saveCredentials(email, password)

        // Authentication
        Log.d("AuthManager", "Attaining secret")
        val secret = authProvider.obtainSecret()
        Log.d("AuthManager", "Secret obtained successfully")

        // Get user data and token in a single call
        Log.d("AuthManager", "Requesting user data")
        val userDataResponse = authProvider.generateUserData(secret, email, password)
        Log.d("AuthManager", "User data received for: ${userDataResponse.userData.username}")

        // Generate auth token using user token
        Log.d("AuthManager", "Generating auth token using user token")
        val authToken = authProvider.generateAuthToken(userDataResponse.userToken)
        Log.d("AuthManager", "Auth token generated successfully")

        val credentials = AuthenticationCredentials(
            secret = secret,
            userToken = userDataResponse.userToken,
            authToken = authToken,
            userData = userDataResponse.userData,
            expiresAt = System.currentTimeMillis() + AUTH_TOKEN_EXPIRATION,
            email = email,
            password = password
        )

        // Save all credentials for future use
        tokenStorage.saveCredentials(credentials)

        return credentials
    }

    /**
     * Refreshes authentication credentials without requiring login.
     * Only updates the secret and auth token while preserving user token and data.
     *
     * @return Updated authentication credentials with refreshed tokens
     */
    suspend fun refreshAuthentication(): AuthenticationCredentials = withContext(Dispatchers.IO) {
        val currentCredentials = tokenStorage.getCurrentCredentials()

        // We only need to refresh the auth token and secret
        val newSecret = authProvider.obtainSecret()
        val newAuthToken = authProvider.generateAuthToken(currentCredentials.userToken)

        val refreshedCredentials = currentCredentials.copy(
            secret = newSecret,
            authToken = newAuthToken,
            expiresAt = System.currentTimeMillis() + AUTH_TOKEN_EXPIRATION
        )

        tokenStorage.saveCredentials(refreshedCredentials)
        return@withContext refreshedCredentials
    }

    /**
     * Retrieves valid authentication credentials, automatically refreshing if expired.
     *
     * @return Valid authentication credentials
     */
    suspend fun getValidCredentials(): AuthenticationCredentials {
        val currentCredentials = tokenStorage.getCurrentCredentials()
        return if (isCredentialsExpired(currentCredentials)) {
            Log.d("AuthManager", "getValidCredentials: Auth token expired, refreshing")
            refreshAuthentication()
        } else {
            Log.d("AuthManager", "getValidCredentials: Using existing auth token")
            currentCredentials
        }
    }

    /**
     * Returns user data without checking if credentials are expired.
     *
     * @return User data object containing user information
     */
    suspend fun getUserData(): UserData {
        return tokenStorage.getCurrentCredentials().userData
    }

    /**
     * Checks if the user is currently logged in.
     *
     * @return True if user is logged in, false otherwise
     */
    suspend fun isLoggedIn(): Boolean {
        return try {
            tokenStorage.getCurrentCredentials()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Logs out the user by clearing all stored credentials.
     */
    suspend fun logout() {
        tokenStorage.clearCredentials()
        credentialsManager.clearCredentials()
    }

    /**
     * Checks if authentication credentials have expired.
     *
     * @param credentials The authentication credentials to check
     * @return True if credentials are expired, false otherwise
     */
    private fun isCredentialsExpired(credentials: AuthenticationCredentials): Boolean {
        // Add a small buffer (30 seconds) to avoid edge cases
        val bufferTime = 30 * 1000L
        return credentials.expiresAt <= (System.currentTimeMillis() + bufferTime)
    }

    /**
     * Retrieves machines for the organization of the logged-in user.
     * Automatically handles token refreshing if needed.
     *
     * @return List of machines in the user's organization
     */
    suspend fun getMachinesForOrganization(): List<Machine> {
        // First ensure we have valid credentials
        Log.d("AuthManager", "getMachinesForOrganization: Getting valid credentials")
        val credentials = getValidCredentials()
        Log.d(
            "AuthManager",
            "getMachinesForOrganization: Got valid credentials for user: ${credentials.userData.username}"
        )
        Log.d(
            "AuthManager",
            "getMachinesForOrganization: Using organization ID: ${credentials.userData.organizationId}"
        )
        Log.d(
            "AuthManager",
            "getMachinesForOrganization: Using entity ID: ${credentials.userData.entityId}"
        )

        // Get organization ID from the user data
        val organizationId = credentials.userData.organizationId
        val entityId = credentials.userData.entityId

        Log.d(
            "AuthManager",
            "getMachinesForOrganization: Calling ReckonAuthProvider.getMachinesForOrganization"
        )
        Log.d("AuthManager", "getMachinesForOrganization: organizationId=$organizationId")
        Log.d("AuthManager", "getMachinesForOrganization: entityId=$entityId")
        Log.d("AuthManager", "getMachinesForOrganization: secret=${credentials.secret.take(5)}...")
        Log.d(
            "AuthManager",
            "getMachinesForOrganization: authToken=${credentials.authToken.take(5)}..."
        )
        Log.d(
            "AuthManager",
            "getMachinesForOrganization: userToken=${credentials.userToken.take(5)}..."
        )

        // Add a check to see if any tokens are empty
        if (credentials.secret.isEmpty()) {
            Log.e("AuthManager", "getMachinesForOrganization: ERROR - secret token is empty!")
        }
        if (credentials.authToken.isEmpty()) {
            Log.e("AuthManager", "getMachinesForOrganization: ERROR - auth token is empty!")
        }
        if (credentials.userToken.isEmpty()) {
            Log.e("AuthManager", "getMachinesForOrganization: ERROR - user token is empty!")
        }
        if (entityId.isEmpty()) {
            Log.e("AuthManager", "getMachinesForOrganization: ERROR - entity ID is empty!")
        }

        return try {
            val machines = (authProvider as ReckonAuthProvider).getMachinesForOrganization(
                organizationId = organizationId,
                secret = credentials.secret,
                authToken = credentials.authToken,
                userToken = credentials.userToken,
                entityId = entityId
            )
            Log.d(
                "AuthManager",
                "getMachinesForOrganization: Successfully retrieved ${machines.size} machines"
            )
            machines
        } catch (e: Exception) {
            Log.e("AuthManager", "getMachinesForOrganization: Error retrieving machines", e)
            Log.e("AuthManager", "getMachinesForOrganization: Error message: ${e.message}")
            Log.e(
                "AuthManager",
                "getMachinesForOrganization: Stack trace: ${e.stackTraceToString()}"
            )
            throw e
        }
    }

    /**
     * Retrieves stocks information for a specific machine.
     *
     * @param machineId The ID of the machine to get stocks for
     * @return JSONObject containing the machine's stock information
     */
    suspend fun getMachineStocks(machineId: String): JSONObject {
        // First ensure we have valid credentials
        Log.d("AuthManager", "getMachineStocks: Getting valid credentials")
        val credentials = getValidCredentials()
        Log.d("AuthManager", "getMachineStocks: Got valid credentials")

        val entityId = credentials.userData.entityId

        Log.d("AuthManager", "getMachineStocks: Calling ReckonAuthProvider.getMachineStocks")
        Log.d("AuthManager", "getMachineStocks: machineId=$machineId")
        Log.d("AuthManager", "getMachineStocks: entityId=$entityId")

        return try {
            (authProvider as ReckonAuthProvider).getMachineStocks(
                machineId = machineId,
                authToken = credentials.authToken,
                userToken = credentials.userToken,
                entityId = entityId
            )
        } catch (e: Exception) {
            Log.e("AuthManager", "getMachineStocks: Error retrieving machine stocks", e)
            throw e
        }
    }

    companion object {
        // Auth token expires every 15 minutes
        private const val AUTH_TOKEN_EXPIRATION = 15 * 60 * 1000L
    }
}