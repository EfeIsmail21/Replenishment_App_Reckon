package edu.ap.be.replenishmachine.auth.token

import edu.ap.be.replenishmachine.model.UserData

/**
 * Data class representing a complete set of authentication credentials.
 * Contains all necessary tokens and user information for API authentication.
 *
 * @property secret Secret key for API communication
 * @property userToken Token representing user authentication status
 * @property authToken Token for authorizing API requests
 * @property userData User's personal and role information
 * @property expiresAt Timestamp when the authentication expires
 * @property email User's email address
 * @property password User's password
 */
data class AuthenticationCredentials(
    val secret: String,
    val userToken: String,
    val authToken: String,
    val userData: UserData,
    val expiresAt: Long,
    val email: String,
    val password: String
)