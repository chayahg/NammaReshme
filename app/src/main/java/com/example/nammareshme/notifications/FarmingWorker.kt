package com.example.nammareshme.notifications

import android.content.Context
import android.content.res.Configuration
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nammareshme.R
import com.example.nammareshme.data.BatchRepository
import com.example.nammareshme.data.LanguageManager
import com.example.nammareshme.ui.screens.BatchStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class FarmingWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val notificationHelper = NotificationHelper(context)
    private val batchRepository = BatchRepository()
    private val languageManager = LanguageManager(context)

    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) return Result.success()

        val language = languageManager.getLanguage.first()
        val locale = if (language == "Kannada") Locale("kn") else Locale("en")
        
        val config = Configuration(applicationContext.resources.configuration)
        config.setLocale(locale)
        val localizedContext = applicationContext.createConfigurationContext(config)

        return try {
            var alertsFound = false
            
            // 1. Check Harvest Reminders
            if (checkHarvestReminders(localizedContext)) alertsFound = true

            // 2. Check Climate Alerts
            if (checkClimateAlerts(localizedContext)) alertsFound = true
            
            if (alertsFound) {
                val currentCount = languageManager.getUnreadCount.first()
                // Only increment if we are not already alerting (to avoid spam)
                if (currentCount == 0) {
                    languageManager.setUnreadCount(1)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun checkHarvestReminders(context: Context): Boolean {
        val batches = batchRepository.getBatches().first()
        val activeBatch = batches.find { it.status == BatchStatus.ACTIVE } ?: return false

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val startDate = try { sdf.parse(activeBatch.startDate) } catch (e: Exception) { null } ?: return false
        
        val calendar = Calendar.getInstance().apply {
            time = startDate
            add(Calendar.DAY_OF_YEAR, 24)
        }
        val harvestDate = calendar.time
        val diff = harvestDate.time - System.currentTimeMillis()
        val daysLeft = TimeUnit.MILLISECONDS.toDays(diff)

        // Fixed range to include harvest day (0)
        if (daysLeft in 0..2) {
            notificationHelper.showNotification(
                context.getString(R.string.notif_harvest_title),
                context.getString(R.string.notif_harvest_msg, activeBatch.id, daysLeft),
                101
            )
            return true
        }
        return false
    }

    private suspend fun checkClimateAlerts(context: Context): Boolean {
        val logs = batchRepository.getClimateLogs().first()
        val latestLog = logs.firstOrNull() ?: return false
        
        // Extended stale check to 12 hours for better coverage
        if (System.currentTimeMillis() - latestLog.timestamp > 12 * 3600 * 1000) return false

        var alertTriggered = false
        // Fixed: Independent checks and aligned thresholds with AlertsScreen/Dashboard
        if (latestLog.temperature > 28.5) {
            notificationHelper.showNotification(
                context.getString(R.string.notif_climate_temp_title),
                context.getString(R.string.notif_climate_temp_msg, latestLog.temperature),
                102
            )
            alertTriggered = true
        } 
        
        if (latestLog.humidity < 65) {
            notificationHelper.showNotification(
                context.getString(R.string.notif_climate_hum_title),
                context.getString(R.string.notif_climate_hum_msg, latestLog.humidity),
                103
            )
            alertTriggered = true
        }
        return alertTriggered
    }
}
