package com.spliteasy.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.spliteasy.api.NetworkErrorMapper
import com.spliteasy.api.RetrofitClient
import com.spliteasy.api.UserCreate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun authenticate(email: String, password: String, isSignUp: Boolean, name: String = "") {
        if (email.isBlank() || password.isBlank() || (isSignUp && name.isBlank())) {
            _state.value = LoginState.Error("Please fill all fields")
            return
        }

        _state.value = LoginState.Loading
        viewModelScope.launch {
            try {
                // 1. Authenticate with Firebase
                val authResult = if (isSignUp) {
                    auth.createUserWithEmailAndPassword(email, password).await()
                } else {
                    auth.signInWithEmailAndPassword(email, password).await()
                }

                val user = authResult.user ?: throw Exception("Auth Failed")
                
                // 2. Retrieve Cryptographic ID Token
                val tokenResult = user.getIdToken(true).await()
                val idToken = tokenResult.token ?: throw Exception("Token Generation Failed")
                
                // 3. Mount Token to Retrofit Interceptor
                RetrofitClient.currentMemberUid = idToken

                // 4. Sync Profile to FastAPI Backend (Always attempt to ensure record exists)
                try {
                    val displayName = if (isSignUp) name else email.substringBefore("@")
                    RetrofitClient.apiService.createUser(UserCreate(user.uid, displayName, email))
                } catch (e: Exception) {
                    // Ignore errors (e.g. 400 User already registered)
                }

                delay(500) // Premium UX delay
                _state.value = LoginState.Success

            } catch (e: Exception) {
                _state.value = LoginState.Error(NetworkErrorMapper.map(e))
            }
        }
    }

    fun resetState() {
        _state.value = LoginState.Idle
    }
}
