package com.example.nammareshme.ui.screens

import java.util.Date

enum class BatchStatus { COMPLETED, ACTIVE, CANCELLED }

data class BatchHistoryItem(
    val id: String = "",
    val breed: String = "",
    val mulberryType: String = "",
    val startDate: String = "",
    val endDate: String? = null,
    val totalDays: Int? = null,
    val cocoonsEst: String? = null,
    val status: BatchStatus = BatchStatus.ACTIVE,
    val progress: Float = 0f,
    val currentStage: String = "1st Instar",
    val cancelledOn: String? = null,
    val reason: String? = null,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ClimateLog(
    val id: String = "",
    val batchId: String = "",
    val temperature: Float = 0f,
    val humidity: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = "",
    val time: String = "",
    val notes: String = ""
)
