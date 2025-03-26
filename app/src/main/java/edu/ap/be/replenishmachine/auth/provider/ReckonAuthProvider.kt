package edu.ap.be.replenishmachine.auth.provider

import android.util.Log
import edu.ap.be.replenishmachine.auth.model.CreatedBy
import edu.ap.be.replenishmachine.auth.model.UserData
import edu.ap.be.replenishmachine.auth.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ReckonAuthProvider(
    private val secretEndpoint: String,
    private val userTokenEndpoint: String,
    private val authTokenEndpoint: String
) : AuthenticationProvider {
    override suspend fun obtainSecret(): String = withContext(Dispatchers.IO) {
        // Implement API call to obtain secret
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://buybye-dev.reckon.ai/admin/test")
            .build()
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Failed to obtain secret")

        // Extract the secret from the JSON response
        try {
            val jsonObject = JSONObject(responseBody)
            if (jsonObject.getBoolean("success")) {
                val result = jsonObject.getString("result")
                Log.d("ReckonAuthProvider", "Obtained secret successfully: $result")
                return@withContext result
            } else {
                val errorMessage = jsonObject.getString("errorMessage") ?: "Unknown error"
                Log.e("ReckonAuthProvider", "Failed to obtain secret: $errorMessage")
                throw Exception("Failed to obtain secret: $errorMessage")
            }
        } catch (e: Exception) {
            Log.e("ReckonAuthProvider", "Failed to parse secret response", e)
            throw Exception("Failed to parse secret response", e)
        }
    }

    override suspend fun generateUserData(
        secret: String,
        email: String,
        password: String
    ): AuthenticationProvider.UserDataResponse = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val body = """
    {
        "url": "v2/user/login",
        "data": {
            "password": "$password",
            "email": "$email"
        },
        "method": "post"
    }
""".trimIndent().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://auth-dev.reckon.ai/app/send")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("reckon-origin", "backoffice.reckon.ai")
            .addHeader("secret", secret)
            .build()

        Log.d("ReckonAuthProvider", "Requesting user data with secret")
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Failed to fetch user data")

        Log.d("ReckonAuthProvider", "User data response received")

        try {
            val jsonObject = JSONObject(responseBody)
            if (jsonObject.getBoolean("success")) {
                val resultObj = jsonObject.getJSONObject("result")
                val userToken = resultObj.getString("token")
                
                // Parse user data
                val userJson = resultObj.getJSONObject("user")
                val entityUserJson = resultObj.getJSONArray("entitiesUser").getJSONObject(0)
                val entityJson = entityUserJson.getJSONObject("entity")
                val organizationJson = entityUserJson.getJSONObject("organization")
                val roleJson = entityUserJson.getJSONObject("role")
                
                val userData = UserData(
                    userId = userJson.getString("_id"),
                    username = userJson.getString("username"),
                    email = userJson.getString("email"),
                    locale = userJson.getString("locale"),
                    createdBy = CreatedBy(
                        id = userJson.getJSONObject("createdBy").getString("_id"),
                        username = userJson.getJSONObject("createdBy").getString("username")
                    ),
                    active = userJson.getBoolean("active"),
                    createdAt = userJson.getString("createdAt"),
                    updatedAt = userJson.getString("updatedAt"),
                    toDelete = userJson.getBoolean("toDelete"),
                    currency = userJson.getString("currency"),
                    entityId = entityJson.getString("_id"),
                    entityName = entityJson.getString("label"),
                    organizationId = organizationJson.getString("_id"),
                    organizationName = organizationJson.getString("organization"),
                    role = UserRole(
                        id = roleJson.getString("_id"),
                        role = roleJson.getString("role"),
                        name = roleJson.getString("name")
                    )
                )
                
                Log.d("ReckonAuthProvider", "Parsed user data successfully for: ${userData.username}")
                return@withContext AuthenticationProvider.UserDataResponse(userToken, userData)
            } else {
                val errorMessage = jsonObject.optString("errorMessage", "Unknown error")
                Log.e("ReckonAuthProvider", "Failed to obtain user data: $errorMessage")
                throw Exception("Failed to obtain user data: $errorMessage")
            }
        } catch (e: Exception) {
            Log.e("ReckonAuthProvider", "Failed to parse user data response", e)
            throw Exception("Failed to parse user data response", e)
        }
    }

    override suspend fun generateAuthToken(userToken: String): String =
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val mediaType = "application/json".toMediaType()
            val body =
                "{\n    \"url\": \"app/token\",\n    \"data\": {\n        \"dns\": \"buybye.reckon.ai\",\n        \"data\": {}\n    },\n    \"method\": \"post\"\n}".toRequestBody(
                    mediaType
                )
            val request = Request.Builder()
                .url("https://auth-dev.reckon.ai/app/send")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("reckon-origin", "backoffice.reckon.ai")
                .addHeader("secret", obtainSecret())
                .build()

            Log.d("ReckonAuthProvider", "Requesting auth token with secret")
            val response = client.newCall(request).execute()
            val responseBody =
                response.body?.string() ?: throw Exception("Failed to generate auth token")

            // Extract the auth token from the JSON response
            try {
                val jsonObject = JSONObject(responseBody)
                if (jsonObject.getBoolean("success")) {
                    val result = jsonObject.getString("result")
                    Log.d("ReckonAuthProvider", "Obtained auth token successfully")
                    return@withContext result
                } else {
                    val errorMessage = jsonObject.getString("errorMessage") ?: "Unknown error"
                    Log.e("ReckonAuthProvider", "Failed to obtain auth token: $errorMessage")
                    throw Exception("Failed to obtain auth token: $errorMessage")
                }
            } catch (e: Exception) {
                Log.e("ReckonAuthProvider", "Failed to parse auth token response", e)
                throw Exception("Failed to parse auth token response", e)
            }
        }
}