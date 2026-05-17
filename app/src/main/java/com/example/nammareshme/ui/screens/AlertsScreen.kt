package com.example.nammareshme.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.example.nammareshme.ui.viewmodel.WeatherUiState
import com.example.nammareshme.ui.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    appViewModel: AppViewModel,
    batchViewModel: BatchViewModel,
    weatherViewModel: WeatherViewModel,
    onBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToBatches: () -> Unit = {},
    onNavigateToClimate: () -> Unit = {},
    onNavigateToHarvestTimer: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onAlertClick: (String) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenW = configuration.screenWidthDp.dp
    val scale = (screenW.value / 400f).coerceIn(0.85f, 1.2f)
    val isNarrow = configuration.screenWidthDp < 360

    val currentLanguage by appViewModel.currentLanguage.collectAsState()
    val unreadNotifications by appViewModel.unreadNotifications.collectAsState()
    val batches by batchViewModel.batches.collectAsState()
    val climateLogs by batchViewModel.climateLogs.collectAsState()
    val weatherState by weatherViewModel.weatherState.collectAsState()

    // Clear unread notifications when viewing the alerts screen
    LaunchedEffect(Unit) {
        appViewModel.clearUnreadCount()
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.all_tab),
        stringResource(R.string.critical_label),
        stringResource(R.string.warning_label),
        stringResource(R.string.info_label),
        stringResource(R.string.all_clear_label)
    )

    val allAlerts = remember(batches, climateLogs, weatherState) {
        val alerts = mutableListOf<AlertItem>()
        val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val sdfDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val todayStr = sdfDate.format(Date())
        
        // 1. Climate Alerts from Real Logs
        climateLogs.forEach { log ->
            if (log.temperature > 28.5) {
                alerts.add(AlertItem(
                    id = log.id.hashCode(),
                    titleRes = R.string.alert_high_temp_title,
                    descRes = R.string.alert_high_temp_desc_long,
                    detailFormatRes = R.string.ideal_range_format,
                    detailArgs = listOf("${log.temperature}°C", "24°C - 28°C"),
                    time = log.time,
                    dateRes = if (log.date == todayStr) R.string.today else R.string.yesterday,
                    type = AlertType.CRITICAL,
                    icon = Icons.Default.Thermostat,
                    route = "temp_alert_details/${log.temperature}/${log.batchId}",
                    value = log.temperature.toString(),
                    batchId = log.batchId
                ))
            }
            if (log.humidity < 65) {
                alerts.add(AlertItem(
                    id = log.id.hashCode() + 500,
                    titleRes = R.string.alert_low_hum_title,
                    descRes = R.string.alert_low_hum_desc_long,
                    detailFormatRes = R.string.ideal_range_format,
                    detailArgs = listOf("${log.humidity}%", "65% - 85%"),
                    time = log.time,
                    dateRes = if (log.date == todayStr) R.string.today else R.string.yesterday,
                    type = AlertType.WARNING,
                    icon = Icons.Default.WaterDrop,
                    route = "hum_alert_details/${log.humidity}/${log.batchId}",
                    value = log.humidity.toString(),
                    batchId = log.batchId
                ))
            }
        }

        // 2. Weather Alerts
        if (weatherState is WeatherUiState.Success) {
            val weather = (weatherState as WeatherUiState.Success).weather
            if (weather.main.temp > 28.5) {
                alerts.add(AlertItem(
                    id = 1001,
                    titleRes = R.string.high_temp_alert,
                    descRes = R.string.heat_detected_advice,
                    time = sdfTime.format(Date()),
                    dateRes = R.string.today,
                    type = AlertType.CRITICAL,
                    icon = Icons.Default.WbSunny,
                    route = "temp_alert_details/${weather.main.temp}/live",
                    value = weather.main.temp.toString(),
                    batchId = "live"
                ))
            }
        }

        // 3. Batch Progress & Harvest Reminders
        batches.filter { it.status == BatchStatus.ACTIVE }.forEach { batch ->
            // Stage update alert
            alerts.add(AlertItem(
                id = batch.id.hashCode() + 2000,
                titleRes = R.string.alert_batch_prog_title,
                descRes = R.string.alert_batch_prog_desc_long,
                detailArgs = listOf(batch.id, batch.currentStage),
                time = sdfTime.format(Date(batch.timestamp)),
                dateRes = R.string.today,
                type = AlertType.INFO,
                icon = Icons.AutoMirrored.Filled.Assignment,
                route = "batch_progress_details/${batch.currentStage}/${batch.id}",
                value = batch.currentStage,
                batchId = batch.id
            ))

            // Harvest reminder alert
            val startDate = try { sdfDate.parse(batch.startDate) } catch (e: Exception) { null }
            if (startDate != null) {
                val calendar = Calendar.getInstance().apply {
                    time = startDate
                    add(Calendar.DAY_OF_YEAR, 24)
                }
                val harvestDate = calendar.time
                val diff = harvestDate.time - System.currentTimeMillis()
                val daysLeft = TimeUnit.MILLISECONDS.toDays(diff)
                
                if (daysLeft in 0..2) {
                    alerts.add(AlertItem(
                        id = batch.id.hashCode() + 3000,
                        titleRes = R.string.notif_harvest_title,
                        descRes = R.string.notif_harvest_msg,
                        detailArgs = listOf(batch.id, daysLeft.toString()),
                        time = "System",
                        dateRes = R.string.today,
                        type = AlertType.CRITICAL,
                        icon = Icons.Default.Timer,
                        route = "timer_screen",
                        value = daysLeft.toString(),
                        batchId = batch.id
                    ))
                }
            }
        }

        alerts.sortedByDescending { it.id }
    }

    val filteredAlerts = when (selectedTab) {
        0 -> allAlerts
        1 -> allAlerts.filter { it.type == AlertType.CRITICAL }
        2 -> allAlerts.filter { it.type == AlertType.WARNING }
        3 -> allAlerts.filter { it.type == AlertType.INFO }
        4 -> allAlerts.filter { it.type == AlertType.ALL_CLEAR }
        else -> allAlerts
    }

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
                currentRoute = "alerts",
                onHome = onNavigateToHome,
                onBatches = onNavigateToBatches,
                onClimateEntry = onNavigateToClimate,
                onHarvestTimer = onNavigateToHarvestTimer,
                onReports = onNavigateToReports,
                onProfile = onNavigateToProfile
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToClimate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = (80 * scale).dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Climate Log")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = (18 * scale).dp, vertical = (4 * scale).dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(vertical = (16 * scale).dp)) {
                        Text(
                            text = stringResource(R.string.alerts),
                            fontSize = (22 * scale).sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = (28 * scale).sp
                        )
                        Text(
                            text = stringResource(R.string.alerts_desc),
                            fontSize = (12 * scale).sp,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = (16 * scale).sp
                        )
                    }
                }

                item {
                    AlertSummaryCards(allAlerts, scale, isNarrow)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = 0.dp,
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = MaterialTheme.colorScheme.primary,
                                height = 2.dp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFE0E0E0).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = (11 * scale).sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (filteredAlerts.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.NotificationsOff, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(stringResource(R.string.no_active_alerts), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                } else {
                    val groupedAlerts = filteredAlerts.groupBy { it.dateRes }
                    
                    groupedAlerts.forEach { (dateRes, alerts) ->
                        item {
                            Text(
                                text = stringResource(dateRes),
                                fontSize = (13 * scale).sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                            )
                        }
                        items(alerts) { alert ->
                            AlertCard(alert, scale, onAlertClick)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToReports() },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.History, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                stringResource(R.string.view_history),
                                fontSize = (14 * scale).sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height((32 * scale).dp))
                }
            }
        }
    }
}

@Composable
fun AlertSummaryCards(allAlerts: List<AlertItem>, scale: Float, isNarrow: Boolean) {
    if (isNarrow) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard(Modifier.weight(1f), stringResource(R.string.critical_label), allAlerts.count { it.type == AlertType.CRITICAL }.toString(), stringResource(R.string.action_required), AlertType.CRITICAL, scale)
                SummaryCard(Modifier.weight(1f), stringResource(R.string.warning_label), allAlerts.count { it.type == AlertType.WARNING }.toString(), stringResource(R.string.attention_label), AlertType.WARNING, scale)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard(Modifier.weight(1f), stringResource(R.string.info_label), allAlerts.count { it.type == AlertType.INFO }.toString(), stringResource(R.string.updates_label), AlertType.INFO, scale)
                SummaryCard(Modifier.weight(1f), stringResource(R.string.all_clear_label), allAlerts.count { it.type == AlertType.ALL_CLEAR }.toString(), stringResource(R.string.normal_label), AlertType.ALL_CLEAR, scale)
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryCard(Modifier.weight(1f), stringResource(R.string.critical_label), allAlerts.count { it.type == AlertType.CRITICAL }.toString(), stringResource(R.string.action_required), AlertType.CRITICAL, scale)
            SummaryCard(Modifier.weight(1f), stringResource(R.string.warning_label), allAlerts.count { it.type == AlertType.WARNING }.toString(), stringResource(R.string.attention_label), AlertType.WARNING, scale)
            SummaryCard(Modifier.weight(1f), stringResource(R.string.info_label), allAlerts.count { it.type == AlertType.INFO }.toString(), stringResource(R.string.updates_label), AlertType.INFO, scale)
            SummaryCard(Modifier.weight(1f), stringResource(R.string.all_clear_label), allAlerts.count { it.type == AlertType.ALL_CLEAR }.toString(), stringResource(R.string.normal_label), AlertType.ALL_CLEAR, scale)
        }
    }
}

@Composable
fun SummaryCard(modifier: Modifier, title: String, count: String, subtitle: String, type: AlertType, scale: Float) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier.padding((10 * scale).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size((28 * scale).dp)
                    .background(type.bgColor.copy(alpha = if (MaterialTheme.colorScheme.surface == Color.White) 1f else 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(type) {
                        AlertType.CRITICAL -> Icons.Default.Warning
                        AlertType.WARNING -> Icons.Default.WarningAmber
                        AlertType.INFO -> Icons.Default.Info
                        AlertType.ALL_CLEAR -> Icons.Default.Check
                    },
                    contentDescription = null,
                    modifier = Modifier.size((14 * scale).dp),
                    tint = type.color
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = (8.5 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center, lineHeight = 10.sp, maxLines = 1)
            Text(count, fontSize = (19 * scale).sp, fontWeight = FontWeight.Black, color = type.color)
            Text(subtitle, fontSize = (8 * scale).sp, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center, lineHeight = 10.sp, maxLines = 2)
        }
    }
}

@Composable
fun AlertCard(alert: AlertItem, scale: Float, onClick: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.1f), spotColor = Color.Black.copy(alpha = 0.1f))
            .clickable { onClick(alert.route) },
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, Color(0xFFF0F0F0).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = (12 * scale).dp, vertical = (12 * scale).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size((30 * scale).dp)
                    .background(alert.type.bgColor.copy(alpha = if (MaterialTheme.colorScheme.surface == Color.White) 1f else 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (alert.icon) {
                    is ImageVector -> Icon(alert.icon, null, Modifier.size((16 * scale).dp), tint = alert.type.color)
                    is Int -> Icon(painterResource(alert.icon), null, Modifier.size((16 * scale).dp), tint = alert.type.color)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = stringResource(alert.titleRes),
                        fontSize = (13.5 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        lineHeight = (18 * scale).sp
                    )
                    Text(
                        text = alert.time,
                        fontSize = (9 * scale).sp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (alert.detailArgs.isEmpty()) stringResource(alert.descRes) else stringResource(alert.descRes, *alert.detailArgs.toTypedArray()),
                    fontSize = (11 * scale).sp,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = (15 * scale).sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    if (alert.detailFormatRes != null) {
                        Text(
                            text = stringResource(alert.detailFormatRes, *alert.detailArgs.toTypedArray()),
                            fontSize = (10 * scale).sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = (14 * scale).sp
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = alert.type.color.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, alert.type.color.copy(alpha = 0.2f)),
                        modifier = Modifier.clickable { onClick(alert.route) }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(if (alert.type == AlertType.CRITICAL || alert.type == AlertType.WARNING) R.string.action_take_action else R.string.view_details),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = alert.type.color
                            )
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(10.dp), tint = alert.type.color)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                Modifier.size(18.dp),
                tint = Color(0xFFE0E0E0)
            )
        }
    }
}
