package com.spliteasy.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spliteasy.ui.theme.PrimaryGradientColors
import com.spliteasy.ui.viewmodels.LoginState
import com.spliteasy.ui.viewmodels.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    
    val state by viewModel.state.collectAsState()
    var isVisible by remember { mutableStateOf(false) }

    val autofill = LocalAutofill.current
    val autofillTree = LocalAutofillTree.current

    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    LaunchedEffect(state) {
        if (state is LoginState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(PrimaryGradientColors))
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { 300 }, animationSpec = tween(700)) + fadeIn(animationSpec = tween(700)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SplitEasy", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                    Text("Secure Expense Sharing", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 32.dp))
                    
                    if (isSignUp) {
                        val nameAutofillNode = remember {
                            AutofillNode(
                                onFill = { name = it },
                                autofillTypes = listOf(AutofillType.PersonFullName)
                            )
                        }
                        LaunchedEffect(Unit) { autofillTree += nameAutofillNode }

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Display Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { nameAutofillNode.boundingBox = it.localBoundingBoxOf(it.parentLayoutCoordinates!!) }
                                .onFocusChanged { focusState ->
                                    autofill?.run {
                                        if (focusState.isFocused) {
                                            requestAutofillForNode(nameAutofillNode)
                                        } else {
                                            cancelAutofillForNode(nameAutofillNode)
                                        }
                                    }
                                }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    val emailAutofillNode = remember {
                        AutofillNode(
                            onFill = { email = it },
                            autofillTypes = listOf(AutofillType.EmailAddress)
                        )
                    }
                    LaunchedEffect(Unit) { autofillTree += emailAutofillNode }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { emailAutofillNode.boundingBox = it.localBoundingBoxOf(it.parentLayoutCoordinates!!) }
                            .onFocusChanged { focusState ->
                                autofill?.run {
                                    if (focusState.isFocused) {
                                        requestAutofillForNode(emailAutofillNode)
                                    } else {
                                        cancelAutofillForNode(emailAutofillNode)
                                    }
                                }
                            }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val passwordAutofillNode = remember {
                        AutofillNode(
                            onFill = { password = it },
                            autofillTypes = listOf(AutofillType.Password)
                        )
                    }
                    LaunchedEffect(Unit) { autofillTree += passwordAutofillNode }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { passwordAutofillNode.boundingBox = it.localBoundingBoxOf(it.parentLayoutCoordinates!!) }
                            .onFocusChanged { focusState ->
                                autofill?.run {
                                    if (focusState.isFocused) {
                                        requestAutofillForNode(passwordAutofillNode)
                                    } else {
                                        cancelAutofillForNode(passwordAutofillNode)
                                    }
                                }
                            }
                    )
                    
                    if (!isSignUp) {
                        Text(
                            text = "Forgot Password?",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clickable { viewModel.sendPasswordReset(email) },
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (state is LoginState.Error) {
                        Text((state as LoginState.Error).message, color = MaterialTheme.colorScheme.error, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp), textAlign = TextAlign.Center)
                    }

                    Button(
                        onClick = { viewModel.authenticate(email, password, isSignUp, name) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        AnimatedContent(targetState = state is LoginState.Loading) { loading ->
                            if (loading) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                            } else {
                                Text(if (isSignUp) "Create Account" else "Log In", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (isSignUp) "Already have an account? Log In" else "Don't have an account? Sign Up",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { 
                            isSignUp = !isSignUp 
                            viewModel.resetState()
                        }
                    )
                }
            }
        }
    }
}
