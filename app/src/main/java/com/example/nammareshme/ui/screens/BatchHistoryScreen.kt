package com.example.nammareshme.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammareshme.R
import com.example.nammareshme.ui.components.GlobalHeader
import com.example.nammareshme.ui.viewmodel.AppViewModel
import com.example.nammareshme.ui.viewmodel.BatchViewModel
import com.example.nammareshme.utils.LocalizationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchHistoryScreen(
    appViewModel: AppViewModel,
    batchViewModel: BatchViewModel,
    onBack: () -> Unit = {},
    onNavigateToDetails: (String) -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToClimate: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenW = configuration.screenWidthDp.dp
    val scale = (screenW.value / 411f).coerceIn(0.85f, 1.0f)

    val currentLanguage by appViewModel.currentLanguage.collectAsState()
    val unreadCount by appViewModel.unreadNotifications.collectAsState()
    val allBatches by batchViewModel.batches.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.all_tab),
        stringResource(R.string.completed_tab),
        stringResource(R.string.active_tab),
        stringResource(R.string.cancelled_tab)
    )

    val filteredBatches = when (selectedTab) {
        1 -> allBatches.filter { it.status == BatchStatus.COMPLETED }
        2 -> allBatches.filter { it.status == BatchStatus.ACTIVE }
        3 -> allBatches.filter { it.status == BatchStatus.CANCELLED }
        else -> allBatches
    }.filter { it.id.contains(searchQuery, ignoreCase = true) || it.breed.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            GlobalHeader(
                scale = scale,
                showBackButton = true,
                onBackClick = onBack,
                currentLanguage = currentLanguage,
                onLanguageChange = { appViewModel.setLanguage(it) },
                unreadNotificationCount = unreadCount,
                onNotificationClick = onNavigateToAlerts,
                onSettingsClick = onProfile,
                onHelpClick = { onProfile() }
            )
        },
        bottomBar = {
            DashboardBottomNavigation(
                scale = scale,
                currentRoute = "batches",
                onHome = onNavigateToHome,
                onBatches = { },
                onClimateEntry = onNavigateToClimate,
                onAlerts = onNavigateToAlerts,
                onProfile = onProfile
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(horizontal = (16 * scale).dp)) {
                Spacer(modifier = Modifier.height((12 * scale).dp))
                
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size((42 * scale).dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size((22 * scale).dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.batch_history),
                            fontSize = (20 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = (26 * scale).sp
                        )
                        Text(
                            text = stringResource(R.string.batch_history_desc),
                            fontSize = (11 * scale).sp,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = (15 * scale).sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height((18 * scale).dp))

                // Search & Filter
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp),
                        placeholder = { Text(stringResource(R.string.search_batches), fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { },
                        modifier = Modifier.height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.FilterAlt, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.filter), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height((16 * scale).dp))

                // Stats Summary Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SummaryItem(Icons.AutoMirrored.Filled.Assignment, allBatches.size.toString(), stringResource(R.string.total_label), MaterialTheme.colorScheme.primary, scale)
                        SummaryItem(Icons.Default.CheckCircle, allBatches.count { it.status == BatchStatus.COMPLETED }.toString(), stringResource(R.string.done_label), Color(0xFF43A047), scale)
                        SummaryItem(Icons.Default.Schedule, allBatches.count { it.status == BatchStatus.ACTIVE }.toString(), stringResource(R.string.live_label), Color(0xFF1976D2), scale)
                        SummaryItem(Icons.Default.Cancel, allBatches.count { it.status == BatchStatus.CANCELLED }.toString(), stringResource(R.string.stopped_label), Color(0xFFD32F2F), scale)
                    }
                }

                Spacer(modifier = Modifier.height((16 * scale).dp))

                // Tabs
                Surface(
                    modifier = Modifier.fillMaxWidth().heightIn(min = (40 * scale).dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = (40 * scale).dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable { selectedTab = index },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = (11 * scale).sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth(0.7f)
                                            .height(2.dp)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height((12 * scale).dp))

                // Batch List
                if (filteredBatches.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.no_batches_found), color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                    }
                } else {
                    filteredBatches.forEach { batch ->
                        RefinedBatchCard(batch, scale, onNavigateToDetails)
                        Spacer(modifier = Modifier.height((10 * scale).dp))
                    }
                }

                Spacer(modifier = Modifier.height((10 * scale).dp))

                // Footer Info
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.view_detailed_records_info),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height((24 * scale).dp))
            }
        }
    }
}

@Composable
fun RefinedBatchCard(batch: BatchHistoryItem, scale: Float, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(batch.id) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding((12 * scale).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size((52 * scale).dp)
                    .background(
                        when (batch.status) {
                            BatchStatus.COMPLETED -> Color(0xFFE8F5E9).copy(alpha = if (isSystemInDarkTheme()) 0.2f else 1f)
                            BatchStatus.ACTIVE -> Color(0xFFE3F2FD).copy(alpha = if (isSystemInDarkTheme()) 0.2f else 1f)
                            BatchStatus.CANCELLED -> Color(0xFFFFEBEE).copy(alpha = if (isSystemInDarkTheme()) 0.2f else 1f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_silkworm),
                    contentDescription = null,
                    modifier = Modifier.size((34 * scale).dp)
                )
            }
            
            Spacer(modifier = Modifier.width((12 * scale).dp))
            
            Column(modifier = Modifier.weight(1.3f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = batch.id,
                        fontSize = (13 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusBadgeRefined(batch.status, scale)
                }
                InfoLineDetail(stringResource(R.string.breed_label), LocalizationUtils.getLocalizedBreed(batch.breed), scale)
                InfoLineDetail(stringResource(R.string.start_date_c), batch.startDate, scale)
                
                when (batch.status) {
                    BatchStatus.COMPLETED -> InfoLineDetail(stringResource(R.string.end_date_c), batch.endDate ?: "", scale)
                    BatchStatus.ACTIVE -> InfoLineDetail(stringResource(R.string.stage_label), LocalizationUtils.getLocalizedStage(batch.currentStage), scale)
                    BatchStatus.CANCELLED -> InfoLineDetail(stringResource(R.string.cancelled_on_c), batch.cancelledOn ?: "", scale)
                }
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                when (batch.status) {
                    BatchStatus.COMPLETED -> {
                        CardStatRefined(stringResource(R.string.total_days_label), batch.totalDays?.toString() ?: "N/A", scale)
                    }
                    BatchStatus.ACTIVE -> {
                        Text("${(batch.progress * 100).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                    BatchStatus.CANCELLED -> {
                        Text(stringResource(R.string.stopped_label), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun InfoLineDetail(label: String, value: String, scale: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label ", fontSize = (10 * scale).sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = (10 * scale).sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
fun CardStatRefined(label: String, value: String, scale: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center, lineHeight = 11.sp)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
    }
}

@Composable
fun StatusBadgeRefined(status: BatchStatus, scale: Float) {
    val bgColor = when (status) {
        BatchStatus.COMPLETED -> Color(0xFFE8F5E9).copy(alpha = if (isSystemInDarkTheme()) 0.2f else 1f)
        BatchStatus.ACTIVE -> Color(0xFFE3F2FD).copy(alpha = if (isSystemInDarkTheme()) 0.2f else 1f)
        BatchStatus.CANCELLED -> Color(0xFFFFEBEE).copy(alpha = if (isSystemInDarkTheme()) 0.2f else 1f)
    }
    val textColor = when (status) {
        BatchStatus.COMPLETED -> Color(0xFF2E7D32)
        BatchStatus.ACTIVE -> Color(0xFF1565C0)
        BatchStatus.CANCELLED -> Color(0xFFC62828)
    }
    val label = when (status) {
        BatchStatus.COMPLETED -> stringResource(R.string.completed_tab)
        BatchStatus.ACTIVE -> stringResource(R.string.active_tab)
        BatchStatus.CANCELLED -> stringResource(R.string.cancelled_tab)
    }

    Surface(shape = RoundedCornerShape(4.dp), color = bgColor) {
        Text(
            text = label,
            fontSize = (8 * scale).sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
            maxLines = 1
        )
    }
}

@Composable
fun SummaryItem(icon: ImageVector, value: String, label: String, color: Color, scale: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size((28 * scale).dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, modifier = Modifier.size(15.dp), tint = color)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
