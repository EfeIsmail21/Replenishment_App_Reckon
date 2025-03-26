package edu.ap.be.replenishmachine.auth.token.storage

import edu.ap.be.replenishmachine.auth.token.AuthenticationCredentials

/**
 * Interface defining operations for storing and retrieving authentication credentials.
 * Provides a contract for credential persistence regardless of storage implementation.
 */
interface TokenStorage {
    /**
     * Saves authentication credentials.
     *
     * @param credentials The credentials to save
     */
    suspend fun saveCredentials(credentials: AuthenticationCredentials)

    /**
     * Retrieves the current authentication credentials.
     *
     * @return Current authentication credentials
     * @throws IllegalStateException if no credentials are available
     */
    suspend fun getCurrentCredentials(): AuthenticationCredentials

    /**
     * Clears all stored credentials.
     */
    suspend fun clearCredentials()
}