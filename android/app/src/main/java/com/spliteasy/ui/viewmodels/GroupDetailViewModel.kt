package com.spliteasy.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.api.DashboardBalanceResponse
import com.spliteasy.api.ExpenseResponse
import com.spliteasy.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LedgerState {
    object Loading : LedgerState()
    data class Success(val balances: List<DashboardBalanceResponse>, val expenses: List<ExpenseResponse>) : LedgerState()
    data class Error(val message: String) : LedgerState()
}

class GroupDetailViewModel : ViewModel() {
    private val _state = MutableStateFlow<LedgerState>(LedgerState.Loading)
    val state: StateFlow<LedgerState> = _state.asStateFlow()

    private var currentGroupId: Int? = null

    fun loadLedger(groupId: Int) {
        if (currentGroupId == groupId && _state.value is LedgerState.Success) return
        
        currentGroupId = groupId
        _state.value = LedgerState.Loading
        
        viewModelScope.launch {
            try {
                val balances = RetrofitClient.apiService.getGroupDashboard(groupId)
                val expenses = RetrofitClient.apiService.getGroupExpenses(groupId)
                _state.value = LedgerState.Success(balances, expenses)
            } catch (e: Exception) {
                _state.value = LedgerState.Error("Offline")
            }
        }
    }
}
