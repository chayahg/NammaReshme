package com.example.nammareshme.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammareshme.R
import com.example.nammareshme.ui.components.GlobalHeader
import com.example.nammareshme.ui.viewmodel.AppViewModel
import com.example.nammareshme.ui.viewmodel.BatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClimateTrendsScreen(
    appViewModel: AppViewModel,
    batchViewModel: BatchViewModel,
    onBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToBatches: () -> Unit = {},
    onNavigateToClimate: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenW = configuration.screenWidthDp.dp
    val scale = (screenW.value / 400f).coerceIn(0.85f, 1.2f)
    
    val currentLanguage by appViewModel.currentLanguage.collectAsState()
    val unreadNotifications by appViewModel.unreadNotifications.collectAsState()
    val climateLogs by batchViewModel.climateLogs.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    
    val chartData = remember(climateLogs, selectedTab) {
        if (climateLogs.isEmpty()) {
            val dummyTemp = listOf(24f, 26f, 28f, 31f, 29f, 27f, 25f)
            val dummyHum = listOf(70f, 68f, 65f, 62f, 68f, 75f, 80f)
            Pair(dummyTemp, dummyHum)
        } else {
            val sortedLogs = climateLogs.sortedBy { it.timestamp }
            val count = if (selectedTab == 0) 10 else 30
            val recentLogs = sortedLogs.takeLast(count)
            Pair(recentLogs.map { it.temperature }, recentLogs.map { it.humidity })
        }
    }

    val stabilityScore = remember(climateLogs) {
        if (climateLogs.isEmpty()) 88 else {
            val inRange = climateLogs.count { it.temperature in 24f..28f && it.humidity in 65f..85f }
            (inRange.toFloat() / climateLogs.size * 100).toInt().coerceIn(0, 100)
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
                onClimateEntry = onNavigateToClimate,
                onReports = { },
                onProfile = onNavigateToProfile
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = (20 * scale).dp),
            verticalArrangement = Arrangement.spacedBy((20 * scale).dp)
        ) {
            item {
                Spacer(modifier = Modifier.height((10 * scale).dp))
                Text(
                    text = stringResource(R.string.climate_analysis), 
                    fontSize = (22 * scale).sp, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = MaterialTheme.colorScheme.primary, 
                    lineHeight = (28 * scale).sp
                )
                Text(
                    text = stringResource(R.string.track_farm_env), 
                    fontSize = (14 * scale).sp, 
                    color = MaterialTheme.colorScheme.secondary, 
                    lineHeight = (18 * scale).sp
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth().height((48 * scale).dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                        FilterTab(stringResource(R.string.daily_trend), selectedTab == 0, { selectedTab = 0 }, Modifier.weight(1f))
                        FilterTab(stringResource(R.string.weekly_overview), selectedTab == 1, { selectedTab = 1 }, Modifier.weight(1f))
                    }
                }
            }

            item { StabilityScoreCard(score = stabilityScore, scale = scale) }

            item {
                TrendChartCard(
                    title = stringResource(R.string.temp_label),
                    points = chartData.first,
                    color = Color(0xFFE67E22),
                    yRange = 15f..40f,
                    scale = scale
                )
            }

            item {
                TrendChartCard(
                    title = stringResource(R.string.hum_label),
                    points = chartData.second,
                    color = Color(0xFF3498DB),
                    yRange = 30f..100f,
                    scale = scale
                )
            }

            item { ClimateInsightCard(scale = scale) }

            item { 
                Text(
                    text = stringResource(R.string.recent_logs), 
                    fontSize = (18 * scale).sp, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.primary, 
                    lineHeight = (22 * scale).sp
                ) 
            }

            if (climateLogs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.no_active_alerts), color = MaterialTheme.colorScheme.secondary)
                    }
                }
            } else {
                items(climateLogs.take(15)) { log -> ClimateLogItem(log, scale = scale) }
            }

            item { Spacer(modifier = Modifier.height((20 * scale).dp)) }
        }
    }
}

@Composable
fun FilterTab(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text, 
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary, 
            fontSize = 13.sp, 
            fontWeight = FontWeight.Bold, 
            textAlign = TextAlign.Center, 
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun StabilityScoreCard(score: Int, scale: Float) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding((20 * scale).dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size((70 * scale).dp)) {
                CircularProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 8.dp,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
                Text(text = "$score%", fontSize = (18 * scale).sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width((20 * scale).dp))
            Column {
                Text(text = stringResource(R.string.climate_stability), fontSize = (16 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, lineHeight = (20 * scale).sp)
                Text(text = stringResource(R.string.env_stable_ideal), fontSize = (13 * scale).sp, color = MaterialTheme.colorScheme.secondary, lineHeight = (18 * scale).sp)
            }
        }
    }
}

@Composable
fun TrendChartCard(title: String, points: List<Float>, color: Color, yRange: ClosedFloatingPointRange<Float>, scale: Float) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = (220 * scale).dp)
    ) {
        Column(modifier = Modifier.padding((18 * scale).dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = (15 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f), lineHeight = (19 * scale).sp)
                Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f), modifier = Modifier.padding(start = 8.dp)) {
                    val avg = if (points.isNotEmpty()) points.average().toInt() else 0
                    Text(text = stringResource(R.string.avg_format, avg), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = (11 * scale).sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
                }
            }
            Spacer(modifier = Modifier.height((16 * scale).dp))
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                if (points.size < 2) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.no_weather_data_warning), color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                    }
                } else {
                    Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 10.dp)) {
                        val width = size.width
                        val height = size.height
                        val spacing = width / (points.size - 1)
                        val maxVal = yRange.endInclusive
                        val minVal = yRange.start
                        val range = if (maxVal == minVal) 1f else (maxVal - minVal)

                        val path = Path()
                        points.forEachIndexed { index, value ->
                            val x = index * spacing
                            val y = height - ((value - minVal) / range * height).coerceIn(0f, height)
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        val fillPath = Path().apply { 
                            addPath(path)
                            lineTo(width, height)
                            lineTo(0f, height)
                            close()
                        }
                        drawPath(path = fillPath, brush = Brush.verticalGradient(colors = listOf(color.copy(alpha = 0.3f), Color.Transparent)))
                        drawPath(path = path, color = color, style = Stroke(width = 3.dp.toPx()))
                        points.forEachIndexed { index, value ->
                            val x = index * spacing
                            val y = height - ((value - minVal) / range * height).coerceIn(0f, height)
                            drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(x, y))
                            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClimateInsightCard(scale: Float) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding((18 * scale).dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size((40 * scale).dp), shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Psychology, null, tint = Color.White, modifier = Modifier.size((24 * scale).dp)) }
            }
            Spacer(modifier = Modifier.width((16 * scale).dp))
            Column {
                Text(text = stringResource(R.string.ai_smart_tip), fontSize = (14 * scale).sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                Text(text = stringResource(R.string.rising_hum_tip), fontSize = (14 * scale).sp, color = Color.White, fontWeight = FontWeight.Medium, lineHeight = (18 * scale).sp)
            }
        }
    }
}

@Composable
fun ClimateLogItem(log: ClimateLog, scale: Float) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding((16 * scale).dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(log.time, fontSize = (14 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                Text(log.date, fontSize = (12 * scale).sp, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Thermostat, null, modifier = Modifier.size((16 * scale).dp), tint = Color(0xFFE67E22))
                Text(stringResource(R.string.unit_celsius, log.temperature.toInt().toString()), fontSize = (15 * scale).sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width((12 * scale).dp))
                Icon(Icons.Default.WaterDrop, null, modifier = Modifier.size((16 * scale).dp), tint = Color(0xFF3498DB))
                Text(stringResource(R.string.unit_percent, log.humidity.toInt().toString()), fontSize = (15 * scale).sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
