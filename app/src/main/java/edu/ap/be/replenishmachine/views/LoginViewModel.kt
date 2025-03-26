package edu.ap.be.replenishmachine.views

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.be.replenishmachine.auth.manager.AuthManager
import edu.ap.be.replenishmachine.auth.token.AuthenticationCredentials
import edu.ap.be.replenishmachine.auth.model.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for handling login-related operations and UI state.
 * Manages authentication logic and exposes login state for UI consumption.
 */
class LoginViewModel(private val authManager: AuthManager) : ViewModel() {

    // Login state for UI updates
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    // User data for displaying in the UI
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    /**
     * Performs user authentication with the provided credentials.
     * Updates login state and user data based on result.
     *
     * @param email User's email address
     * @param password User's password
     * @return Result containing authentication credentials on success or exception on failure
     */
    suspend fun performLogin(email: String, password: String): Result<AuthenticationCredentials> {
        return try {
            Log.d("LoginViewModel", "Attempting login for email: $email")
            _loginState.value = LoginState.Loading
            
            val credentials = authManager.login(email, password)
            
            // Store user data for easy access in the UI
            _userData.value = credentials.userData
            
            Log.d("LoginViewModel", "Login successful for: ${credentials.userData.username}")
            _loginState.value = LoginState.Success(credentials.userData)
            
            Result.success(credentials)
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Login failed", e)
            _loginState.value = LoginState.Error(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    /**
     * Checks if the user is already logged in and updates state accordingly.
     * Called when the app starts to restore user session if available.
     */
    fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                if (authManager.isLoggedIn()) {
                    val userData = authManager.getUserData()
                    _userData.value = userData
                    _loginState.value = LoginState.Success(userData)
                }
            } catch (e: Exception) {
                // User is not logged in, stay in Idle state
                Log.d("LoginViewModel", "No existing login detected")
            }
        }
    }
}

/**
 * Sealed class representing different states of the login process.
 */
sealed class LoginState {
    /**
     * Initial state before any login attempt.
     */
    object Idle : LoginState()

    /**
     * State during an ongoing login operation.
     */
    object Loading : LoginState()

    /**
     * State representing a successful login with user data.
     */
    data class Success(val userData: UserData) : LoginState()

    /**
     * State representing a failed login with error message.
     */
    data class Error(val message: String) : LoginState()
}