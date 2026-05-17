package com.example.nammareshme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammareshme.data.BatchRepository
import com.example.nammareshme.ui.screens.BatchHistoryItem
import com.example.nammareshme.ui.screens.ClimateLog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BatchViewModel(private val repository: BatchRepository) : ViewModel() {

    val batches: StateFlow<List<BatchHistoryItem>> = repository.getBatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val climateLogs: StateFlow<List<ClimateLog>> = repository.getClimateLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBatch(batch: BatchHistoryItem, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.addBatch(batch)
                .onSuccess { onSuccess() }
                .onFailure { onError(it.message ?: "Failed to add batch") }
        }
    }

    fun addClimateLog(log: ClimateLog, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.addClimateLog(log)
                .onSuccess { onSuccess() }
                .onFailure { onError(it.message ?: "Failed to save climate log") }
        }
    }
}
