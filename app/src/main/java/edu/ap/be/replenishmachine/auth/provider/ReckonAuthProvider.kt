package edu.ap.be.replenishmachine.auth.provider

import android.util.Log
import edu.ap.be.replenishmachine.model.CreatedBy
import edu.ap.be.replenishmachine.model.Machine
import edu.ap.be.replenishmachine.model.UserData
import edu.ap.be.replenishmachine.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ReckonAuthProvider(
    private val secretEndpoint: String,
    private val userTokenEndpoint: String,
    private val authTokenEndpoint: String
) : AuthenticationProvider {
    override suspend fun obtainSecret(): String = withContext(Dispatchers.IO) {
        Log.d("ReckonAuthProvider", "obtainSecret: Starting request to ${secretEndpoint}")
        Log.d("ReckonAuthProvider", "obtainSecret: Creating OkHttpClient with 60s timeout")
        
        try {
            // Check DNS resolution first
            Log.d("ReckonAuthProvider", "obtainSecret: Testing DNS resolution for buybye-dev.reckon.ai")
            val inetAddress = java.net.InetAddress.getByName("buybye-dev.reckon.ai")
            Log.d("ReckonAuthProvider", "obtainSecret: DNS resolution successful: ${inetAddress.hostAddress}")
        } catch (e: Exception) {
            Log.e("ReckonAuthProvider", "obtainSecret: DNS resolution failed", e)
            // Continue anyway to see the actual HTTP error
        }
        
        // Implement API call to obtain secret
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)  // Even longer timeout
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)  // Enable retries
            .build()
        
        Log.d("ReckonAuthProvider", "obtainSecret: Building request for ${secretEndpoint}")
        val request = Request.Builder()
            .url(secretEndpoint)
            .build()
        
        Log.d("ReckonAuthProvider", "obtainSecret: Executing request to ${secretEndpoint}")
        try {
            val response = client.newCall(request).execute()
            Log.d("ReckonAuthProvider", "obtainSecret: Response received with code: ${response.code}")
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
        } catch (e: Exception) {
            Log.e("ReckonAuthProvider", "Network error during obtainSecret()", e)
            throw e
        }
    }

    override suspend fun generateUserData(
        secret: String,
        email: String,
        password: String
    ): AuthenticationProvider.UserDataResponse = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)  // Even longer timeout
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)  // Enable retries
            .build()
        val mediaType = "application/json".toMediaType()
        val bodyContent = """
    {
        "url": "v2/user/login",
        "data": {
            "password": "$password",
            "email": "$email"
        },
        "method": "post"
    }
""".trimIndent()
        val body = bodyContent.toRequestBody(mediaType)

        // Log request details for comparison
        Log.d(
            "ReckonAuthProvider",
            "------------------------ USER LOGIN REQUEST DETAILS ------------------------"
        )
        Log.d("ReckonAuthProvider", "Request URL: https://auth-dev.reckon.ai/app/send")
        Log.d("ReckonAuthProvider", "Request body: $bodyContent")
        Log.d("ReckonAuthProvider", "Header - Content-Type: application/json")
        Log.d("ReckonAuthProvider", "Header - reckon-origin: backoffice.reckon.ai")
        Log.d("ReckonAuthProvider", "Header - secret: $secret")
        Log.d(
            "ReckonAuthProvider",
            "-------------------------------------------------------------------------"
        )

        val request = Request.Builder()
            .url("https://auth-dev.reckon.ai/app/send")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("reckon-origin", "backoffice.reckon.ai")
            .addHeader("secret", secret)
            .build()

        Log.d("ReckonAuthProvider", "Requesting user data with secret")
        val response = client.newCall(request).execute()
        val responseCode = response.code
        val responseBody = response.body?.string() ?: throw Exception("Failed to fetch user data")

        Log.d(
            "ReckonAuthProvider",
            "------------------------ USER LOGIN RESPONSE DETAILS ------------------------"
        )
        Log.d("ReckonAuthProvider", "Response code: $responseCode")
        Log.d("ReckonAuthProvider", "Response body: ${responseBody.take(200)}...")
        Log.d(
            "ReckonAuthProvider",
            "-----------------------------------------------------------------------------"
        )

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
            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // Even longer timeout
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)  // Enable retries
                .build()
            val mediaType = "application/json".toMediaType()
            val bodyContent =
                "{\n    \"url\": \"app/token\",\n    \"data\": {\n        \"dns\": \"buybye.reckon.ai\",\n        \"data\": {}\n    },\n    \"method\": \"post\"\n}"
            val body = bodyContent.toRequestBody(mediaType)

            // Get a new secret for auth token
            val secretForAuth = obtainSecret()

            // Log request details
            Log.d(
                "ReckonAuthProvider",
                "------------------------ AUTH TOKEN REQUEST DETAILS ------------------------"
            )
            Log.d("ReckonAuthProvider", "Request URL: https://auth-dev.reckon.ai/app/send")
            Log.d("ReckonAuthProvider", "Request body: $bodyContent")
            Log.d("ReckonAuthProvider", "Header - Content-Type: application/json")
            Log.d("ReckonAuthProvider", "Header - reckon-origin: backoffice.reckon.ai")
            Log.d("ReckonAuthProvider", "Header - secret: $secretForAuth")
            Log.d(
                "ReckonAuthProvider",
                "---------------------------------------------------------------------------"
            )

            val request = Request.Builder()
                .url("https://auth-dev.reckon.ai/app/send")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("reckon-origin", "backoffice.reckon.ai")
                .addHeader("secret", secretForAuth)
                .build()

            Log.d("ReckonAuthProvider", "Requesting auth token with secret")
            val response = client.newCall(request).execute()
            val responseCode = response.code
            val responseBody =
                response.body?.string() ?: throw Exception("Failed to generate auth token")

            Log.d(
                "ReckonAuthProvider",
                "------------------------ AUTH TOKEN RESPONSE DETAILS ------------------------"
            )
            Log.d("ReckonAuthProvider", "Response code: $responseCode")
            Log.d("ReckonAuthProvider", "Response body: $responseBody")
            Log.d(
                "ReckonAuthProvider",
                "-----------------------------------------------------------------------------"
            )

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

    suspend fun getMachinesForOrganization(
        organizationId: String,
        secret: String,
        authToken: String,
        userToken: String,
        entityId: String
    ): List<Machine> = withContext(Dispatchers.IO) {
        // Create OkHttpClient with increased timeouts and retry mechanism
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)  // Even longer timeout
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)  // Enable retries
            .build()

        // This is the exact correct URL format
        val finalUrl =
            "https://buybye-dev.reckon.ai/backoffice/v2/organization/machines/list/?idOrganization=$organizationId"

        Log.d("ReckonAuthProvider", "Using the exact correct URL: $finalUrl")

        // Log the exact request details
        Log.d(
            "ReckonAuthProvider",
            "------------------------ REQUEST DETAILS ------------------------"
        )
        Log.d("ReckonAuthProvider", "Request URL: $finalUrl")
        Log.d("ReckonAuthProvider", "Header - authtoken: ${authToken.take(20)}...")
        Log.d("ReckonAuthProvider", "Header - usertoken: ${userToken.take(20)}...")
        Log.d("ReckonAuthProvider", "Header - identity: $entityId")
        Log.d(
            "ReckonAuthProvider",
            "---------------------------------------------------------------"
        )

        // Check token formats
        Log.d(
            "ReckonAuthProvider",
            "AuthToken format check - starts with 'eyJ': ${authToken.startsWith("eyJ")}"
        )
        Log.d(
            "ReckonAuthProvider",
            "UserToken format check - starts with 'eyJ': ${userToken.startsWith("eyJ")}"
        )
        Log.d("ReckonAuthProvider", "AuthToken length: ${authToken.length}")
        Log.d("ReckonAuthProvider", "UserToken length: ${userToken.length}")

        val request = Request.Builder()
            .url(finalUrl)
            .addHeader("authtoken", authToken)  // Using addHeader as in the example
            .addHeader("usertoken", userToken)
            .addHeader("identity", entityId)
            .build()

        Log.d("ReckonAuthProvider", "Requesting machines for organization: $organizationId")

        val response = client.newCall(request).execute()
        val responseCode = response.code
        val responseBody = response.body?.string() ?: throw Exception("Failed to fetch machines")

        Log.d(
            "ReckonAuthProvider",
            "------------------------ RESPONSE DETAILS ------------------------"
        )
        Log.d("ReckonAuthProvider", "Response code: $responseCode")
        Log.d("ReckonAuthProvider", "Response received: $responseBody")
        Log.d(
            "ReckonAuthProvider",
            "------------------------------------------------------------------"
        )

        try {
            val jsonObject = JSONObject(responseBody)
            if (jsonObject.getBoolean("success")) {
                val machines = mutableListOf<Machine>()
                val resultObj = jsonObject.optJSONObject("result") ?: JSONObject()

                // Check if "machines" key exists in the response
                if (resultObj.has("machines")) {
                    val machinesArray = resultObj.getJSONArray("machines")
                    Log.d(
                        "ReckonAuthProvider",
                        "Found machines array with ${machinesArray.length()} items"
                    )

                    for (i in 0 until machinesArray.length()) {
                        val machineJson = machinesArray.getJSONObject(i)
                        machines.add(
                            Machine(
                                id = machineJson.getString("_id"),
                                organization = machineJson.getString("organization"),
                                code = machineJson.getString("code"),
                                createdAt = machineJson.getString("createdAt")
                            )
                        )
                    }
                } else if (jsonObject.has("result") && jsonObject.get("result") is JSONArray) {
                    // Check if result is directly an array
                    val machinesArray = jsonObject.getJSONArray("result")
                    Log.d(
                        "ReckonAuthProvider",
                        "Found machines array with ${machinesArray.length()} items"
                    )

                    for (i in 0 until machinesArray.length()) {
                        val machineJson = machinesArray.getJSONObject(i)
                        machines.add(
                            Machine(
                                id = machineJson.getString("_id"),
                                organization = machineJson.optString(
                                    "organization",
                                    organizationId
                                ),
                                code = machineJson.optString("code", "N/A"),
                                createdAt = machineJson.optString("createdAt", "N/A")
                            )
                        )
                    }
                } else {
                    // Log the actual structure of the result for debugging
                    if (jsonObject.has("result")) {
                        val resultValue = jsonObject.get("result")
                        Log.d(
                            "ReckonAuthProvider",
                            "Result is of type: ${resultValue.javaClass.simpleName}"
                        )
                        Log.d("ReckonAuthProvider", "Result value: $resultValue")
                    } else {
                        Log.d("ReckonAuthProvider", "No 'result' key found in response")
                        Log.d(
                            "ReckonAuthProvider",
                            "Available keys: ${
                                jsonObject.keys().asSequence().toList().joinToString()
                            }"
                        )
                    }
                }

                Log.d("ReckonAuthProvider", "Retrieved ${machines.size} machines")
                return@withContext machines
            } else {
                val errorMessage = if (jsonObject.has("errorMessage")) {
                    jsonObject.get("errorMessage").toString()
                } else {
                    "Unknown error"
                }
                Log.e("ReckonAuthProvider", "Failed to get machines: $errorMessage")

                if (errorMessage.contains("Unauthorized")) {
                    Log.e("ReckonAuthProvider", "Authorization error detected. Check tokens.")
                }

                throw Exception("Failed to get machines: $errorMessage")
            }
        } catch (e: Exception) {
            Log.e("ReckonAuthProvider", "Failed to parse machines response", e)
            throw Exception("Failed to parse machines response: ${e.message}")
        }
    }

    suspend fun getMachineStocks(
        machineId: String,
        authToken: String,
        userToken: String,
        entityId: String
    ): JSONObject = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)  // Even longer timeout
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)  // Enable retries
            .build()

        // Use same URL pattern as was successful for machines list
        val finalUrl = "https://buybye-dev.reckon.ai/backoffice/v2/machine/stocks/$machineId"

        // Log the exact request details
        Log.d(
            "ReckonAuthProvider",
            "------------------------ MACHINE STOCKS REQUEST DETAILS ------------------------"
        )
        Log.d("ReckonAuthProvider", "Request URL: $finalUrl")
        Log.d("ReckonAuthProvider", "Header - authtoken: ${authToken.take(20)}...")
        Log.d("ReckonAuthProvider", "Header - usertoken: ${userToken.take(20)}...")
        Log.d("ReckonAuthProvider", "Header - identity: $entityId")
        Log.d(
            "ReckonAuthProvider",
            "------------------------------------------------------------------------------"
        )

        val request = Request.Builder()
            .url(finalUrl)
            .addHeader("authtoken", authToken)  // Using addHeader as in the example
            .addHeader("usertoken", userToken)
            .addHeader("identity", entityId)
            .build()

        Log.d("ReckonAuthProvider", "Requesting stocks for machine: $machineId")

        val response = client.newCall(request).execute()
        val responseCode = response.code
        val responseBody =
            response.body?.string() ?: throw Exception("Failed to fetch machine stocks")

        Log.d(
            "ReckonAuthProvider",
            "------------------------ MACHINE STOCKS RESPONSE DETAILS ------------------------"
        )
        Log.d("ReckonAuthProvider", "Response code: $responseCode")
        Log.d("ReckonAuthProvider", "Response received: $responseBody")
        Log.d(
            "ReckonAuthProvider",
            "---------------------------------------------------------------------------------"
        )

        try {
            val jsonObject = JSONObject(responseBody)
            if (jsonObject.getBoolean("success")) {
                val result = jsonObject.optJSONObject("result") ?: JSONObject()
                Log.d("ReckonAuthProvider", "Retrieved stocks for machine: $machineId")
                return@withContext result
            } else {
                val errorMessage = if (jsonObject.has("errorMessage")) {
                    jsonObject.get("errorMessage").toString()
                } else {
                    "Unknown error"
                }
                Log.e("ReckonAuthProvider", "Failed to get machine stocks: $errorMessage")

                throw Exception("Failed to get machine stocks: $errorMessage")
            }
        } catch (e: Exception) {
            Log.e("ReckonAuthProvider", "Failed to parse machine stocks response", e)
            throw Exception("Failed to parse machine stocks response: ${e.message}")
        }
    }
}