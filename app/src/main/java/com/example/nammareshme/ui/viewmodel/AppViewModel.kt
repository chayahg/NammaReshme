package com.example.nammareshme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammareshme.data.LanguageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(private val languageManager: LanguageManager) : ViewModel() {
    private val _currentLanguage = MutableStateFlow("English")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _unreadNotifications = MutableStateFlow(0)
    val unreadNotifications: StateFlow<Int> = _unreadNotifications.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        viewModelScope.launch {
            languageManager.getLanguage.collect { language ->
                _currentLanguage.value = language
            }
        }
        viewModelScope.launch {
            languageManager.getUnreadCount.collect { count ->
                _unreadNotifications.value = count
            }
        }
        viewModelScope.launch {
            languageManager.isDarkMode.collect { isDark ->
                _isDarkMode.value = isDark
            }
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            languageManager.saveLanguage(language)
            _currentLanguage.value = language
        }
    }

    fun setUnreadCount(count: Int) {
        viewModelScope.launch {
            languageManager.setUnreadCount(count)
            _unreadNotifications.value = count
        }
    }

    fun clearUnreadCount() {
        viewModelScope.launch {
            languageManager.setUnreadCount(0)
            _unreadNotifications.value = 0
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val newValue = !_isDarkMode.value
            languageManager.setDarkMode(newValue)
            _isDarkMode.value = newValue
        }
    }

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            languageManager.setDarkMode(isDark)
            _isDarkMode.value = isDark
        }
    }
}
