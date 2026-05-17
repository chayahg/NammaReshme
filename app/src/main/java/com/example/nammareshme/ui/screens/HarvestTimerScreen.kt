package com.example.nammareshme.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammareshme.R
import com.example.nammareshme.ui.components.GlobalHeader
import com.example.nammareshme.ui.viewmodel.AppViewModel
import com.example.nammareshme.ui.viewmodel.BatchViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestTimerScreen(
    appViewModel: AppViewModel,
    batchViewModel: BatchViewModel,
    onBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToBatches: () -> Unit = {},
    onNavigateToClimate: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToDetails: (String) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenW = configuration.screenWidthDp.dp
    val scale = (screenW.value / 400f).coerceIn(0.85f, 1.2f)

    val currentLanguage by appViewModel.currentLanguage.collectAsState()
    val unreadNotifications by appViewModel.unreadNotifications.collectAsState()
    val batches by batchViewModel.batches.collectAsState()
    
    val activeBatch = remember(batches) { batches.firstOrNull { it.status == BatchStatus.ACTIVE } }

    var harvestAlertEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            GlobalHeader(
                scale = scale,
                showBackButton = true,
                onBackClick = onBack,
                currentLanguage = currentLanguage,
                onLanguageChange = { appViewModel.setLanguage(it) },
                unreadNotificationCount = unreadNotifications,
                onNotificationClick = onNavigateToAlerts,
                onSettingsClick = onNavigateToProfile,
                onHelpClick = { onNavigateToProfile() }
            )
        },
        bottomBar = {
            DashboardBottomNavigation(
                scale = scale,
                currentRoute = "harvest_timer",
                onHome = onNavigateToHome,
                onBatches = onNavigateToBatches,
                onClimateEntry = onNavigateToClimate,
                onAlerts = onNavigateToAlerts,
                onProfile = onNavigateToProfile
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = (16 * scale).dp)
        ) {
            Spacer(modifier = Modifier.height((12 * scale).dp))
            
            // Header Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size((48 * scale).dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size((28 * scale).dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.harvest_timer),
                        fontSize = (22 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = (28 * scale).sp
                    )
                    Text(
                        text = stringResource(R.string.harvest_timer_desc),
                        fontSize = (12 * scale).sp,
                        color = MaterialTheme.colorScheme.secondary,
                        lineHeight = (16 * scale).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height((20 * scale).dp))

            if (activeBatch != null) {
                // Calculation Logic
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val startDate = try { sdf.parse(activeBatch.startDate) } catch (e: Exception) { Date() } ?: Date()
                val calendar = Calendar.getInstance().apply { 
                    time = startDate
                    add(Calendar.DAY_OF_YEAR, 24) // 24 days average cycle
                }
                val harvestDate = calendar.time
                val diffInMillis = harvestDate.time - System.currentTimeMillis()
                val daysLeft = (TimeUnit.MILLISECONDS.toDays(diffInMillis)).coerceAtLeast(0).toInt()
                val totalProgress = ((24 - daysLeft).toFloat() / 24f).coerceIn(0f, 1f)

                val stages = listOf(
                    StageData(stringResource(R.string.instar_1), "1-3"),
                    StageData(stringResource(R.string.instar_2), "4-5"),
                    StageData(stringResource(R.string.instar_3), "6-8"),
                    StageData(stringResource(R.string.instar_4), "9-12"),
                    StageData(stringResource(R.string.instar_5), "13-18"),
                    StageData(stringResource(R.string.cocooning), "19-24")
                )
                
                val currentDay = (24 - daysLeft).coerceIn(1, 24)
                val currentStageIndex = when {
                    currentDay <= 3 -> 0
                    currentDay <= 5 -> 1
                    currentDay <= 8 -> 2
                    currentDay <= 12 -> 3
                    currentDay <= 18 -> 4
                    else -> 5
                }

                // Main Active Batch Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding((16 * scale).dp)) {
                        Text(
                            text = stringResource(R.string.active_batch),
                            fontSize = (14 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Batch Details Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size((50 * scale).dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_silkworm),
                                    contentDescription = null,
                                    modifier = Modifier.size((30 * scale).dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.batches) + " - ${activeBatch.id}",
                                    fontSize = (14 * scale).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                                Text(
                                    text = stringResource(R.string.breed_label) + " ${activeBatch.breed}",
                                    fontSize = (12 * scale).sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1
                                )
                                Text(
                                    text = stringResource(R.string.start_date_c) + " ${activeBatch.startDate}",
                                    fontSize = (12 * scale).sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                                modifier = Modifier.clickable { onNavigateToDetails(activeBatch.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        stringResource(R.string.view_details),
                                        fontSize = (10 * scale).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1
                                    )
                                    Icon(Icons.Default.ChevronRight, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(24.dp))

                        // Progress Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.current_instar), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center)
                                Text(stages[currentStageIndex].name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.day_x_of_y, currentDay, 24),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                                CircularProgressWithLabel(
                                    progress = totalProgress, 
                                    label = "${(totalProgress * 100).toInt()}%", 
                                    scale = scale
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.est_harvest), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center)
                                Text(sdf.format(harvestDate), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.Center) {
                                    Icon(Icons.Default.Event, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("$daysLeft " + stringResource(R.string.days_left), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height((30 * scale).dp))

                        // Horizontal Progress Timeline
                        InstarHorizontalTimeline(
                            currentStageIndex = currentStageIndex, 
                            stages = stages,
                            scale = scale
                        )
                    }
                }

                Spacer(modifier = Modifier.height((24 * scale).dp))

                // Lower Content
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Vertical Timeline (Summarized)
                    Column(modifier = Modifier.weight(1.4f)) {
                        Text(
                            text = stringResource(R.string.timeline),
                            fontSize = (15 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val timelineItems = stages.mapIndexed { index, stage ->
                            val status = when {
                                index < currentStageIndex -> TimelineStatus.COMPLETED
                                index == currentStageIndex -> TimelineStatus.IN_PROGRESS
                                else -> TimelineStatus.UPCOMING
                            }
                            TimelineData(stage.name, stage.days, status)
                        }

                        timelineItems.forEachIndexed { index, item ->
                            VerticalTimelineItem(item, isLast = index == timelineItems.size - 1, scale = scale)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Sidebar: Tips and Alerts
                    Column(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lightbulb, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.key_tips), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                TipItem(Icons.Default.Thermostat, "24°C-28°C", Color(0xFFE57373))
                                TipItem(Icons.Default.WaterDrop, "65%-85%", Color(0xFF42A5F5))
                                TipItem(Icons.Default.Eco, stringResource(R.string.matured_leaves), Color(0xFF66BB6A))
                                TipItem(Icons.Default.Air, stringResource(R.string.adv_vent_title), Color(0xFF26A69A))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.NotificationsActive, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.harvest_alert), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, lineHeight = 16.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.harvest_alert_desc),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    lineHeight = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Switch(
                                    checked = harvestAlertEnabled,
                                    onCheckedChange = { harvestAlertEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.scale(0.8f).align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            } else {
                // Empty state if no active batch
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.no_active_batch_tracking), color = MaterialTheme.colorScheme.secondary)
                        TextButton(onClick = { onNavigateToBatches() }) {
                            Text(stringResource(R.string.start_new_batch), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        stringResource(R.string.harvest_info_footer),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CircularProgressWithLabel(progress: Float, label: String, scale: Float) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size((80 * scale).dp)) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 6.dp,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            strokeCap = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = (18 * scale).sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.completed_tab),
                fontSize = (8 * scale).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun InstarHorizontalTimeline(currentStageIndex: Int, stages: List<StageData>, scale: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = (20 * scale).dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                thickness = 2.dp
            )
            
            val totalStages = if (stages.size > 1) stages.size - 1 else 1
            val progressWidth = (currentStageIndex.toFloat() / totalStages).coerceIn(0f, 1f)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressWidth)
                    .padding(horizontal = (20 * scale).dp)
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                stages.forEachIndexed { index, _ ->
                    val isCompleted = index < currentStageIndex
                    val isCurrent = index == currentStageIndex
                    
                    Box(
                        modifier = Modifier
                            .size((20 * scale).dp)
                            .background(
                                if (isCompleted || isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                CircleShape
                            )
                            .border(
                                1.dp,
                                if (isCompleted || isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(Icons.Default.Check, null, Modifier.size((12 * scale).dp), tint = MaterialTheme.colorScheme.onPrimary)
                        } else if (isCurrent) {
                            Box(modifier = Modifier.size((6 * scale).dp).background(MaterialTheme.colorScheme.surface, CircleShape))
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stages.forEachIndexed { index, stage ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width((60 * scale).dp)) {
                    Text(
                        text = stage.name,
                        fontSize = (8 * scale).sp,
                        fontWeight = if (index == currentStageIndex) FontWeight.Bold else FontWeight.Normal,
                        color = if (index <= currentStageIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        lineHeight = (10 * scale).sp
                    )
                    Text(
                        text = stage.days,
                        fontSize = (7 * scale).sp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun VerticalTimelineItem(data: TimelineData, isLast: Boolean, scale: Float) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size((24 * scale).dp)
                    .background(
                        when (data.status) {
                            TimelineStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                            TimelineStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            TimelineStatus.UPCOMING -> MaterialTheme.colorScheme.surface
                        },
                        CircleShape
                    )
                    .border(
                        1.dp,
                        when (data.status) {
                            TimelineStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                            TimelineStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                            TimelineStatus.UPCOMING -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (data.status) {
                    TimelineStatus.COMPLETED -> Icon(Icons.Default.Check, null, Modifier.size((14 * scale).dp), tint = MaterialTheme.colorScheme.onPrimary)
                    TimelineStatus.IN_PROGRESS -> Icon(Icons.Default.HourglassEmpty, null, Modifier.size((12 * scale).dp), tint = MaterialTheme.colorScheme.primary)
                    TimelineStatus.UPCOMING -> {
                        if (data.title.contains(stringResource(R.string.cocooning)) || data.title.contains("Cocooning")) {
                             Icon(painterResource(R.drawable.ic_silkworm), null, Modifier.size((12 * scale).dp), tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                        } else {
                             Icon(Icons.Default.HourglassEmpty, null, Modifier.size((12 * scale).dp), tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                        }
                    }
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height((40 * scale).dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.padding(bottom = (16 * scale).dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.title,
                        fontSize = (12 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = if (data.status == TimelineStatus.UPCOMING) MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary,
                        lineHeight = 14.sp
                    )
                    Text(
                        text = data.date,
                        fontSize = (10 * scale).sp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when (data.status) {
                        TimelineStatus.COMPLETED -> Color.Transparent
                        TimelineStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        TimelineStatus.UPCOMING -> Color.Transparent
                    }
                ) {
                    val statusLabel = when(data.status) {
                        TimelineStatus.COMPLETED -> stringResource(R.string.completed_tab)
                        TimelineStatus.IN_PROGRESS -> stringResource(R.string.batch_in_progress)
                        TimelineStatus.UPCOMING -> stringResource(R.string.upcoming_label)
                    }
                    Text(
                        text = statusLabel,
                        fontSize = (9 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = when (data.status) {
                            TimelineStatus.COMPLETED -> Color(0xFF43A047)
                            TimelineStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                            TimelineStatus.UPCOMING -> Color(0xFF1E88E5)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TipItem(icon: ImageVector, text: String, iconColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(icon, null, Modifier.size(18.dp), tint = iconColor)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 14.sp
        )
    }
}

data class StageData(val name: String, val days: String)

data class TimelineData(
    val title: String,
    val date: String,
    val status: TimelineStatus
)

enum class TimelineStatus(val label: String) {
    COMPLETED("Completed"),
    IN_PROGRESS("In Progress"),
    UPCOMING("Upcoming")
}
