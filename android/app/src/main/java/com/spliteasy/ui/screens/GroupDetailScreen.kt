package com.spliteasy.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spliteasy.api.DashboardBalanceResponse
import com.spliteasy.api.ExpenseResponse
import com.spliteasy.api.GroupMemberResponse
import com.spliteasy.ui.SettingsManager
import com.spliteasy.ui.viewmodels.GroupDetailViewModel
import com.spliteasy.ui.viewmodels.LedgerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: Int, 
    onBack: () -> Unit, 
    onAddExpenseClick: () -> Unit,
    viewModel: GroupDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val currencySymbol = SettingsManager.getCurrencySymbol()
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<ExpenseResponse?>(null) }
    var selectedBalanceMember by remember { mutableStateOf<DashboardBalanceResponse?>(null) }

    LaunchedEffect(groupId) {
        viewModel.loadLedger(groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ledger", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddMemberDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Member")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddExpenseClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Expense", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (showAddMemberDialog) {
            var memberName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddMemberDialog = false },
                title = { Text("Add Member", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = memberName,
                        onValueChange = { memberName = it },
                        label = { Text("Member Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (memberName.isNotBlank()) {
                                viewModel.addMember(memberName)
                                showAddMemberDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddMemberDialog = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (selectedExpense != null) {
            ExpenseDetailDialog(
                expense = selectedExpense!!,
                members = (state as? LedgerState.Success)?.members ?: emptyList(),
                onDismiss = { selectedExpense = null }
            )
        }

        AnimatedContent(
            targetState = state,
            modifier = Modifier.padding(padding)
        ) { ledgerState ->
            when (ledgerState) {
                is LedgerState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is LedgerState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error: ${ledgerState.message}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { viewModel.loadLedger(groupId) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                is LedgerState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text("Group Balances", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                ledgerState.balances.forEach { balance ->
                                    val isPositive = balance.balance > 0
                                    val isNeutral = balance.balance == 0
                                    val valColor = if (isPositive) MaterialTheme.colorScheme.primary else if (isNeutral) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.tertiary
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedBalanceMember = balance }
                                            .padding(vertical = 12.dp, horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(balance.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(
                                            text = if (isPositive) "+$currencySymbol${balance.balance}" else if (isNeutral) "Settled" else "-$currencySymbol${Math.abs(balance.balance)}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp,
                                            color = valColor
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Recent Activity", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(ledgerState.expenses.reversed()) { expense ->
                                val paidByMember = ledgerState.members.find { it.memberId == expense.paidBy }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                        .clickable { selectedExpense = expense }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(expense.description, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Paid by ${paidByMember?.name ?: "#${expense.paidBy}"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("$currencySymbol${expense.amount}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (selectedBalanceMember != null) {
            SettleDebtDialog(
                member = selectedBalanceMember!!,
                onDismiss = { selectedBalanceMember = null },
                onSettle = { amount ->
                    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                    val currentUser = (state as? LedgerState.Success)?.members?.find { it.userId == auth.currentUser?.uid }
                    if (currentUser != null && selectedBalanceMember != null) {
                        // If selected member has negative balance, THEY owe money -> WE receive
                        // If selected member has positive balance, THEY are owed money -> WE pay
                        if (selectedBalanceMember!!.balance > 0) {
                            // We pay them
                            viewModel.settleDebt(currentUser.memberId, selectedBalanceMember!!.memberId, amount)
                        } else {
                            // They pay us
                            viewModel.settleDebt(selectedBalanceMember!!.memberId, currentUser.memberId, amount)
                        }
                    }
                    selectedBalanceMember = null
                }
            )
        }
    }
}

@Composable
fun SettleDebtDialog(
    member: com.spliteasy.api.DashboardBalanceResponse,
    onDismiss: () -> Unit,
    onSettle: (Int) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    val currencySymbol = SettingsManager.getCurrencySymbol()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settle with ${member.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter the amount paid to/by this person.", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                    label = { Text("Amount ($currencySymbol)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSettle(amount.toIntOrNull() ?: 0) }) {
                Text("Settle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun ExpenseDetailDialog(
    expense: ExpenseResponse,
    members: List<GroupMemberResponse>,
    onDismiss: () -> Unit
) {
    val currencySymbol = SettingsManager.getCurrencySymbol()
    val paidByMember = members.find { it.memberId == expense.paidBy }
    val splitAmount = if (members.isNotEmpty()) expense.amount.toDouble() / members.size else 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "$currencySymbol${expense.amount}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Paid by", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        paidByMember?.name ?: "Unknown",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Date", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        expense.dateAdded.take(10), // Basic date display
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                
                Text(
                    "Split Details",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                members.forEach { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            member.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (member.memberId == expense.paidBy) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            "$currencySymbol${"%.2f".format(splitAmount)}",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
