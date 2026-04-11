package com.spliteasy.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SettingsManager {
    private val _currency = MutableStateFlow("USD ($)")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _themeMode = MutableStateFlow("Dark") // Dark, Light, System
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setCurrency(newCurrency: String) {
        _currency.value = newCurrency
    }

    fun setThemeMode(newMode: String) {
        _themeMode.value = newMode
    }

    fun getCurrencySymbol(): String {
        return _currency.value.substringAfterLast("(", "").substringBefore(")", "$")
    }
}
