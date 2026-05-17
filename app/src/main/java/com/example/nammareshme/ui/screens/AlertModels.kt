package com.example.nammareshme.ui.screens

import androidx.compose.ui.graphics.Color

data class AlertItem(
    val id: Int,
    val titleRes: Int,
    val descRes: Int,
    val detailFormatRes: Int? = null,
    val detailArgs: List<String> = emptyList(),
    val time: String,
    val dateRes: Int,
    val type: AlertType,
    val icon: Any,
    val route: String,
    val value: String = "",
    val batchId: String = ""
)

enum class AlertType(val color: Color, val bgColor: Color) {
    CRITICAL(Color(0xFFD32F2F), Color(0xFFFFEBEE)),
    WARNING(Color(0xFFF57C00), Color(0xFFFFF3E0)),
    INFO(Color(0xFF1976D2), Color(0xFFE3F2FD)),
    ALL_CLEAR(Color(0xFF388E3C), Color(0xFFE8F5E9))
}
