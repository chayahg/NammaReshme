package com.example.nammareshme.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammareshme.R
import com.example.nammareshme.ui.components.GlobalHeader
import com.example.nammareshme.ui.viewmodel.AppViewModel
import com.example.nammareshme.ui.viewmodel.BatchViewModel
import com.example.nammareshme.utils.LocalizationUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailsScreen(
    appViewModel: AppViewModel,
    batchViewModel: BatchViewModel,
    batchId: String,
    onBack: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenW = configuration.screenWidthDp.dp
    val scale = (screenW.value / 411f).coerceIn(0.85f, 1.0f)

    val currentLanguage by appViewModel.currentLanguage.collectAsState()
    val unreadNotifications by appViewModel.unreadNotifications.collectAsState()
    val batches by batchViewModel.batches.collectAsState()
    val climateLogs by batchViewModel.climateLogs.collectAsState()

    val batch = remember(batches, batchId) { batches.find { it.id == batchId } }
    val logs = remember(climateLogs, batchId) { climateLogs.filter { it.batchId == batchId } }

    Scaffold(
        topBar = {
            GlobalHeader(
                scale = scale,
                showBackButton = true,
                onBackClick = onBack,
                currentLanguage = currentLanguage,
                onLanguageChange = { appViewModel.setLanguage(it) },
                unreadNotificationCount = unreadNotifications,
                onNotificationClick = { }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (batch == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.batch_not_found), color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = (18 * scale).dp)
            ) {
                Spacer(modifier = Modifier.height((12 * scale).dp))
                
                Text(
                    text = stringResource(R.string.batch_details_title),
                    fontSize = (22 * scale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = (28 * scale).sp
                )

                Spacer(modifier = Modifier.height((16 * scale).dp))

                // Premium Header Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size((52 * scale).dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_silkworm),
                                    contentDescription = null,
                                    modifier = Modifier.size((34 * scale).dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = batch.id,
                                    fontSize = (18 * scale).sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    lineHeight = 22.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadgeRefined(batch.status, scale)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        stringResource(R.string.breed_label) + " " + LocalizationUtils.getLocalizedBreed(batch.breed),
                                        fontSize = (11 * scale).sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailInfo(Icons.Default.CalendarToday, stringResource(R.string.start_date_label), batch.startDate, Modifier.weight(1f), scale)
                            DetailInfo(Icons.Default.EventAvailable, stringResource(R.string.end_date_c), batch.endDate ?: stringResource(R.string.tbd), Modifier.weight(1f), scale)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailInfo(Icons.Default.Timer, stringResource(R.string.current_stage_label), LocalizationUtils.getLocalizedStage(batch.currentStage), Modifier.weight(1f), scale)
                            DetailInfo(Icons.Default.Eco, stringResource(R.string.mulberry_type_label), LocalizationUtils.getLocalizedMulberry(batch.mulberryType), Modifier.weight(1f), scale)
                        }
                    }
                }

                Spacer(modifier = Modifier.height((20 * scale).dp))

                // Section: Climate Logs Summary (Real Data)
                if (logs.isNotEmpty()) {
                    SubSectionTitle(stringResource(R.string.climate_logs_summary), scale)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            val avgTemp = logs.map { it.temperature }.average()
                            val avgHum = logs.map { it.humidity }.average()
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                ClimateStatItem(stringResource(R.string.avg_temp), String.format(Locale.getDefault(), "%.1f°C", avgTemp), Icons.Default.Thermostat, Color(0xFFE57373), scale)
                                ClimateStatItem(stringResource(R.string.avg_humidity), "${avgHum.toInt()}%", Icons.Default.WaterDrop, Color(0xFF64B5F6), scale)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    stringResource(R.string.total_logs_format, logs.size, logs.first().date, logs.first().time),
                                    fontSize = (10 * scale).sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(8.dp),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height((20 * scale).dp))
                }

                // Section: Notes & Observations
                if (batch.notes.isNotBlank()) {
                    SubSectionTitle(stringResource(R.string.notes_observations), scale)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                batch.notes,
                                fontSize = (11 * scale).sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SubSectionTitle(title: String, scale: Float) {
    Text(
        text = title,
        fontSize = (14 * scale).sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 10.dp, start = 2.dp),
        lineHeight = 18.sp
    )
}

@Composable
fun DetailInfo(icon: ImageVector, label: String, value: String, modifier: Modifier, scale: Float) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size((28 * scale).dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            val labelStr = label.replace("*", "").trim()
            Text(labelStr, fontSize = (9 * scale).sp, color = MaterialTheme.colorScheme.secondary, lineHeight = (11 * scale).sp)
            Text(value, fontSize = (11 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, lineHeight = 14.sp)
        }
    }
}

@Composable
fun ClimateStatItem(label: String, value: String, icon: ImageVector, color: Color, scale: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size((36 * scale).dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, fontSize = (14 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, lineHeight = 18.sp)
        Text(label, fontSize = (9 * scale).sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 12.sp)
    }
}
