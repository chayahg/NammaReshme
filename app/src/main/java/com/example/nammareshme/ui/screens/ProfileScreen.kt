package com.example.nammareshme.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nammareshme.R
import com.example.nammareshme.ui.components.GlobalHeader
import com.example.nammareshme.ui.viewmodel.AppViewModel
import com.example.nammareshme.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    appViewModel: AppViewModel,
    onBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToBatches: () -> Unit = {},
    onNavigateToClimate: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onLogoutSuccess: () -> Unit = {},
    onSettingClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val farmStats by viewModel.farmStats.collectAsState()
    val isUploading by viewModel.isUploadingImage.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val configuration = LocalConfiguration.current
    val screenW = configuration.screenWidthDp.dp
    val scale = (screenW.value / 411f).coerceIn(0.85f, 1.0f)
    val isNarrow = configuration.screenWidthDp < 360

    val currentLanguage by appViewModel.currentLanguage.collectAsState()
    val unreadNotifications by appViewModel.unreadNotifications.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.saveLocalProfileImage(context, it) }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.logout_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogoutSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(stringResource(R.string.logout), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                onNotificationClick = onNavigateToAlerts
            )
        },
        bottomBar = {
            DashboardBottomNavigation(
                scale = scale,
                currentRoute = "profile",
                onHome = onNavigateToHome,
                onBatches = onNavigateToBatches,
                onClimateEntry = onNavigateToClimate,
                onAlerts = onNavigateToAlerts,
                onProfile = { }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = (18 * scale).dp)
        ) {
            Spacer(modifier = Modifier.height((12 * scale).dp))
            
            Text(
                text = stringResource(R.string.my_profile),
                fontSize = (24 * scale).sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.manage_account_pref),
                fontSize = (13 * scale).sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height((20 * scale).dp))

            // Profile Header Card
            userProfile?.let { profile ->
                ProfileHeaderCard(
                    profile = profile, 
                    scale = scale, 
                    onClick = onSettingClick, 
                    onImageClick = { 
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                    }, 
                    isUploading = isUploading
                )
            } ?: Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height((24 * scale).dp))

            // Farm Overview Section
            FarmOverviewSection(farmStats, scale, onSettingClick)

            Spacer(modifier = Modifier.height((24 * scale).dp))

            if (isNarrow) {
                Column {
                    SettingsSectionTitle(Icons.Default.Settings, stringResource(R.string.account_settings), scale)
                    AccountSettingsList(scale, onSettingClick, currentLanguage)
                    Spacer(modifier = Modifier.height((24 * scale).dp))
                    SettingsSectionTitle(Icons.Default.Smartphone, stringResource(R.string.app_settings), scale)
                    AppSettingsList(scale, onSettingClick)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        SettingsSectionTitle(Icons.Default.Settings, stringResource(R.string.account_settings), scale)
                        AccountSettingsList(scale, onSettingClick, currentLanguage)
                    }
                    Spacer(modifier = Modifier.width((16 * scale).dp))
                    Column(modifier = Modifier.weight(1f)) {
                        SettingsSectionTitle(Icons.Default.Smartphone, stringResource(R.string.app_settings), scale)
                        AppSettingsList(scale, onSettingClick)
                    }
                }
            }

            Spacer(modifier = Modifier.height((24 * scale).dp))

            // Help Support Card
            HelpSupportCard(scale, onSettingClick)

            Spacer(modifier = Modifier.height((16 * scale).dp))

            // Logout Button
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height((50 * scale).dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE57373)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size((18 * scale).dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.logout), fontSize = (14 * scale).sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height((32 * scale).dp))
        }
    }
}

@Composable
fun ProfileHeaderCard(profile: UserProfile, scale: Float, onClick: (String) -> Unit, onImageClick: () -> Unit, isUploading: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick("personal_info") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding((16 * scale).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size((80 * scale).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .clickable { onImageClick() },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profile.profileImageUrl ?: R.drawable.ic_launcher_background)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_launcher_background),
                    placeholder = painterResource(id = R.drawable.ic_launcher_background)
                )
                if (isUploading) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    }
                }
                Surface(
                    modifier = Modifier
                        .size((24 * scale).dp)
                        .align(Alignment.BottomEnd),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size((12 * scale).dp), tint = Color.White)
                    }
                }
            }
            
            Spacer(modifier = Modifier.width((16 * scale).dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    fontSize = (18 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
                
                if (profile.isVerified) {
                    Surface(
                        color = Color(0xFFE8F5E9).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(12.dp), tint = Color(0xFF43A047))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.verified_farmer), fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                if (profile.phone.isNotBlank()) {
                    ProfileInfoRow(Icons.Default.Phone, profile.phone, scale)
                }
                if (profile.location.isNotBlank()) {
                    ProfileInfoRow(Icons.Default.LocationOn, profile.location, scale)
                }
            }
            
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, text: String, scale: Float) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp)) {
        Icon(icon, null, modifier = Modifier.size((12 * scale).dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = (11 * scale).sp, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
    }
}

@Composable
fun FarmOverviewSection(stats: FarmStats, scale: Float, onClick: (String) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(id = R.drawable.ic_leaf), null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.farm_overview), fontSize = (14 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            TextButton(onClick = { onClick("farm_details") }) {
                Text(stringResource(R.string.view_farm_details), fontSize = (11 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FarmStatCard(stats.activeBatches.toString(), stringResource(R.string.active_batches), Color(0xFF2D6A4F), painterResource(R.drawable.ic_leaf), Modifier.weight(1f), scale)
                FarmStatCard(stats.totalBatches.toString(), stringResource(R.string.total_batches), Color(0xFF1976D2), painterResource(R.drawable.ic_silkworm), Modifier.weight(1f), scale)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FarmStatCard(stats.totalCocoons, stringResource(R.string.total_cocoons), Color(0xFFEF6C00), Icons.Default.BrightnessLow, Modifier.weight(1f), scale)
                FarmStatCard(stats.avgSurvivalRate, stringResource(R.string.avg_survival_rate), Color(0xFF7B1FA2), Icons.AutoMirrored.Filled.TrendingUp, Modifier.weight(1f), scale)
            }
        }
    }
}

@Composable
fun FarmStatCard(value: String, label: String, color: Color, icon: Any, modifier: Modifier, scale: Float) {
    Card(
        modifier = modifier.height((90 * scale).dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(1.dp, Color(0xFFF5F5F5).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size((36 * scale).dp).background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (icon) {
                    is ImageVector -> Icon(icon, null, Modifier.size((20 * scale).dp), tint = color)
                    else -> Icon(icon as androidx.compose.ui.graphics.painter.Painter, null, Modifier.size((20 * scale).dp), tint = color)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, fontSize = (16 * scale).sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text(label, fontSize = (9 * scale).sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 11.sp, maxLines = 1)
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(icon: ImageVector, title: String, scale: Float) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Icon(icon, null, modifier = Modifier.size((14 * scale).dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(6.dp))
        Text(title, fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun AccountSettingsList(scale: Float, onClick: (String) -> Unit, currentLanguage: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(1.dp, Color(0xFFF5F5F5).copy(alpha = 0.3f))
    ) {
        Column {
            SettingRow(Icons.Default.PersonOutline, stringResource(R.string.personal_info), stringResource(R.string.personal_info_desc), scale) { onClick("personal_info") }
            HorizontalDivider(color = Color(0xFFF5F5F5).copy(alpha = 0.3f), thickness = 0.5.dp)
            SettingRow(Icons.Default.Key, stringResource(R.string.change_password), stringResource(R.string.change_password_desc), scale) { onClick("change_password") }
            HorizontalDivider(color = Color(0xFFF5F5F5).copy(alpha = 0.3f), thickness = 0.5.dp)
            SettingRow(Icons.Default.Language, stringResource(R.string.language), stringResource(R.string.language_desc), scale, trailingText = if (currentLanguage == "English") "English" else "ಕನ್ನಡ") { onClick("language_settings") }
            HorizontalDivider(color = Color(0xFFF5F5F5).copy(alpha = 0.3f), thickness = 0.5.dp)
            SettingRow(Icons.Default.NotificationsNone, stringResource(R.string.notification_settings), stringResource(R.string.notification_settings_desc), scale) { onClick("notification_settings") }
            HorizontalDivider(color = Color(0xFFF5F5F5).copy(alpha = 0.3f), thickness = 0.5.dp)
            SettingRow(Icons.Default.SettingsInputComponent, stringResource(R.string.preferences), stringResource(R.string.preferences_desc), scale) { onClick("preferences") }
        }
    }
}

@Composable
fun AppSettingsList(scale: Float, onClick: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(1.dp, Color(0xFFF5F5F5).copy(alpha = 0.3f))
    ) {
        Column {
            SettingRow(Icons.Default.Sync, stringResource(R.string.data_sync), stringResource(R.string.last_synced), scale, trailingIcon = Icons.Default.CheckCircle, trailingIconColor = Color(0xFF43A047)) { onClick("data_sync") }
            HorizontalDivider(color = Color(0xFFF5F5F5).copy(alpha = 0.3f), thickness = 0.5.dp)
            SettingRow(Icons.Default.CloudUpload, stringResource(R.string.backup_restore), stringResource(R.string.backup_desc), scale) { onClick("backup") }
            HorizontalDivider(color = Color(0xFFF5F5F5).copy(alpha = 0.3f), thickness = 0.5.dp)
            SettingRow(Icons.Default.FileDownload, stringResource(R.string.export_data), stringResource(R.string.export_desc), scale) { onClick("export_data") }
            HorizontalDivider(color = Color(0xFFF5F5F5).copy(alpha = 0.3f), thickness = 0.5.dp)
            SettingRow(Icons.AutoMirrored.Filled.HelpOutline, stringResource(R.string.help_support), stringResource(R.string.help_desc), scale) { onClick("help") }
            HorizontalDivider(color = Color(0xFFF5F5F5).copy(alpha = 0.3f), thickness = 0.5.dp)
            SettingRow(Icons.Default.Info, stringResource(R.string.about_app), stringResource(R.string.version_text), scale) { onClick("about") }
        }
    }
}

@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    scale: Float,
    trailingText: String? = null,
    trailingIcon: ImageVector? = null,
    trailingIconColor: Color = Color(0xFF1B4332),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding((12 * scale).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size((18 * scale).dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(subtitle, fontSize = (9 * scale).sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 11.sp, maxLines = 1)
        }
        
        if (trailingText != null) {
            Text(trailingText, fontSize = (10 * scale).sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
        }
        
        if (trailingIcon != null) {
            Icon(trailingIcon, null, modifier = Modifier.size((14 * scale).dp), tint = trailingIconColor)
        } else {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size((16 * scale).dp), tint = Color(0xFFBDBDBD))
        }
    }
}

@Composable
fun HelpSupportCard(scale: Float, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding((12 * scale).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size((36 * scale).dp).background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.HeadsetMic, null, modifier = Modifier.size((20 * scale).dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.need_help), fontSize = (13 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.help_q_desc), fontSize = (10 * scale).sp, color = MaterialTheme.colorScheme.secondary)
            }
            Button(
                onClick = { onClick("help") },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.contact_support), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}
