package com.example.nammareshme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammareshme.data.AuthRepository
import com.example.nammareshme.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        checkUserSession()
    }

    private fun checkUserSession() {
        val firebaseUser = repository.currentUser
        if (firebaseUser != null) {
            _authState.value = AuthState.Loading
            viewModelScope.launch {
                repository.getUserData(firebaseUser.uid).onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated(user)
                }.onFailure {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun login(phone: String, password: String) {
        if (phone.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please enter phone and password")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.login(phone, password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated(user)
                }
                .onFailure { e ->
                    _authState.value = AuthState.Error(e.message ?: "Login failed")
                }
        }
    }

    fun register(name: String, phone: String, password: String) {
        if (name.isBlank() || phone.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill all fields")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.register(name, phone, password)
                .onSuccess {
                    repository.logout()
                    _authState.value = AuthState.RegistrationSuccess
                }
                .onFailure { e ->
                    _authState.value = AuthState.Error(e.message ?: "Registration failed")
                }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.signInWithGoogle(idToken)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated(user)
                }
                .onFailure { e ->
                    _authState.value = AuthState.Error(e.message ?: "Google Sign-In failed")
                }
        }
    }

    fun forgotPassword(phone: String) {
        if (phone.isBlank()) {
            _authState.value = AuthState.Error("Please enter your phone number")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.forgotPassword(phone)
                .onSuccess {
                    _authState.value = AuthState.Success("Password reset email sent")
                }
                .onFailure { e ->
                    _authState.value = AuthState.Error(e.message ?: "Failed to send reset email")
                }
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    object RegistrationSuccess : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
    data class Success(val message: String) : AuthState()
}
