package com.example.nammareshme.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammareshme.R
import com.example.nammareshme.ui.components.GlobalHeader
import com.example.nammareshme.ui.viewmodel.AppViewModel
import com.example.nammareshme.ui.viewmodel.BatchViewModel
import com.example.nammareshme.ui.viewmodel.WeatherUiState
import com.example.nammareshme.ui.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClimateEntryScreen(
    appViewModel: AppViewModel,
    weatherViewModel: WeatherViewModel,
    batchViewModel: BatchViewModel,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToBatches: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenW = configuration.screenWidthDp.dp
    val scale = (screenW.value / 400f).coerceIn(0.85f, 1.2f)

    val currentLanguage by appViewModel.currentLanguage.collectAsState()
    val unreadNotifications by appViewModel.unreadNotifications.collectAsState()
    val weatherState by weatherViewModel.weatherState.collectAsState()
    val batches by batchViewModel.batches.collectAsState()
    
    val activeBatch = remember(batches) { batches.firstOrNull { it.status == BatchStatus.ACTIVE } }

    // UI State - Consistent date format with AlertsScreen
    var dateText by remember { mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())) }
    var timeText by remember { mutableStateOf(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())) }
    var temperature by remember { mutableFloatStateOf(26f) }
    var humidity by remember { mutableFloatStateOf(70f) }
    var notes by remember { mutableStateOf("") }

    // Update with live weather if available
    LaunchedEffect(weatherState) {
        if (weatherState is WeatherUiState.Success) {
            val weather = (weatherState as WeatherUiState.Success).weather
            temperature = weather.main.temp.toFloat()
            humidity = weather.main.humidity.toFloat()
        }
    }

    // Dialog Control
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()

    Scaffold(
        topBar = {
            GlobalHeader(
                scale = scale,
                showBackButton = true,
                onBackClick = onBack,
                currentLanguage = currentLanguage,
                onLanguageChange = { appViewModel.setLanguage(it) },
                unreadNotificationCount = unreadNotifications,
                onNotificationClick = { },
                onSettingsClick = onNavigateToProfile,
                onHelpClick = { onNavigateToProfile() }
            )
        },
        bottomBar = {
            DashboardBottomNavigation(
                scale = scale,
                currentRoute = "climate_entry",
                onHome = onNavigateToHome,
                onBatches = onNavigateToBatches,
                onReports = onNavigateToReports,
                onProfile = onNavigateToProfile
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = (22 * scale).dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height((12 * scale).dp))

            Text(
                text = stringResource(R.string.climate_entry),
                fontSize = (22 * scale).sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = (28 * scale).sp
            )
            Text(
                text = stringResource(R.string.climate_entry_desc),
                fontSize = (12 * scale).sp,
                color = MaterialTheme.colorScheme.secondary,
                lineHeight = (16 * scale).sp
            )

            Spacer(modifier = Modifier.height((20 * scale).dp))

            if (activeBatch != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE3F2FD).copy(alpha = if (MaterialTheme.colorScheme.surface == Color.White) 1f else 0.1f),
                    border = BorderStroke(1.dp, Color(0xFF1976D2).copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = if (MaterialTheme.colorScheme.surface == Color.White) Color(0xFF1976D2) else Color(0xFF90CAF9))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.active_batch) + ": ${activeBatch.id}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (MaterialTheme.colorScheme.surface == Color.White) Color(0xFF1976D2) else Color(0xFF90CAF9),
                            lineHeight = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height((20 * scale).dp))
            }

            // --- LIVE WEATHER INFO ---
            if (weatherState is WeatherUiState.Success) {
                val weather = (weatherState as WeatherUiState.Success).weather
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Cloud, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(stringResource(R.string.live_weather), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("${weather.main.temp}°C, ${weather.main.humidity}% " + stringResource(R.string.humidity) + ", ${weather.wind.speed} m/s " + stringResource(R.string.wind), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), lineHeight = 14.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height((20 * scale).dp))
            }

            // --- DATE & TIME SELECTION ---
            SectionLabel(stringResource(R.string.log_schedule))
            Row(modifier = Modifier.fillMaxWidth()) {
                SelectionCard(
                    icon = Icons.Default.CalendarToday,
                    label = stringResource(R.string.date_label),
                    value = dateText,
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                SelectionCard(
                    icon = Icons.Default.Schedule,
                    label = stringResource(R.string.time_label),
                    value = timeText,
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height((26 * scale).dp))

            // --- CLIMATE INPUTS ---
            SectionLabel(stringResource(R.string.env_monitoring))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Temperature Control
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Thermostat, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.temp_label), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("${temperature.toInt()}°C", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = temperature,
                        onValueChange = { temperature = it },
                        valueRange = 15f..40f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Humidity Control
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, null, tint = Color(0xFF1565C0), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.hum_label), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("${humidity.toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF1565C0))
                    }
                    Slider(
                        value = humidity,
                        onValueChange = { humidity = it },
                        valueRange = 30f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF1565C0),
                            activeTrackColor = Color(0xFF1565C0),
                            inactiveTrackColor = Color(0xFFE3F2FD).copy(alpha = 0.2f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height((26 * scale).dp))

            // --- DYNAMIC SUMMARY ---
            SectionLabel(stringResource(R.string.condition_analysis))
            val summary = calculateClimateSummary(temperature, humidity)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = summary.color.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, summary.color.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(summary.icon, null, tint = summary.color, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(stringResource(summary.titleRes), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = summary.color)
                        Text(stringResource(summary.descRes), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), lineHeight = 17.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height((26 * scale).dp))

            // --- NOTES ---
            SectionLabel(stringResource(R.string.notes_label))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                placeholder = { Text(stringResource(R.string.notes_placeholder), fontSize = 14.sp) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height((32 * scale).dp))

            // --- ACTIONS ---
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = (32 * scale).dp)) {
                OutlinedButton(
                    onClick = {
                        temperature = 26f
                        humidity = 70f
                        notes = ""
                    },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        val log = ClimateLog(
                            batchId = activeBatch?.id ?: "unknown",
                            temperature = temperature,
                            humidity = humidity,
                            date = dateText,
                            time = timeText,
                            notes = notes
                        )
                        batchViewModel.addClimateLog(log, {
                            Toast.makeText(context, context.getString(R.string.save_success), Toast.LENGTH_SHORT).show()
                            onSaveSuccess()
                        }, { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        })
                    },
                    modifier = Modifier.weight(1.5f).height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.log_entry), fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }

    // --- PICKER DIALOGS ---
    if (showDatePicker) {
        val confirmText = stringResource(R.string.confirm)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dateText = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
                    }
                    showDatePicker = false
                }) { Text(confirmText, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    timeText = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
                    showTimePicker = false
                }) { Text(stringResource(R.string.set_time), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 10.dp),
        lineHeight = 19.sp
    )
}

@Composable
private fun SelectionCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 12.sp)
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, lineHeight = 16.sp)
            }
        }
    }
}

private data class SummaryData(val titleRes: Int, val descRes: Int, val color: Color, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private fun calculateClimateSummary(temp: Float, hum: Float): SummaryData {
    return when {
        temp in 24f..28f && hum in 60f..85f -> 
            SummaryData(R.string.cond_optimal, R.string.cond_optimal_desc, Color(0xFF2E7D32), Icons.Default.CheckCircle)
        temp > 30f || hum > 90f -> 
            SummaryData(R.string.cond_high_risk, R.string.cond_high_risk_desc, Color(0xFFD32F2F), Icons.Default.Warning)
        else -> 
            SummaryData(R.string.cond_moderate, R.string.cond_moderate_desc, Color(0xFFF9A825), Icons.Default.Info)
    }
}
