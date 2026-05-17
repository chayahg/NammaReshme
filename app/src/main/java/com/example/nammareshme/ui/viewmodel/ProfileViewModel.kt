package com.example.nammareshme.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammareshme.data.AuthRepository
import com.example.nammareshme.data.BatchRepository
import com.example.nammareshme.data.LanguageManager
import com.example.nammareshme.data.api.RetrofitClient
import com.example.nammareshme.models.User
import com.example.nammareshme.ui.screens.BatchStatus
import com.example.nammareshme.ui.screens.FarmStats
import com.example.nammareshme.ui.screens.UserProfile
import com.example.nammareshme.utils.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class ProfileViewModel(
    private val repository: AuthRepository,
    private val batchRepository: BatchRepository,
    private val languageManager: LanguageManager
) : ViewModel() {

    private val _realUser = MutableStateFlow<User?>(null)
    val realUser: StateFlow<User?> = _realUser.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    val userProfile: StateFlow<UserProfile?> = _realUser.combine(languageManager.getProfileImageUri) { user, localUri ->
        user?.let {
            UserProfile(
                name = it.name,
                isVerified = it.isVerified,
                phone = it.phone,
                email = if (it.isGoogleUser) it.email else "", 
                location = it.location,
                profileImageRes = null,
                profileImageUrl = localUri ?: it.profileImageUrl
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val farmStats: StateFlow<FarmStats> = batchRepository.getBatches()
        .combine(_realUser) { batches, user ->
            val active = batches.count { it.status == BatchStatus.ACTIVE }
            val completed = batches.filter { it.status == BatchStatus.COMPLETED }
            
            var totalCocoonsValue = 0f
            completed.forEach { batch ->
                batch.cocoonsEst?.let {
                    val numeric = it.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: 0f
                    totalCocoonsValue += numeric
                }
            }
            
            val totalCocoonsStr = if (totalCocoonsValue > 0) String.format(Locale.getDefault(), "%.1f kg", totalCocoonsValue) else "0 kg"
            
            FarmStats(
                activeBatches = active,
                totalBatches = batches.size,
                totalCocoons = totalCocoonsStr, 
                avgSurvivalRate = if (batches.isNotEmpty()) "94%" else "0%" 
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FarmStats(0, 0, "0 kg", "0%")
        )

    private val _isLocationLoading = MutableStateFlow(false)
    val isLocationLoading: StateFlow<Boolean> = _isLocationLoading.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val firebaseUser = repository.currentUser
        if (firebaseUser != null) {
            viewModelScope.launch {
                repository.getUserData(firebaseUser.uid).onSuccess { user ->
                    _realUser.value = user
                }.onFailure {
                    _error.value = it.message ?: "Failed to load profile"
                }
            }
        }
    }

    fun saveLocalProfileImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isUploadingImage.value = true
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.filesDir, "profile_image.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                
                val localUri = Uri.fromFile(file).toString()
                languageManager.saveProfileImageUri(localUri)
                
            } catch (e: Exception) {
                _error.value = "Failed to save image locally: ${e.message}"
            } finally {
                _isUploadingImage.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun fetchCurrentLocation(context: Context, onLocationFetched: (String, Double, Double) -> Unit, onError: (String) -> Unit) {
        val locationHelper = LocationHelper(context)
        viewModelScope.launch {
            _isLocationLoading.value = true
            try {
                val location = locationHelper.getCurrentLocation()
                if (location != null) {
                    val response = RetrofitClient.nominatimApi.reverseGeocode(location.latitude, location.longitude)
                    val addr = response.address
                    val addressString = listOfNotNull(
                        addr.village ?: addr.town ?: addr.city,
                        addr.state_district,
                        addr.state,
                        addr.postcode
                    ).joinToString(", ")
                    
                    onLocationFetched(addressString, location.latitude, location.longitude)
                } else {
                    onError("Could not get GPS location. Ensure GPS is on.")
                }
            } catch (e: Exception) {
                onError("Error: ${e.localizedMessage ?: "Unknown error"}")
            } finally {
                _isLocationLoading.value = false
            }
        }
    }

    fun updatePersonalDetails(name: String, phone: String, email: String, location: String, farmName: String, lat: Double? = null, lon: Double? = null) {
        val current = _realUser.value ?: return
        val updated = current.copy(
            name = name,
            phone = phone,
            email = if (current.isGoogleUser) email else current.email,
            location = location,
            farmName = farmName,
            latitude = lat ?: current.latitude,
            longitude = lon ?: current.longitude
        )
        viewModelScope.launch {
            repository.saveUserData(updated).onSuccess {
                loadUserProfile()
            }.onFailure {
                _error.value = it.message ?: "Failed to save profile"
            }
        }
    }

    fun updateSettings(
        tempUnit: String? = null,
        weightUnit: String? = null,
        batchAlerts: Boolean? = null,
        climateAlerts: Boolean? = null,
        feedingReminders: Boolean? = null,
        systemNotifications: Boolean? = null,
        language: String? = null
    ) {
        val current = _realUser.value ?: return
        val updated = current.copy(
            tempUnit = tempUnit ?: current.tempUnit,
            weightUnit = weightUnit ?: current.weightUnit,
            batchAlerts = batchAlerts ?: current.batchAlerts,
            climateAlerts = climateAlerts ?: current.climateAlerts,
            feedingReminders = feedingReminders ?: current.feedingReminders,
            systemNotifications = systemNotifications ?: current.systemNotifications,
            language = language ?: current.language
        )
        viewModelScope.launch {
            repository.saveUserData(updated).onSuccess {
                loadUserProfile()
            }
        }
    }

    fun logout() {
        repository.logout()
        _realUser.value = null
    }
}
