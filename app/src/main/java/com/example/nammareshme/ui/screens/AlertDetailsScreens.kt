package com.example.nammareshme.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammareshme.R
import com.example.nammareshme.ui.components.GlobalHeader
import com.example.nammareshme.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDetailTemplate(
    appViewModel: AppViewModel,
    title: String,
    subtitle: String,
    type: AlertType,
    icon: ImageVector,
    timestamp: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.(Float) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenW = configuration.screenWidthDp.dp
    val scale = (screenW.value / 400f).coerceIn(0.85f, 1.2f)

    val currentLanguage by appViewModel.currentLanguage.collectAsState()
    val unreadNotifications by appViewModel.unreadNotifications.collectAsState()

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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding((20 * scale).dp)
        ) {
            // Header Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(type.bgColor.copy(alpha = if (MaterialTheme.colorScheme.surface == Color.White) 1f else 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, null, modifier = Modifier.size(24.dp), tint = type.color)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, lineHeight = 22.sp)
                            Text(timestamp, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = type.bgColor.copy(alpha = if (MaterialTheme.colorScheme.surface == Color.White) 1f else 0.2f),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            val typeLabel = when(type) {
                                AlertType.CRITICAL -> stringResource(R.string.critical_label)
                                AlertType.WARNING -> stringResource(R.string.warning_label)
                                AlertType.INFO -> stringResource(R.string.info_label)
                                AlertType.ALL_CLEAR -> stringResource(R.string.all_clear_label)
                            }
                            Text(
                                typeLabel,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = type.color,
                                maxLines = 1
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            content(scale)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DetailSection(title: String, icon: ImageVector, scale: Float, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size((18 * scale).dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontSize = (15 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, scale: Float, color: Color = MaterialTheme.colorScheme.primary) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = (13 * scale).sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f), lineHeight = 16.sp)
        Text(value, fontSize = (13 * scale).sp, fontWeight = FontWeight.Bold, color = color, textAlign = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun ActionButton(text: String, icon: ImageVector, scale: Float, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Icon(icon, null, modifier = Modifier.size((20 * scale).dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, fontSize = (16 * scale).sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun TemperatureAlertDetailsScreen(appViewModel: AppViewModel, value: String, batchId: String, onBack: () -> Unit, onAction: () -> Unit) {
    AlertDetailTemplate(
        appViewModel = appViewModel,
        title = stringResource(R.string.alert_high_temp_title),
        subtitle = stringResource(R.string.alert_high_temp_desc_long),
        type = AlertType.CRITICAL,
        icon = Icons.Default.Thermostat,
        timestamp = stringResource(R.string.today),
        onBack = onBack
    ) { scale ->
        DetailSection(stringResource(R.string.climate_data), Icons.Default.Assessment, scale) {
            InfoRow(stringResource(R.string.temp_label), "$value°C", scale, Color(0xFFD32F2F))
            InfoRow(stringResource(R.string.ideal_range), "24°C - 28°C", scale)
            InfoRow(stringResource(R.string.active_batch), batchId, scale)
            InfoRow(stringResource(R.string.status_label), stringResource(R.string.critical_overheat), scale)
        }
        Spacer(modifier = Modifier.height(20.dp))
        DetailSection(stringResource(R.string.recommendations), Icons.Default.Lightbulb, scale) {
            Text("• " + stringResource(R.string.adv_vent_tip1), fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 18.sp)
            Text("• " + stringResource(R.string.adv_vent_tip2), fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 18.sp)
            Text("• " + stringResource(R.string.sprinkle_water_tip), fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 18.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        ActionButton(stringResource(R.string.log_corrective), Icons.Default.Edit, scale, MaterialTheme.colorScheme.primary, onClick = onAction)
    }
}

@Composable
fun HumidityAlertDetailsScreen(appViewModel: AppViewModel, value: String, batchId: String, onBack: () -> Unit, onAction: () -> Unit) {
    AlertDetailTemplate(
        appViewModel = appViewModel,
        title = stringResource(R.string.alert_low_hum_title),
        subtitle = stringResource(R.string.low_hum_subtitle),
        type = AlertType.WARNING,
        icon = Icons.Default.WaterDrop,
        timestamp = stringResource(R.string.today),
        onBack = onBack
    ) { scale ->
        DetailSection(stringResource(R.string.climate_data), Icons.Default.Assessment, scale) {
            InfoRow(stringResource(R.string.hum_label), "$value%", scale, Color(0xFFF57C00))
            InfoRow(stringResource(R.string.ideal_range), "65% - 85%", scale)
            InfoRow(stringResource(R.string.active_batch), batchId, scale)
        }
        Spacer(modifier = Modifier.height(20.dp))
        DetailSection(stringResource(R.string.recommendations), Icons.Default.Lightbulb, scale) {
            Text("• " + stringResource(R.string.hang_bags_tip), fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 18.sp)
            Text("• " + stringResource(R.string.adv_ideal_tip4), fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 18.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        ActionButton(stringResource(R.string.update_hum), Icons.Default.Refresh, scale, MaterialTheme.colorScheme.primary, onClick = onAction)
    }
}

@Composable
fun VentilationDetailsScreen(appViewModel: AppViewModel, onBack: () -> Unit, onAction: () -> Unit) {
    AlertDetailTemplate(
        appViewModel = appViewModel,
        title = stringResource(R.string.alert_vent_title),
        subtitle = stringResource(R.string.vent_subtitle),
        type = AlertType.INFO,
        icon = Icons.Default.Air,
        timestamp = stringResource(R.string.today),
        onBack = onBack
    ) { scale ->
        DetailSection(stringResource(R.string.info_label), Icons.Default.Info, scale) {
            Text(stringResource(R.string.vent_desc_long), fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 18.sp)
        }
        Spacer(modifier = Modifier.height(20.dp))
        DetailSection(stringResource(R.string.action_plan), Icons.Default.TaskAlt, scale) {
            Text("• " + stringResource(R.string.adv_vent_tip3), fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 18.sp)
            Text("• " + stringResource(R.string.run_fans_tip), fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 18.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        ActionButton(stringResource(R.string.mark_as_done), Icons.Default.Check, scale, MaterialTheme.colorScheme.secondary, onClick = onAction)
    }
}

@Composable
fun FeedingReminderScreen(appViewModel: AppViewModel, onBack: () -> Unit, onAction: () -> Unit) {
    AlertDetailTemplate(
        appViewModel = appViewModel,
        title = stringResource(R.string.alert_feeding_title),
        subtitle = stringResource(R.string.feeding_subtitle),
        type = AlertType.INFO,
        icon = Icons.Default.Restaurant,
        timestamp = stringResource(R.string.today),
        onBack = onBack
    ) { scale ->
        DetailSection(stringResource(R.string.feeding_guide), Icons.Default.Spa, scale) {
            InfoRow(stringResource(R.string.instar_stage_label), stringResource(R.string.instar_4), scale)
            InfoRow(stringResource(R.string.leaf_type_label), stringResource(R.string.matured_leaves), scale)
            InfoRow(stringResource(R.string.quantity), stringResource(R.string.kg_approx_format, 5.5f), scale)
        }
        Spacer(modifier = Modifier.height(20.dp))
        ActionButton(stringResource(R.string.log_feeding), Icons.Default.Add, scale, MaterialTheme.colorScheme.primary, onClick = onAction)
    }
}

@Composable
fun BatchProgressScreen(appViewModel: AppViewModel, stage: String, batchId: String, onBack: () -> Unit, onAction: () -> Unit) {
    AlertDetailTemplate(
        appViewModel = appViewModel,
        title = stringResource(R.string.alert_batch_prog_title),
        subtitle = stringResource(R.string.batch_progress_subtitle),
        type = AlertType.INFO,
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        timestamp = stringResource(R.string.today),
        onBack = onBack
    ) { scale ->
        DetailSection(stringResource(R.string.current_progress), Icons.Default.AutoGraph, scale) {
            InfoRow(stringResource(R.string.batches), batchId, scale)
            InfoRow(stringResource(R.string.stage_label), stage, scale)
            InfoRow(stringResource(R.string.next_milestone), stringResource(R.string.upcoming_label), scale)
        }
        Spacer(modifier = Modifier.height(24.dp))
        ActionButton(stringResource(R.string.view_full_timeline), Icons.Default.Timeline, scale, MaterialTheme.colorScheme.primary, onClick = onAction)
    }
}

@Composable
fun ClimateStatusScreen(appViewModel: AppViewModel, onBack: () -> Unit, onAction: () -> Unit) {
    AlertDetailTemplate(
        appViewModel = appViewModel,
        title = stringResource(R.string.alert_cond_normal_title),
        subtitle = stringResource(R.string.conditions_normal_subtitle),
        type = AlertType.ALL_CLEAR,
        icon = Icons.Default.CheckCircle,
        timestamp = stringResource(R.string.today),
        onBack = onBack
    ) { scale ->
        DetailSection(stringResource(R.string.env_summary), Icons.Default.WbSunny, scale) {
            InfoRow(stringResource(R.string.temperature), "27.1°C", scale, Color(0xFF388E3C))
            InfoRow(stringResource(R.string.humidity_label), "72%", scale, Color(0xFF388E3C))
            InfoRow(stringResource(R.string.adv_vent_title), stringResource(R.string.good_status), scale)
        }
        Spacer(modifier = Modifier.height(24.dp))
        ActionButton(stringResource(R.string.check_trends), Icons.Default.BarChart, scale, MaterialTheme.colorScheme.secondary, onClick = onAction)
    }
}
