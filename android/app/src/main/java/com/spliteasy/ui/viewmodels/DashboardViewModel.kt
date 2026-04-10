package com.spliteasy.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.spliteasy.api.GroupCreate
import com.spliteasy.api.GroupResponse
import com.spliteasy.api.NetworkErrorMapper
import com.spliteasy.api.RetrofitClient
import com.spliteasy.api.UserCreate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val groups: List<GroupResponse>) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel : ViewModel() {
    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups() {
        _state.value = DashboardState.Loading
        viewModelScope.launch {
            try {
                fetchGroupsInternal()
            } catch (e: Exception) {
                val errorMsg = NetworkErrorMapper.map(e)
                if (errorMsg.contains("User profile not registered internally")) {
                    // AUTO-FIX: Attempt to register the missing profile
                    val auth = FirebaseAuth.getInstance()
                    val user = auth.currentUser
                    if (user != null) {
                        try {
                            RetrofitClient.apiService.createUser(
                                UserCreate(
                                    uid = user.uid,
                                    name = user.email?.substringBefore("@") ?: "User",
                                    email = user.email ?: ""
                                )
                            )
                            // Retry fetching groups now that profile is sync'd
                            fetchGroupsInternal()
                            return@launch
                        } catch (re: Exception) {
                            // Registration retry failed too
                        }
                    }
                }
                _state.value = DashboardState.Error(errorMsg)
            }
        }
    }

    private suspend fun fetchGroupsInternal() {
        val groups = RetrofitClient.apiService.getMyGroups()
        delay(300)
        _state.value = DashboardState.Success(groups)
    }

    fun createGroup(groupName: String) {
        if (groupName.isBlank()) return
        
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.createGroup(GroupCreate(groupName))
                loadGroups() // Refresh silently
            } catch (e: Exception) {}
        }
    }
}
