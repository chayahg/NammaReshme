package com.example.nammareshme.models

import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    @get:PropertyName("verified")
    @set:PropertyName("verified")
    var isVerified: Boolean = false,
    val location: String = "",
    val farmName: String = "",
    @get:PropertyName("googleUser")
    @set:PropertyName("googleUser")
    var isGoogleUser: Boolean = false,
    // Location coordinates
    val latitude: Double? = null,
    val longitude: Double? = null,
    // Settings persisted in Firestore
    val tempUnit: String = "Celsius (°C)",
    val weightUnit: String = "Kilograms (kg)",
    val batchAlerts: Boolean = true,
    val climateAlerts: Boolean = true,
    val feedingReminders: Boolean = true,
    val systemNotifications: Boolean = false,
    val language: String = "English"
)
