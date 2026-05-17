package com.example.nammareshme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammareshme.data.WeatherRepository
import com.example.nammareshme.models.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weather: WeatherResponse) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
    object Empty : WeatherUiState()
}

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Empty)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            repository.getWeather(lat, lon).fold(
                onSuccess = {
                    _weatherState.value = WeatherUiState.Success(it)
                },
                onFailure = {
                    _weatherState.value = WeatherUiState.Error(it.message ?: "Unknown error occurred")
                }
            )
        }
    }
}
