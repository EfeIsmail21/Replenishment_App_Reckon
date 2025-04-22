package edu.ap.be.replenishmachine.auth.provider

import edu.ap.be.replenishmachine.model.UserData

/**
 * Interface defining authentication operations to be implemented by specific providers.
 * Abstracts authentication functionality to allow different backend implementations.
 */
interface AuthenticationProvider {
    /**
     * Obtains a secret key required for authentication operations.
     *
     * @return Secret string used for securing API communication
     */
    suspend fun obtainSecret(): String

    /**
     * Generates user data for a given email and password.
     *
     * @param secret Authentication secret from obtainSecret()
     * @param email User's email address
     * @param password User's password
     * @return Response containing user token and user data
     */
    suspend fun generateUserData(secret: String, email: String, password: String): UserDataResponse

    /**
     * Generates an authentication token using a previously obtained user token.
     *
     * @param userToken The user token from a successful login
     * @return Authentication token for API access
     */
    suspend fun generateAuthToken(userToken: String): String

    /**
     * Data class representing a response containing user token and data.
     */
    data class UserDataResponse(
        val userToken: String,
        val userData: UserData
    )
}