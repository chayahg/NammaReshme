package com.example.nammareshme.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.nammareshme.R
import com.example.nammareshme.ui.components.GlobalHeader
import com.example.nammareshme.ui.viewmodel.AppViewModel
import com.example.nammareshme.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreenBase(
    title: String,
    onBack: () -> Unit,
    appViewModel: AppViewModel,
    content: @Composable ColumnScope.(scale: Float) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenW = configuration.screenWidthDp.dp
    val scale = (screenW.value / 411f).coerceIn(0.85f, 1.0f)

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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = (20 * scale).dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = (22 * scale).sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = (28 * scale).sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            content(scale)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PersonalInfoScreen(viewModel: ProfileViewModel, appViewModel: AppViewModel, onBack: () -> Unit) {
    val user by viewModel.realUser.collectAsState()
    val context = LocalContext.current
    val isLocationLoading by viewModel.isLocationLoading.collectAsState()

    ProfileDetailScreenBase(stringResource(R.string.personal_info), onBack, appViewModel) { scale ->
        var name by remember(user) { mutableStateOf(user?.name ?: "") }
        var phone by remember(user) { mutableStateOf(user?.phone ?: "") }
        var email by remember(user) { mutableStateOf(if (user?.isGoogleUser == true) user?.email ?: "" else "") }
        var farmName by remember(user) { mutableStateOf(user?.farmName ?: "") }
        var address by remember(user) { mutableStateOf(user?.location ?: "") }
        var latitude by remember(user) { mutableStateOf(user?.latitude) }
        var longitude by remember(user) { mutableStateOf(user?.longitude) }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                          permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                viewModel.fetchCurrentLocation(context, { addr, lat, lon ->
                    address = addr
                    latitude = lat
                    longitude = lon
                    Toast.makeText(context, context.getString(R.string.location_success), Toast.LENGTH_SHORT).show()
                }, { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                })
            } else {
                Toast.makeText(context, context.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            }
        }

        InfoSectionTitle(stringResource(R.string.primary_details), scale)
        SettingsTextField(stringResource(R.string.enter_name), name, { name = it }, scale)
        SettingsTextField(stringResource(R.string.enter_phone), phone, { phone = it }, scale)
        
        if (user?.isGoogleUser == true) {
            SettingsTextField(stringResource(R.string.email_address), email, { email = it }, scale)
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        InfoSectionTitle(stringResource(R.string.view_farm_details), scale)
        SettingsTextField(stringResource(R.string.farm_name), farmName, { farmName = it }, scale)
        
        Column(modifier = Modifier.padding(bottom = 14.dp)) {
            Text(stringResource(R.string.address_location), fontSize = (11 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = Color(0xFFF0F0F0),
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                trailingIcon = {
                    if (isLocationLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                    } else {
                        IconButton(onClick = {
                            val fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            val coarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                            
                            if (fineLocation == PackageManager.PERMISSION_GRANTED || coarseLocation == PackageManager.PERMISSION_GRANTED) {
                                viewModel.fetchCurrentLocation(context, { addr, lat, lon ->
                                    address = addr
                                    latitude = lat
                                    longitude = lon
                                    Toast.makeText(context, context.getString(R.string.location_success), Toast.LENGTH_SHORT).show()
                                }, { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                })
                            } else {
                                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            }
                        }) {
                            Icon(Icons.Default.MyLocation, contentDescription = stringResource(R.string.use_current_loc_desc), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                textStyle = LocalTextStyle.current.copy(fontSize = (14 * scale).sp, lineHeight = 18.sp)
            )
        }

        if (latitude != null && longitude != null) {
            Text(
                text = stringResource(R.string.lat_lon_format, latitude ?: 0.0, longitude ?: 0.0),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        PremiumButton(stringResource(R.string.save_changes), scale) { 
            viewModel.updatePersonalDetails(name, phone, email, address, farmName, latitude, longitude)
            Toast.makeText(context, context.getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
            onBack()
        }
    }
}

@Composable
fun ChangePasswordScreen(appViewModel: AppViewModel, onBack: () -> Unit) {
    ProfileDetailScreenBase(stringResource(R.string.change_password), onBack, appViewModel) { scale ->
        var currentPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }

        InfoSectionTitle(stringResource(R.string.security_update), scale)
        SettingsTextField(stringResource(R.string.current_password), currentPass, { currentPass = it }, scale, isPassword = true)
        SettingsTextField(stringResource(R.string.new_password), newPass, { newPass = it }, scale, isPassword = true)
        SettingsTextField(stringResource(R.string.confirm_new_password), confirmPass, { confirmPass = it }, scale, isPassword = true)

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.password_hint),
            fontSize = 11.sp,
            color = Color(0xFF757575),
            modifier = Modifier.padding(horizontal = 4.dp),
            lineHeight = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))
        PremiumButton(stringResource(R.string.update_password), scale) { 
            onBack() 
        }
    }
}

@Composable
fun LanguageSettingsScreen(viewModel: ProfileViewModel, appViewModel: AppViewModel, onBack: () -> Unit) {
    val user by viewModel.realUser.collectAsState()
    
    ProfileDetailScreenBase(stringResource(R.string.language), onBack, appViewModel) { scale ->
        val languages = listOf("English", "Kannada")
        val displayNames = mapOf("English" to stringResource(R.string.english), "Kannada" to stringResource(R.string.kannada))

        InfoSectionTitle(stringResource(R.string.language_settings_title), scale)
        
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFF0F0F0))
        ) {
            Column {
                languages.forEachIndexed { index, lang ->
                    LanguageRow(displayNames[lang] ?: lang, user?.language == lang, { 
                        appViewModel.setLanguage(lang)
                        viewModel.updateSettings(language = lang)
                    }, scale)
                    if (index < languages.size - 1) HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun NotificationSettingsScreen(viewModel: ProfileViewModel, appViewModel: AppViewModel, onBack: () -> Unit) {
    val user by viewModel.realUser.collectAsState()

    ProfileDetailScreenBase(stringResource(R.string.notification_settings), onBack, appViewModel) { scale ->
        InfoSectionTitle(stringResource(R.string.alert_preferences), scale)
        
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFF0F0F0))
        ) {
            Column {
                ToggleRow(stringResource(R.string.batch_progress_alerts), stringResource(R.string.batch_progress_desc), user?.batchAlerts ?: true, { viewModel.updateSettings(batchAlerts = it) }, scale)
                HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                ToggleRow(stringResource(R.string.climate_threshold_alerts), stringResource(R.string.climate_threshold_desc), user?.climateAlerts ?: true, { viewModel.updateSettings(climateAlerts = it) }, scale)
                HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                ToggleRow(stringResource(R.string.feeding_reminders), stringResource(R.string.feeding_desc), user?.feedingReminders ?: true, { viewModel.updateSettings(feedingReminders = it) }, scale)
                HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                ToggleRow(stringResource(R.string.system_notifications), stringResource(R.string.system_desc), user?.systemNotifications ?: false, { viewModel.updateSettings(systemNotifications = it) }, scale)
            }
        }
    }
}

@Composable
fun PreferencesScreen(viewModel: ProfileViewModel, appViewModel: AppViewModel, onBack: () -> Unit) {
    val user by viewModel.realUser.collectAsState()
    val isDarkMode by appViewModel.isDarkMode.collectAsState()

    ProfileDetailScreenBase(stringResource(R.string.preferences), onBack, appViewModel) { scale ->
        InfoSectionTitle(stringResource(R.string.measurement_units), scale)
        
        PreferenceDropdown(
            label = stringResource(R.string.temp_unit), 
            selected = if (user?.tempUnit == "Fahrenheit (°F)") stringResource(R.string.fahrenheit) else stringResource(R.string.celsius), 
            options = listOf(stringResource(R.string.celsius), stringResource(R.string.fahrenheit)), 
            onSelected = { viewModel.updateSettings(tempUnit = it) }, 
            scale = scale
        )
        Spacer(modifier = Modifier.height(16.dp))
        PreferenceDropdown(
            label = stringResource(R.string.weight_unit), 
            selected = when(user?.weightUnit) {
                "Grams (g)" -> stringResource(R.string.grams)
                "Pounds (lb)" -> stringResource(R.string.pounds)
                else -> stringResource(R.string.kilograms)
            }, 
            options = listOf(stringResource(R.string.kilograms), stringResource(R.string.grams), stringResource(R.string.pounds)), 
            onSelected = { viewModel.updateSettings(weightUnit = it) }, 
            scale = scale
        )

        Spacer(modifier = Modifier.height(24.dp))
        InfoSectionTitle(stringResource(R.string.display_preferences), scale)
        ToggleRow(
            stringResource(R.string.dark_mode), 
            stringResource(R.string.dark_mode_desc), 
            isDarkMode, 
            { appViewModel.toggleDarkMode() }, 
            scale
        )
    }
}

@Composable
fun DataSyncScreen(appViewModel: AppViewModel, onBack: () -> Unit) {
    ProfileDetailScreenBase(stringResource(R.string.data_sync), onBack, appViewModel) { scale ->
        InfoSectionTitle(stringResource(R.string.cloud_sync), scale)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFF0F0F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDone, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(stringResource(R.string.data_up_to_date), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.last_synced), fontSize = 11.sp, color = Color(0xFF757575))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        var autoSync by remember { mutableStateOf(true) }
        ToggleRow(stringResource(R.string.auto_sync), stringResource(R.string.auto_sync_desc), autoSync, { autoSync = it }, scale)
        
        Spacer(modifier = Modifier.height(32.dp))
        PremiumButton(stringResource(R.string.sync_now), scale) { }
    }
}

@Composable
fun BackupRestoreScreen(appViewModel: AppViewModel, onBack: () -> Unit) {
    ProfileDetailScreenBase(stringResource(R.string.backup_restore), onBack, appViewModel) { scale ->
        InfoSectionTitle(stringResource(R.string.manage_backups), scale)
        
        PremiumButton(stringResource(R.string.create_backup), scale, icon = Icons.Default.Add) { }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.previous_backups), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
        
        repeat(3) { 
            BackupItem("Backup_2025_06_15.nrf", stringResource(R.string.backup_file_detail, "Size: 4.2 MB", "15 June 2025"), scale)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun ExportDataScreen(appViewModel: AppViewModel, onBack: () -> Unit) {
    val completeDataText = stringResource(R.string.complete_data)
    val pdfDocText = stringResource(R.string.pdf_doc)

    ProfileDetailScreenBase(stringResource(R.string.export_data), onBack, appViewModel) { scale ->
        var selectedType by remember(completeDataText) { mutableStateOf(completeDataText) }
        var selectedFormat by remember(pdfDocText) { mutableStateOf(pdfDocText) }

        InfoSectionTitle(stringResource(R.string.export_config), scale)
        PreferenceDropdown(stringResource(R.string.data_range), selectedType, listOf(stringResource(R.string.complete_data), stringResource(R.string.current_batch_only), stringResource(R.string.climate_logs_only)), { selectedType = it }, scale)
        Spacer(modifier = Modifier.height(16.dp))
        PreferenceDropdown(stringResource(R.string.file_format), selectedFormat, listOf(stringResource(R.string.pdf_doc), stringResource(R.string.excel_sheet), stringResource(R.string.csv_file)), { selectedFormat = it }, scale)

        Spacer(modifier = Modifier.height(32.dp))
        PremiumButton(stringResource(R.string.export_data), scale, icon = Icons.Default.FileDownload) { }
    }
}

@Composable
fun HelpSupportScreen(appViewModel: AppViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val email = stringResource(R.string.support_email)
    val phone = stringResource(R.string.support_phone)

    ProfileDetailScreenBase(stringResource(R.string.help_support), onBack, appViewModel) { scale ->
        InfoSectionTitle(stringResource(R.string.faqs), scale)
        
        val faqs = listOf(
            stringResource(R.string.faq_q1) to stringResource(R.string.faq_a1),
            stringResource(R.string.faq_q2) to stringResource(R.string.faq_a2),
            stringResource(R.string.faq_q3) to stringResource(R.string.faq_a3)
        )
        
        faqs.forEach { (q, a) ->
            FAQItem(q, a, scale)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height((24 * scale).dp))
        InfoSectionTitle(stringResource(R.string.contact_channels), scale)
        
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFF0F0F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.support_desc),
                    fontSize = (12 * scale).sp,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                ContactRow(Icons.Default.Email, email, scale) {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$email")
                        putExtra(Intent.EXTRA_SUBJECT, "Namma Reshme Support Request")
                    }
                    context.startActivity(intent)
                }
                Spacer(modifier = Modifier.height(10.dp))
                ContactRow(Icons.Default.Call, phone, scale) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phone")
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun AboutAppScreen(
    appViewModel: AppViewModel, 
    onBack: () -> Unit,
    onNavigateToTerms: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {}
) {
    ProfileDetailScreenBase(stringResource(R.string.about_app), onBack, appViewModel) { scale ->
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(15.dp)
            ) {
                Image(painter = painterResource(id = R.drawable.logo), contentDescription = null, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.app_title), fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(stringResource(R.string.version_text), fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
            
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFF0F0F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AboutLinkRow(stringResource(R.string.privacy_policy), scale, onNavigateToPrivacy)
                    HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                    AboutLinkRow(stringResource(R.string.terms_service), scale, onNavigateToTerms)
                    HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                    AboutLinkRow(stringResource(R.string.licenses), scale) { }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            Text(stringResource(R.string.developed_by), fontSize = 11.sp, color = Color(0xFFBDBDBD), fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun TermsAndConditionsScreen(appViewModel: AppViewModel, onBack: () -> Unit) {
    ProfileDetailScreenBase(stringResource(R.string.terms_service), onBack, appViewModel) { scale ->
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFF0F0F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.welcome_legal),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                LegalTextSection(stringResource(R.string.legal_s1_title), stringResource(R.string.legal_s1_body), scale)
                LegalTextSection(stringResource(R.string.legal_s2_title), stringResource(R.string.legal_s2_body), scale)
                LegalTextSection(stringResource(R.string.legal_s3_title), stringResource(R.string.legal_s3_body), scale)
                LegalTextSection(stringResource(R.string.legal_s4_title), stringResource(R.string.legal_s4_body), scale)
                LegalTextSection(stringResource(R.string.legal_s5_title), stringResource(R.string.legal_s5_body), scale)
            }
        }
    }
}

@Composable
fun PrivacyPolicyScreen(appViewModel: AppViewModel, onBack: () -> Unit) {
    ProfileDetailScreenBase(stringResource(R.string.privacy_policy), onBack, appViewModel) { scale ->
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFF0F0F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.privacy_welcome),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                LegalTextSection(stringResource(R.string.priv_s1_title), stringResource(R.string.priv_s1_body), scale)
                LegalTextSection(stringResource(R.string.priv_s2_title), stringResource(R.string.priv_s2_body), scale)
                LegalTextSection(stringResource(R.string.priv_s3_title), stringResource(R.string.priv_s3_body), scale)
                LegalTextSection(stringResource(R.string.priv_s4_title), stringResource(R.string.priv_s4_body), scale)
            }
        }
    }
}

@Composable
fun LegalTextSection(title: String, body: String, scale: Float) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, lineHeight = 18.sp)
        Text(body, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 18.sp)
    }
}

@Composable
fun FarmDetailsScreen(appViewModel: AppViewModel, onBack: () -> Unit) {
    ProfileDetailScreenBase(stringResource(R.string.view_farm_details), onBack, appViewModel) { scale ->
        InfoSectionTitle(stringResource(R.string.farm_infra), scale)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFF0F0F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FarmDetailRow(stringResource(R.string.rearing_house_type), stringResource(R.string.controlled_env), scale)
                FarmDetailRow(stringResource(R.string.total_floor_area), stringResource(R.string.sq_ft_format, "1,200"), scale)
                FarmDetailRow(stringResource(R.string.shelf_capacity), stringResource(R.string.trays_format, "40"), scale)
                FarmDetailRow(stringResource(R.string.mulberry_variety), stringResource(R.string.variety_format, "V1"), scale)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        InfoSectionTitle(stringResource(R.string.water_irrigation), scale)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFF0F0F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FarmDetailRow(stringResource(R.string.water_source), stringResource(R.string.borewell), scale)
                FarmDetailRow(stringResource(R.string.irrigation_type), stringResource(R.string.drip_system), scale)
            }
        }
    }
}

// Helper Components

@Composable
fun FarmDetailRow(label: String, value: String, scale: Float) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = (12 * scale).sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f), lineHeight = 16.sp)
        Text(value, fontSize = (13 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun InfoSectionTitle(title: String, scale: Float) {
    Text(
        text = title,
        fontSize = (13 * scale).sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
        lineHeight = 17.sp
    )
}

@Composable
fun SettingsTextField(label: String, value: String, onValueChange: (String) -> Unit, scale: Float, isPassword: Boolean = false) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(label, fontSize = (11 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp), lineHeight = 14.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = Color(0xFFF0F0F0),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            textStyle = LocalTextStyle.current.copy(fontSize = (14 * scale).sp, lineHeight = 18.sp)
        )
    }
}

@Composable
fun PremiumButton(text: String, scale: Float, icon: ImageVector? = null, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text, fontSize = (15 * scale).sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun LanguageRow(name: String, selected: Boolean, onClick: () -> Unit, scale: Float) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, fontSize = (14 * scale).sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF333333), lineHeight = 18.sp)
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun ToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, scale: Float) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = (13 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, lineHeight = (18 * scale).sp)
            Text(subtitle, fontSize = (10 * scale).sp, color = Color(0xFF757575), lineHeight = (14 * scale).sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun PreferenceDropdown(label: String, selected: String, options: List<String>, onSelected: (String) -> Unit, scale: Float) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, fontSize = (11 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp), lineHeight = 14.sp)
        Box {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFF0F0F0))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(selected, fontSize = (14 * scale).sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium, lineHeight = 18.sp)
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { onSelected(option); expanded = false })
                }
            }
        }
    }
}

@Composable
fun BackupItem(name: String, detail: String, scale: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.InsertDriveFile, null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, lineHeight = 16.sp)
                Text(detail, fontSize = (10 * scale).sp, color = Color(0xFF757575), lineHeight = 13.sp)
            }
            IconButton(onClick = { }) { Icon(Icons.Default.Restore, null, tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = { }) { Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFFE57373)) }
        }
    }
}

@Composable
fun FAQItem(question: String, answer: String, scale: Float) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(question, fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f), lineHeight = (18 * scale).sp)
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = MaterialTheme.colorScheme.primary)
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(answer, fontSize = (11 * scale).sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
fun ContactRow(icon: ImageVector, text: String, scale: Float, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
        border = BorderStroke(1.dp, Color(0xFFC8E6C9))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, fontSize = (13 * scale).sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f), lineHeight = 17.sp)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun AboutLinkRow(text: String, scale: Float, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text, fontSize = (13 * scale).sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary, lineHeight = 17.sp)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(16.dp), tint = Color(0xFFBDBDBD))
    }
}
