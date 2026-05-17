package com.example.nammareshme.ui.screens

data class UserProfile(
    val name: String,
    val isVerified: Boolean,
    val phone: String,
    val email: String,
    val location: String,
    val profileImageRes: Int? = null,
    val profileImageUrl: String? = null
)

data class FarmStats(
    val activeBatches: Int,
    val totalBatches: Int,
    val totalCocoons: String,
    val avgSurvivalRate: String
)

data class SettingItem(
    val icon: Any, // ImageVector or ResId
    val title: String,
    val subtitle: String? = null,
    val trailingText: String? = null,
    val onClick: () -> Unit = {}
)
