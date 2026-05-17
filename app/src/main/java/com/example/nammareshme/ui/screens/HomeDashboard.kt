package com.example.nammareshme.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.nammareshme.R
import com.example.nammareshme.models.WeatherResponse
import com.example.nammareshme.ui.components.GlobalHeader
import com.example.nammareshme.ui.viewmodel.AppViewModel
import com.example.nammareshme.ui.viewmodel.BatchViewModel
import com.example.nammareshme.ui.viewmodel.ProfileViewModel
import com.example.nammareshme.ui.viewmodel.WeatherUiState
import com.example.nammareshme.ui.viewmodel.WeatherViewModel
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun HomeDashboard(
    appViewModel: AppViewModel,
    profileViewModel: ProfileViewModel,
    weatherViewModel: WeatherViewModel,
    batchViewModel: BatchViewModel,
    onAddBatch: () -> Unit = {},
    onClimateEntry: () -> Unit = {},
    onSmartAdvice: () -> Unit = {},
    onHarvestTimer: () -> Unit = {},
    onBatchHistory: () -> Unit = {},
    onAlerts: () -> Unit = {},
    onReports: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenW = configuration.screenWidthDp.dp
    val scale = (screenW.value / 400f).coerceIn(0.85f, 1.2f)

    val currentLanguage by appViewModel.currentLanguage.collectAsState()
    val unreadNotifications by appViewModel.unreadNotifications.collectAsState()
    val user by profileViewModel.realUser.collectAsState()
    val isLocationLoading by profileViewModel.isLocationLoading.collectAsState()
    val weatherState by weatherViewModel.weatherState.collectAsState()
    val climateLogs by batchViewModel.climateLogs.collectAsState()

    var showLocationPrompt by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, context.getString(R.string.notif_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
        delay(1000)
        
        val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        if (!hasFineLocation && !hasCoarseLocation) {
            showLocationPrompt = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(user?.latitude, user?.longitude) {
        user?.let {
            if (it.latitude != null && it.longitude != null) {
                weatherViewModel.fetchWeather(it.latitude, it.longitude)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            showLocationPrompt = false
            profileViewModel.fetchCurrentLocation(context, { addr, lat, lon ->
                profileViewModel.updatePersonalDetails(
                    name = user?.name ?: "",
                    phone = user?.phone ?: "",
                    email = user?.email ?: "",
                    location = addr,
                    farmName = user?.farmName ?: "",
                    lat = lat,
                    lon = lon
                )
                Toast.makeText(context, context.getString(R.string.location_success), Toast.LENGTH_SHORT).show()
            }, { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            })
        } else {
            showLocationPrompt = false
            Toast.makeText(context, context.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            GlobalHeader(
                scale = scale,
                currentLanguage = currentLanguage,
                onLanguageChange = { appViewModel.setLanguage(it) },
                unreadNotificationCount = unreadNotifications,
                onNotificationClick = onAlerts,
                onSettingsClick = onProfile,
                onHelpClick = { onProfile() }
            )
        },
        bottomBar = { 
            DashboardBottomNavigation(
                scale = scale, 
                currentRoute = "home",
                onHome = { },
                onBatches = onBatchHistory,
                onClimateEntry = onClimateEntry,
                onAlerts = onAlerts,
                onProfile = onProfile
            ) 
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = (20 * scale).dp)
            ) {
                Spacer(modifier = Modifier.height((16 * scale).dp))

                val userName = user?.name?.split(" ")?.firstOrNull() ?: stringResource(R.string.farmer_default_name)
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val greetingRes = when {
                    hour in 0..11 -> R.string.good_morning
                    hour in 12..16 -> R.string.good_afternoon
                    else -> R.string.good_evening
                }

                Text(
                    text = stringResource(greetingRes, userName),
                    fontSize = (22 * scale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = (28 * scale).sp
                )
                Text(
                    text = stringResource(R.string.productive_day),
                    fontSize = (13.5 * scale).sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 2.dp),
                    lineHeight = (18 * scale).sp
                )

                Spacer(modifier = Modifier.height((24 * scale).dp))

                WeatherClimateSection(
                    scale = scale, 
                    weatherState = weatherState,
                    onClick = onClimateEntry, 
                    location = if (isLocationLoading) stringResource(R.string.fetching_location) else user?.location ?: stringResource(R.string.unknown_location)
                )

                Spacer(modifier = Modifier.height((24 * scale).dp))

                DashboardGrid(
                    scale = scale,
                    onAddBatch = onAddBatch,
                    onClimateEntry = onClimateEntry,
                    onSmartAdvice = onSmartAdvice,
                    onHarvestTimer = onHarvestTimer,
                    onBatchHistory = onBatchHistory,
                    onAlerts = onAlerts
                )

                Spacer(modifier = Modifier.height((20 * scale).dp))

                val latestLog = climateLogs.firstOrNull()
                // Action Required if climate is bad OR if there are unread notifications (harvest, etc.)
                val isHealthy = unreadNotifications == 0 && (latestLog == null || (latestLog.temperature in 24.0f..28.5f && latestLog.humidity in 65f..85f))
                
                SystemStatusCard(
                    scale = scale, 
                    isHealthy = isHealthy,
                    onClick = onAlerts
                )

                Spacer(modifier = Modifier.height((24 * scale).dp))
            }

            if (showLocationPrompt) {
                Dialog(
                    onDismissRequest = { showLocationPrompt = false },
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LocationRationalePopup(
                            scale = scale,
                            onAllow = { 
                                showLocationPrompt = false
                                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            },
                            onDismiss = { showLocationPrompt = false }
                        )
                    }
                }
            }

            if (isLocationLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 4.dp)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                stringResource(R.string.fetching_location), 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherClimateSection(scale: Float, weatherState: WeatherUiState, onClick: () -> Unit, location: String) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding((18 * scale).dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.current_climate), fontSize = (13 * scale).sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text(location, fontSize = (10 * scale).sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(14.dp))
            
            when (weatherState) {
                is WeatherUiState.Loading -> {
                    Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    }
                }
                is WeatherUiState.Success -> {
                    val weather = weatherState.weather
                    WeatherContent(scale, weather)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    WeatherAdviceCard(scale, weather)
                }
                is WeatherUiState.Error -> {
                    Text(weatherState.message, color = Color.Red, fontSize = 12.sp)
                }
                else -> {
                    Text(stringResource(R.string.no_weather_data_warning), fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun WeatherContent(scale: Float, weather: WeatherResponse) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.weight(1.3f), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_thermometer), null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("${weather.main.temp.toInt()}°C", fontSize = (24 * scale).sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Cloud, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(weather.weather.firstOrNull()?.main ?: "Clear", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
            }
        }
        VerticalDivider(modifier = Modifier.height(30.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WaterDrop, null, Modifier.size(14.dp), tint = Color(0xFF1565C0))
                Spacer(modifier = Modifier.width(6.dp))
                Text("${weather.main.humidity}%", fontSize = (15 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Text(stringResource(R.string.humidity), fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Air, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("${weather.wind.speed.toInt()}m/s", fontSize = (15 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Text(stringResource(R.string.wind), fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun WeatherAdviceCard(scale: Float, weather: WeatherResponse) {
    val temp = weather.main.temp
    val advice = when {
        temp > 30 -> stringResource(R.string.heat_detected_advice)
        temp < 20 -> stringResource(R.string.cool_weather_advice)
        else -> stringResource(R.string.optimal_conditions_advice)
    }
    
    Surface(
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(advice, fontSize = (11 * scale).sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium, lineHeight = 15.sp)
        }
    }
}

@Composable
fun LocationRationalePopup(scale: Float, onAllow: () -> Unit, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape((28 * scale).dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding((24 * scale).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size((64 * scale).dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size((34 * scale).dp))
            }
            Spacer(modifier = Modifier.height((20 * scale).dp))
            Text(stringResource(R.string.location_rationale_title), fontSize = (20 * scale).sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height((10 * scale).dp))
            Text(stringResource(R.string.location_rationale_desc), fontSize = (14 * scale).sp, color = MaterialTheme.colorScheme.secondary, lineHeight = (20 * scale).sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height((32 * scale).dp))
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onAllow, modifier = Modifier.fillMaxWidth().height((54 * scale).dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(16.dp)) {
                    Text(stringResource(R.string.allow_location), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height((48 * scale).dp)) {
                    Text(stringResource(R.string.maybe_later), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
