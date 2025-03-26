package edu.ap.be.replenishmachine.auth.token.storage

import android.content.Context
import android.content.SharedPreferences
import edu.ap.be.replenishmachine.auth.model.CreatedBy
import edu.ap.be.replenishmachine.auth.model.UserData
import edu.ap.be.replenishmachine.auth.model.UserRole
import edu.ap.be.replenishmachine.auth.token.AuthenticationCredentials
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SharedPrefsTokenStorage(private val context: Context) : TokenStorage {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    override suspend fun saveCredentials(credentials: AuthenticationCredentials): Unit =
        withContext(Dispatchers.IO) {
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
        }

    override suspend fun getCurrentCredentials(): AuthenticationCredentials = withContext(Dispatchers.IO) {
        if (!prefs.contains("userToken")) {
            throw IllegalStateException("No credentials available")
        }
        
        val userDataJson = JSONObject(prefs.getString("userData", "{}") ?: "{}")
        val createdByJson = userDataJson.getJSONObject("createdBy")
        val roleJson = userDataJson.getJSONObject("role")
        
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
            organizationId = userDataJson.getString("organizationId"),
            organizationName = userDataJson.getString("organizationName"),
            role = UserRole(
                id = roleJson.getString("id"),
                role = roleJson.getString("role"),
                name = roleJson.getString("name")
            )
        )
        
        return@withContext AuthenticationCredentials(
            secret = prefs.getString("secret", "") ?: "",
            userToken = prefs.getString("userToken", "") ?: "",
            authToken = prefs.getString("authToken", "") ?: "",
            userData = userData,
            expiresAt = prefs.getLong("expiresAt", 0),
            email = prefs.getString("email", "") ?: "",
            password = prefs.getString("password", "") ?: ""
        )
    }
    
    override suspend fun clearCredentials() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
    }
}