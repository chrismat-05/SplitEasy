package com.spliteasy.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.api.DashboardBalanceResponse
import com.spliteasy.api.ExpenseResponse
import com.spliteasy.api.GroupMemberCreate
import com.spliteasy.api.GroupMemberResponse
import com.spliteasy.api.NetworkErrorMapper
import com.spliteasy.api.RetrofitClient
import com.spliteasy.api.SettlementCreate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LedgerState {
    object Loading : LedgerState()
    data class Success(
        val balances: List<DashboardBalanceResponse>, 
        val expenses: List<ExpenseResponse>,
        val members: List<GroupMemberResponse>
    ) : LedgerState()
    data class Error(val message: String) : LedgerState()
}

class GroupDetailViewModel : ViewModel() {
    private val _state = MutableStateFlow<LedgerState>(LedgerState.Loading)
    val state: StateFlow<LedgerState> = _state.asStateFlow()

    private var currentGroupId: Int? = null

    fun loadLedger(groupId: Int) {
        currentGroupId = groupId
        // Don't skip loading if we want to refresh
        _state.value = LedgerState.Loading
        
        viewModelScope.launch {
            try {
                val balances = RetrofitClient.apiService.getGroupDashboard(groupId)
                val expenses = RetrofitClient.apiService.getGroupExpenses(groupId)
                val members = RetrofitClient.apiService.getGroupMembers(groupId)
                _state.value = LedgerState.Success(balances, expenses, members)
            } catch (e: Exception) {
                _state.value = LedgerState.Error(NetworkErrorMapper.map(e))
            }
        }
    }

    fun addMember(name: String) {
        val groupId = currentGroupId ?: return
        if (name.isBlank()) return

        viewModelScope.launch {
            try {
                RetrofitClient.apiService.addGroupMember(groupId, GroupMemberCreate(name))
                loadLedger(groupId) // Refresh
            } catch (e: Exception) {
                // Handle error if needed, for now we just fail silently or could update state
            }
        }
    }

    fun settleDebt(fromMemberId: Int, toMemberId: Int, amount: Int) {
        val groupId = currentGroupId ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            try {
                RetrofitClient.apiService.addSettlement(groupId, SettlementCreate(fromMemberId, toMemberId, amount))
                loadLedger(groupId)
            } catch (e: Exception) {}
        }
    }
}
