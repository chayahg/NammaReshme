package com.example.nammareshme.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartAdviceScreen(
    appViewModel: AppViewModel,
    weatherViewModel: WeatherViewModel,
    batchViewModel: BatchViewModel,
    onBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToBatches: () -> Unit = {},
    onNavigateToHarvestTimer: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToClimateEntry: () -> Unit = {},
    onRecommendationClick: (String) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenW = configuration.screenWidthDp.dp
    val scale = (screenW.value / 400f).coerceIn(0.85f, 1.2f)

    val currentLanguage by appViewModel.currentLanguage.collectAsState()
    val unreadNotifications by appViewModel.unreadNotifications.collectAsState()
    val weatherState by weatherViewModel.weatherState.collectAsState()
    val climateLogs by batchViewModel.climateLogs.collectAsState()

    val latestLog = climateLogs.firstOrNull()
    var temperature by remember { mutableFloatStateOf(26.0f) }
    var humidity by remember { mutableFloatStateOf(70f) }
    var lastUpdated by remember { mutableStateOf("") }
    var lastUpdatedRes by remember { mutableIntStateOf(R.string.just_now) }

    LaunchedEffect(weatherState, latestLog) {
        if (latestLog != null) {
            temperature = latestLog.temperature
            humidity = latestLog.humidity
            lastUpdated = "${latestLog.time}, ${latestLog.date}"
            lastUpdatedRes = 0 
        } else if (weatherState is WeatherUiState.Success) {
            val weather = (weatherState as WeatherUiState.Success).weather
            temperature = weather.main.temp.toFloat()
            humidity = weather.main.humidity.toFloat()
            lastUpdated = ""
            lastUpdatedRes = R.string.live_weather
        }
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
                onNotificationClick = { }
            )
        },
        bottomBar = {
            DashboardBottomNavigation(
                scale = scale,
                currentRoute = "reports",
                onHome = onNavigateToHome,
                onBatches = onNavigateToBatches,
                onHarvestTimer = onNavigateToHarvestTimer,
                onReports = onNavigateToReports,
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
                .padding(horizontal = (20 * scale).dp)
        ) {
            Spacer(modifier = Modifier.height((12 * scale).dp))

            // --- HEADER ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size((48 * scale).dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size((24 * scale).dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.smart_advice),
                        fontSize = (22 * scale).sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = (28 * scale).sp
                    )
                    Text(
                        text = stringResource(R.string.smart_advice_desc),
                        fontSize = (12 * scale).sp,
                        color = MaterialTheme.colorScheme.secondary,
                        lineHeight = (16 * scale).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height((20 * scale).dp))

            // --- CLIMATE SUMMARY CARD ---
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AdviceStatItem(Icons.Default.Thermostat, stringResource(R.string.temperature), stringResource(R.string.unit_celsius, temperature.toInt().toString()), Color(0xFFE67E22), scale, temperature in 24f..28.5f)
                    Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)))
                    AdviceStatItem(Icons.Default.WaterDrop, stringResource(R.string.humidity_label), stringResource(R.string.unit_percent, humidity.toInt().toString()), Color(0xFF3498DB), scale, humidity in 65f..85f)
                    Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.updated_at), fontSize = (10 * scale).sp, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center)
                        if (lastUpdatedRes != 0) {
                            Text(stringResource(lastUpdatedRes), fontSize = (13 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                        } else {
                            if (lastUpdated.contains(",")) {
                                Text(lastUpdated.substringBefore(","), fontSize = (13 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                                Text(lastUpdated.substringAfter(", ", ""), fontSize = (10 * scale).sp, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center)
                            } else {
                                Text(lastUpdated, fontSize = (13 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height((24 * scale).dp))

            // --- OVERALL STATUS ---
            Text(stringResource(R.string.overall_status), fontSize = (15 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 12.dp))
            val status = getAdviceStatus(temperature, humidity.toInt())
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = status.color.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, status.color.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size((36 * scale).dp).background(status.color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(status.icon, null, tint = Color.White, modifier = Modifier.size((20 * scale).dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(status.titleRes), fontSize = (16 * scale).sp, fontWeight = FontWeight.Bold, color = status.color, lineHeight = 20.sp)
                        Text(stringResource(status.descRes), fontSize = (13 * scale).sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), lineHeight = 17.sp)
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = status.color.copy(alpha = 0.15f),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = stringResource(status.badgeRes),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = (11 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            color = status.color,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height((24 * scale).dp))

            // --- RECOMMENDATIONS ---
            SubHeaderLabel(stringResource(R.string.recommendations), scale)
            val recommendations = getRecommendationsData(temperature, humidity.toInt())
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                recommendations.forEach { item -> 
                    AdviceListItem(item, scale, onClick = { onRecommendationClick(item.type) }) 
                }
            }

            Spacer(modifier = Modifier.height((24 * scale).dp))

            // --- ALERTS ---
            SubHeaderLabel(stringResource(R.string.alerts), scale)
            val alerts = getAlertsData(temperature, humidity.toInt())
            if (alerts.isEmpty()) {
                Text(stringResource(R.string.no_active_alerts), fontSize = (13 * scale).sp, color = Color.Gray)
            } else {
                alerts.forEach { item -> 
                    AdviceAlertItem(item, scale, onClick = { onNavigateToClimateEntry() }) 
                }
            }

            Spacer(modifier = Modifier.height((24 * scale).dp))

            // --- FOOTER BANNER ---
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VerifiedUser, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size((18 * scale).dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.advice_footer),
                        fontSize = (12 * scale).sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f),
                        lineHeight = 16.sp
                    )
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), modifier = Modifier.size((16 * scale).dp))
                }
            }
            Spacer(modifier = Modifier.height((24 * scale).dp))
        }
    }
}

@Composable
fun SubHeaderLabel(text: String, scale: Float) {
    Text(
        text = text,
        fontSize = (15 * scale).sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun AdviceStatItem(icon: ImageVector, label: String, value: String, color: Color, scale: Float, isGood: Boolean = true) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = (10 * scale).sp, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            Icon(icon, null, modifier = Modifier.size((18 * scale).dp), tint = if (isGood) color else Color(0xFFD32F2F))
            Spacer(modifier = Modifier.width(4.dp))
            Text(value, fontSize = (18 * scale).sp, fontWeight = FontWeight.Black, color = if (isGood) MaterialTheme.colorScheme.primary else Color(0xFFD32F2F))
        }
        Text(
            text = stringResource(if (isGood) R.string.good else R.string.action_required), 
            fontSize = (11 * scale).sp, 
            fontWeight = FontWeight.Bold, 
            color = if (isGood) Color(0xFF2D6A4F) else Color(0xFFD32F2F)
        )
    }
}

@Composable
fun AdviceListItem(data: AdviceItemDataRes, scale: Float, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(modifier = Modifier.size((36 * scale).dp), shape = CircleShape, color = data.color.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) { Icon(data.icon, null, tint = data.color, modifier = Modifier.size((20 * scale).dp)) }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(data.titleRes), fontSize = (14 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, lineHeight = 18.sp)
                Text(stringResource(data.subtitleRes), fontSize = (12 * scale).sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 16.sp)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray, modifier = Modifier.size((20 * scale).dp))
        }
    }
}

@Composable
fun AdviceAlertItem(data: AdviceAlertDataRes, scale: Float, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp), 
        color = data.color.copy(alpha = 0.05f), 
        border = BorderStroke(1.dp, data.color.copy(alpha = 0.15f)), 
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, null, tint = data.color, modifier = Modifier.size((22 * scale).dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(data.titleRes), fontSize = (14 * scale).sp, fontWeight = FontWeight.Bold, color = data.color, lineHeight = 18.sp)
                Text(stringResource(data.descRes), fontSize = (12 * scale).sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), lineHeight = 16.sp)
            }
            Surface(
                shape = RoundedCornerShape(12.dp), 
                color = Color.White, 
                border = BorderStroke(1.dp, data.color.copy(alpha = 0.3f)), 
                modifier = Modifier.padding(start = 8.dp).clickable { onClick() }
            ) {
                Text(
                    stringResource(data.actionRes), 
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), 
                    fontSize = (11 * scale).sp, 
                    fontWeight = FontWeight.Bold, 
                    color = data.color, 
                    maxLines = 1
                )
            }
        }
    }
}

data class AdviceItemDataRes(val type: String, val titleRes: Int, val subtitleRes: Int, val icon: ImageVector, val color: Color)
data class AdviceAlertDataRes(val titleRes: Int, val descRes: Int, val actionRes: Int, val color: Color)
data class AdviceStatusData(val titleRes: Int, val descRes: Int, val badgeRes: Int, val color: Color, val icon: ImageVector)

private fun getAdviceStatus(temp: Float, hum: Int) = when {
    temp in 24f..28.5f && hum in 65..85 -> AdviceStatusData(R.string.system_healthy, R.string.monitoring_desc, R.string.optimal, Color(0xFF2D6A4F), Icons.Default.Check)
    temp > 30f || hum < 60 || hum > 90 -> AdviceStatusData(R.string.critical, R.string.action_required_status, R.string.critical_label, Color(0xFFD32F2F), Icons.Default.Error)
    else -> AdviceStatusData(R.string.fair, R.string.attention_label, R.string.fair, Color(0xFFFFB300), Icons.Default.Info)
}

private fun getRecommendationsData(temp: Float, hum: Int): List<AdviceItemDataRes> {
    val list = mutableListOf<AdviceItemDataRes>()
    if (temp in 24f..28.5f && hum in 65..85) list.add(AdviceItemDataRes("Maintain Conditions", R.string.adv_maintain_title, R.string.adv_maintain_desc, Icons.Default.Eco, Color(0xFF2D6A4F)))
    if (temp > 28.5f) list.add(AdviceItemDataRes("Ventilation", R.string.adv_vent_title, R.string.adv_vent_desc, Icons.Default.Air, Color(0xFF3498DB)))
    list.add(AdviceItemDataRes("Cleaning", R.string.adv_clean_title_rec, R.string.adv_clean_desc_rec, Icons.Default.CleaningServices, Color(0xFFE67E22)))
    list.add(AdviceItemDataRes("Mulberry Leaves", R.string.adv_mulberry_title, R.string.adv_mulberry_desc, Icons.Default.Spa, Color(0xFF9C27B0)))
    return list
}

private fun getAlertsData(temp: Float, hum: Int): List<AdviceAlertDataRes> {
    val list = mutableListOf<AdviceAlertDataRes>()
    if (temp > 28.5f) list.add(AdviceAlertDataRes(R.string.alert_high_temp, R.string.alert_high_temp_desc, R.string.action_monitor, if (temp > 31f) Color(0xFFD32F2F) else Color(0xFFFFB300)))
    if (hum < 65) list.add(AdviceAlertDataRes(R.string.alert_low_hum_title, R.string.alert_low_hum_desc_long, R.string.action_take_action, Color(0xFFD32F2F)))
    if (hum > 85) list.add(AdviceAlertDataRes(R.string.alert_high_hum, R.string.alert_high_hum_desc, R.string.action_take_action, Color(0xFF1565C0)))
    return list
}
