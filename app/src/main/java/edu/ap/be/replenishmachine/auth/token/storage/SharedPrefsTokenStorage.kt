package edu.ap.be.replenishmachine.auth.token.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import edu.ap.be.replenishmachine.model.CreatedBy
import edu.ap.be.replenishmachine.model.UserData
import edu.ap.be.replenishmachine.model.UserRole
import edu.ap.be.replenishmachine.auth.token.AuthenticationCredentials
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SharedPrefsTokenStorage(private val context: Context) : TokenStorage {
    private val TAG = "SharedPrefsTokenStorage"
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    override suspend fun saveCredentials(credentials: AuthenticationCredentials): Unit =
        withContext(Dispatchers.IO) {
            Log.d(
                TAG,
                "saveCredentials: Saving credentials for user: ${credentials.userData.username}"
            )
            prefs.edit().apply {
                putString("secret", credentials.secret)
                putString("userToken", credentials.userToken)
                putString("authToken", credentials.authToken)
                
                // Store user data in JSON format
                val userData = JSONObject().apply {
                    put("userId", credentials.userData.userId)
                    put("username", credentials.userData.username)
                    put("email", credentials.userData.email)
                    put("locale", credentials.userData.locale)
                    
                    val createdBy = JSONObject().apply {
                        put("id", credentials.userData.createdBy.id)
                        put("username", credentials.userData.createdBy.username)
                    }
                    put("createdBy", createdBy)
                    
                    put("active", credentials.userData.active)
                    put("createdAt", credentials.userData.createdAt)
                    put("updatedAt", credentials.userData.updatedAt)
                    put("toDelete", credentials.userData.toDelete)
                    put("currency", credentials.userData.currency)
                    put("entityId", credentials.userData.entityId)
                    put("entityName", credentials.userData.entityName)
                    put("organizationId", credentials.userData.organizationId)
                    put("organizationName", credentials.userData.organizationName)
                    
                    val role = JSONObject().apply {
                        put("id", credentials.userData.role.id)
                        put("role", credentials.userData.role.role)
                        put("name", credentials.userData.role.name)
                    }
                    put("role", role)
                }
                
                putString("userData", userData.toString())
                putLong("expiresAt", credentials.expiresAt)
                putString("email", credentials.email)
                putString("password", credentials.password)
                apply()
            }
            Log.d(TAG, "saveCredentials: Credentials saved successfully")
        }

    override suspend fun getCurrentCredentials(): AuthenticationCredentials = withContext(Dispatchers.IO) {
        Log.d(TAG, "getCurrentCredentials: Retrieving credentials from SharedPreferences")

        if (!prefs.contains("userToken")) {
            Log.e(TAG, "getCurrentCredentials: No userToken found in SharedPreferences")
            throw IllegalStateException("No credentials available")
        }

        try {
            val userDataString = prefs.getString("userData", "{}") ?: "{}"
            Log.d(
                TAG,
                "getCurrentCredentials: Retrieved user data string: ${userDataString.take(50)}..."
            )

            val userDataJson = JSONObject(userDataString)
            val createdByJson = userDataJson.getJSONObject("createdBy")
            val roleJson = userDataJson.getJSONObject("role")

            val organizationId = userDataJson.getString("organizationId")
            Log.d(TAG, "getCurrentCredentials: Retrieved organization ID: $organizationId")

            val userData = UserData(
                userId = userDataJson.getString("userId"),
                username = userDataJson.getString("username"),
                email = userDataJson.getString("email"),
                locale = userDataJson.getString("locale"),
                createdBy = CreatedBy(
                    id = createdByJson.getString("id"),
                    username = createdByJson.getString("username")
                ),
                active = userDataJson.getBoolean("active"),
                createdAt = userDataJson.getString("createdAt"),
                updatedAt = userDataJson.getString("updatedAt"),
                toDelete = userDataJson.getBoolean("toDelete"),
                currency = userDataJson.getString("currency"),
                entityId = userDataJson.getString("entityId"),
                entityName = userDataJson.getString("entityName"),
                organizationId = organizationId,
                organizationName = userDataJson.getString("organizationName"),
                role = UserRole(
                    id = roleJson.getString("id"),
                    role = roleJson.getString("role"),
                    name = roleJson.getString("name")
                )
            )

            val secret = prefs.getString("secret", "") ?: ""
            val userToken = prefs.getString("userToken", "") ?: ""
            val authToken = prefs.getString("authToken", "") ?: ""

            Log.d(TAG, "getCurrentCredentials: Credentials retrieved successfully")
            Log.d(TAG, "getCurrentCredentials: User: ${userData.username}")
            Log.d(TAG, "getCurrentCredentials: Secret length: ${secret.length}")
            Log.d(TAG, "getCurrentCredentials: UserToken length: ${userToken.length}")
            Log.d(TAG, "getCurrentCredentials: AuthToken length: ${authToken.length}")

            // Validate token formats
            if (!userToken.startsWith("eyJ")) {
                Log.e(
                    TAG,
                    "getCurrentCredentials: WARNING - userToken does not start with 'eyJ' as expected for JWT token"
                )
            }
            if (!authToken.startsWith("eyJ")) {
                Log.e(
                    TAG,
                    "getCurrentCredentials: WARNING - authToken does not start with 'eyJ' as expected for JWT token"
                )
            }

            // Check if tokens are in expected format
            if (!userToken.contains(".") || !authToken.contains(".")) {
                Log.e(
                    TAG,
                    "getCurrentCredentials: WARNING - tokens don't contain dots as expected in JWT format"
                )
            }

            return@withContext AuthenticationCredentials(
                secret = secret,
                userToken = userToken,
                authToken = authToken,
                userData = userData,
                expiresAt = prefs.getLong("expiresAt", 0),
                email = prefs.getString("email", "") ?: "",
                password = prefs.getString("password", "") ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "getCurrentCredentials: Error retrieving credentials", e)
            Log.e(TAG, "getCurrentCredentials: Error message: ${e.message}")
            Log.e(TAG, "getCurrentCredentials: Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }

    override suspend fun clearCredentials(): Unit = withContext(Dispatchers.IO) {
        Log.d(TAG, "clearCredentials: Clearing all saved credentials")
        prefs.edit().clear().apply()
        Log.d(TAG, "clearCredentials: Credentials cleared")
    }
}