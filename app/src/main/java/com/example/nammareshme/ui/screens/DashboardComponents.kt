package com.example.nammareshme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammareshme.R

@Composable
fun DashboardBottomNavigation(
    scale: Float,
    currentRoute: String,
    onHome: () -> Unit = {},
    onBatches: () -> Unit = {},
    onClimateEntry: () -> Unit = {},
    onAlerts: () -> Unit = {},
    onReports: () -> Unit = {},
    onProfile: () -> Unit = {},
    onHarvestTimer: () -> Unit = {}
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.height((75 * scale).dp)
    ) {
        val items = listOf(
            Triple("home", Icons.Default.Home, stringResource(R.string.home)),
            Triple("batches", Icons.AutoMirrored.Filled.Assignment, stringResource(R.string.batches)),
            Triple("climate_entry", Icons.Default.Cloud, stringResource(R.string.climate)),
            Triple("alerts", Icons.Default.Notifications, stringResource(R.string.alerts)),
            Triple("profile", Icons.Default.Person, stringResource(R.string.profile))
        )

        items.forEach { (route, icon, label) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    when (route) {
                        "home" -> onHome()
                        "batches" -> onBatches()
                        "climate_entry" -> onClimateEntry()
                        "alerts" -> onAlerts()
                        "profile" -> onProfile()
                    }
                },
                icon = {
                    Icon(
                        icon,
                        contentDescription = label,
                        modifier = Modifier.size((24 * scale).dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    )
                },
                label = {
                    Text(
                        label,
                        fontSize = (9.5 * scale).sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

@Composable
fun DashboardGrid(
    scale: Float,
    onAddBatch: () -> Unit,
    onClimateEntry: () -> Unit,
    onSmartAdvice: () -> Unit,
    onHarvestTimer: () -> Unit,
    onBatchHistory: () -> Unit,
    onAlerts: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isNarrow = configuration.screenWidthDp < 360

    if (isNarrow) {
        Column(verticalArrangement = Arrangement.spacedBy((12 * scale).dp)) {
            GridItemRow(stringResource(R.string.add_batch), Icons.Default.AddCircle, Color(0xFF2D6A4F), scale, onAddBatch)
            GridItemRow(stringResource(R.string.climate_entry), Icons.Default.Thermostat, Color(0xFFE67E22), scale, onClimateEntry)
            GridItemRow(stringResource(R.string.smart_advice), Icons.Default.Lightbulb, Color(0xFFF1C40F), scale, onSmartAdvice)
            GridItemRow(stringResource(R.string.harvest_timer), Icons.Default.Timer, Color(0xFF3498DB), scale, onHarvestTimer)
            GridItemRow(stringResource(R.string.batch_history), Icons.AutoMirrored.Filled.Assignment, Color(0xFF9B59B6), scale, onBatchHistory)
            GridItemRow(stringResource(R.string.alerts), Icons.Default.Notifications, Color(0xFFE74C3C), scale, onAlerts)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy((16 * scale).dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy((16 * scale).dp)) {
                GridItem(stringResource(R.string.add_batch), Icons.Default.AddCircle, Color(0xFF2D6A4F), Modifier.weight(1f), scale, onAddBatch)
                GridItem(stringResource(R.string.climate_entry), Icons.Default.Thermostat, Color(0xFFE67E22), Modifier.weight(1f), scale, onClimateEntry)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy((16 * scale).dp)) {
                GridItem(stringResource(R.string.smart_advice), Icons.Default.Lightbulb, Color(0xFFF1C40F), Modifier.weight(1f), scale, onSmartAdvice)
                GridItem(stringResource(R.string.harvest_timer), Icons.Default.Timer, Color(0xFF3498DB), Modifier.weight(1f), scale, onHarvestTimer)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy((16 * scale).dp)) {
                GridItem(stringResource(R.string.batch_history), Icons.AutoMirrored.Filled.Assignment, Color(0xFF9B59B6), Modifier.weight(1f), scale, onBatchHistory)
                GridItem(stringResource(R.string.alerts), Icons.Default.Notifications, Color(0xFFE74C3C), Modifier.weight(1f), scale, onAlerts)
            }
        }
    }
}

@Composable
fun GridItem(label: String, icon: ImageVector, color: Color, modifier: Modifier, scale: Float, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .heightIn(min = (115 * scale).dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size((44 * scale).dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size((24 * scale).dp), tint = color)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                fontSize = (12.5 * scale).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                lineHeight = (15 * scale).sp,
                maxLines = 2,
                overflow = TextOverflow.Visible
            )
        }
    }
}

@Composable
fun GridItemRow(label: String, icon: ImageVector, color: Color, scale: Float, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height((70 * scale).dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size((40 * scale).dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size((20 * scale).dp), tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = (14 * scale).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun SystemStatusCard(scale: Float, isHealthy: Boolean = true, onClick: () -> Unit) {
    val bgColor = if (isHealthy) MaterialTheme.colorScheme.primary else Color(0xFFD32F2F)
    val title = if (isHealthy) stringResource(R.string.system_healthy) else stringResource(R.string.action_required_status)
    val desc = if (isHealthy) stringResource(R.string.all_parameters_normal) else stringResource(R.string.climate_outside_range)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding((20 * scale).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size((40 * scale).dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isHealthy) Icons.Default.VerifiedUser else Icons.Default.Warning, 
                    contentDescription = null,
                    tint = Color.White, 
                    modifier = Modifier.size((24 * scale).dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = (15 * scale).sp, fontWeight = FontWeight.Bold)
                Text(desc, color = Color.White.copy(alpha = 0.7f), fontSize = (11 * scale).sp, lineHeight = 15.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.White)
        }
    }
}
