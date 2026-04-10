package com.spliteasy.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spliteasy.api.ExpenseCreate
import com.spliteasy.api.GroupMemberResponse
import com.spliteasy.api.NetworkErrorMapper
import com.spliteasy.api.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TransferState {
    object Idle : TransferState()
    data class LoadingMembers(val isFetching: Boolean) : TransferState()
    data class Ready(val members: List<GroupMemberResponse>) : TransferState()
    object Submitting : TransferState()
    object Success : TransferState()
    data class Error(val message: String, val lastMembers: List<GroupMemberResponse>?) : TransferState()
}

class AddExpenseViewModel : ViewModel() {
    private val _state = MutableStateFlow<TransferState>(TransferState.Idle)
    val state: StateFlow<TransferState> = _state.asStateFlow()

    private var cachedMembers: List<GroupMemberResponse> = emptyList()

    fun fetchMembers(groupId: Int) {
        if (cachedMembers.isNotEmpty()) {
            _state.value = TransferState.Ready(cachedMembers)
            return
        }

        _state.value = TransferState.LoadingMembers(true)
        viewModelScope.launch {
            try {
                cachedMembers = RetrofitClient.apiService.getGroupMembers(groupId)
                _state.value = TransferState.Ready(cachedMembers)
            } catch (e: Exception) {
                _state.value = TransferState.Error(NetworkErrorMapper.map(e), null)
            }
        }
    }

    fun submitExpense(groupId: Int, amountStr: String, description: String, memberId: Int?) {
        val amountInt = amountStr.toIntOrNull()
        if (amountInt == null || amountInt <= 0 || description.isBlank() || memberId == null) {
            _state.value = TransferState.Error("Invalid input details", cachedMembers)
            return
        }

        _state.value = TransferState.Submitting
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.addExpense(groupId, ExpenseCreate(amountInt, description, memberId))
                delay(600)
                _state.value = TransferState.Success
            } catch (e: Exception) {
                _state.value = TransferState.Error(NetworkErrorMapper.map(e), cachedMembers)
            }
        }
    }

    fun resetState() {
        _state.value = TransferState.Idle
        cachedMembers = emptyList()
    }
}
