package com.spliteasy.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.api.GroupCreate
import com.spliteasy.api.GroupResponse
import com.spliteasy.api.RetrofitClient
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
                val groups = RetrofitClient.apiService.getMyGroups()
                delay(300)
                _state.value = DashboardState.Success(groups)
            } catch (e: Exception) {
                _state.value = DashboardState.Error("Failed to fetch groups")
            }
        }
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
