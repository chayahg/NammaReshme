package com.example.nammareshme.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammareshme.R
import com.example.nammareshme.ui.components.GlobalHeader
import com.example.nammareshme.ui.viewmodel.AppViewModel
import com.example.nammareshme.ui.viewmodel.BatchViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBatchScreen(
    appViewModel: AppViewModel,
    batchViewModel: BatchViewModel,
    onBack: () -> Unit = {},
    onSaveSuccess: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToBatches: () -> Unit = {},
    onNavigateToHarvestTimer: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenW = configuration.screenWidthDp.dp
    val scale = (screenW.value / 400f).coerceIn(0.85f, 1.2f)
    
    val currentLanguage by appViewModel.currentLanguage.collectAsState()
    val unreadNotifications by appViewModel.unreadNotifications.collectAsState()

    // Form States
    var batchName by remember { mutableStateOf("") }
    var breedType by remember { mutableStateOf("") }
    var mulberryType by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())) }
    var instarStage by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Dialog Control
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

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
                currentRoute = "batches",
                onHome = onNavigateToHome,
                onBatches = onNavigateToBatches,
                onHarvestTimer = onNavigateToHarvestTimer,
                onReports = onNavigateToReports,
                onProfile = onNavigateToProfile
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = (20 * scale).dp)
        ) {
            Spacer(modifier = Modifier.height((16 * scale).dp))

            // --- HEADER ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size((48 * scale).dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_silkworm), 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size((24 * scale).dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        stringResource(R.string.add_batch),
                        fontSize = (22 * scale).sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = (28 * scale).sp
                    )
                    Text(
                        stringResource(R.string.add_batch_desc),
                        fontSize = (12 * scale).sp,
                        color = MaterialTheme.colorScheme.secondary,
                        lineHeight = (16 * scale).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height((24 * scale).dp))

            // --- FORM FIELDS ---
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding((18 * scale).dp)) {
                    // 1. Batch Name
                    SectionLabel(stringResource(R.string.batch_name_label))
                    CustomTextField(
                        value = batchName,
                        onValueChange = { batchName = it },
                        placeholder = stringResource(R.string.batch_name_placeholder),
                        icon = Icons.AutoMirrored.Filled.Label,
                        scale = scale
                    )
                    Text(
                        stringResource(R.string.batch_name_helper),
                        fontSize = (11 * scale).sp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height((20 * scale).dp))

                    // 2. Breed Type
                    SectionLabel(stringResource(R.string.breed_type_label))
                    val breeds = mapOf(
                        stringResource(R.string.bivoltine) to "Bivoltine",
                        stringResource(R.string.multivoltine) to "Multivoltine",
                        stringResource(R.string.cross_breed) to "Cross Breed"
                    )
                    CustomDropdownField(
                        value = breeds.filterValues { it == breedType }.keys.firstOrNull() ?: "",
                        placeholder = stringResource(R.string.breed_type_placeholder),
                        options = breeds.keys.toList(),
                        onOptionSelected = { breedType = breeds[it] ?: it },
                        iconPainter = painterResource(id = R.drawable.ic_silkworm),
                        scale = scale
                    )

                    Spacer(modifier = Modifier.height((20 * scale).dp))

                    // 3. Mulberry Type
                    SectionLabel(stringResource(R.string.mulberry_type_label))
                    val mulberryTypes = mapOf(
                        stringResource(R.string.v1) to "V1",
                        stringResource(R.string.s36) to "S36",
                        stringResource(R.string.g4) to "G4",
                        stringResource(R.string.local) to "Local"
                    )
                    CustomDropdownField(
                        value = mulberryTypes.filterValues { it == mulberryType }.keys.firstOrNull() ?: "",
                        placeholder = stringResource(R.string.mulberry_type_placeholder),
                        options = mulberryTypes.keys.toList(),
                        onOptionSelected = { mulberryType = mulberryTypes[it] ?: it },
                        iconPainter = painterResource(id = R.drawable.ic_leaf),
                        scale = scale
                    )

                    Spacer(modifier = Modifier.height((20 * scale).dp))

                    // 4 & 5. Date and Instar
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            SectionLabel(stringResource(R.string.start_date_label))
                            SelectionSurface(
                                value = startDate,
                                icon = Icons.Default.CalendarToday,
                                onClick = { showDatePicker = true },
                                scale = scale
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            SectionLabel(stringResource(R.string.instar_stage_label))
                            val stages = mapOf(
                                stringResource(R.string.instar_1) to "1st Instar",
                                stringResource(R.string.instar_2) to "2nd Instar",
                                stringResource(R.string.instar_3) to "3rd Instar",
                                stringResource(R.string.instar_4) to "4th Instar",
                                stringResource(R.string.instar_5) to "5th Instar"
                            )
                            CustomDropdownField(
                                value = stages.filterValues { it == instarStage }.keys.firstOrNull() ?: "",
                                placeholder = stringResource(R.string.instar_stage_placeholder),
                                options = stages.keys.toList(),
                                onOptionSelected = { instarStage = stages[it] ?: it },
                                iconPainter = painterResource(id = R.drawable.ic_silkworm),
                                scale = scale
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height((20 * scale).dp))

                    // 6. Notes
                    SectionLabel(stringResource(R.string.notes_label))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { if (it.length <= 200) notes = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = (100 * scale).dp),
                        placeholder = { Text(stringResource(R.string.notes_placeholder), fontSize = (14 * scale).sp) },
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(20.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    Text(
                        "${notes.length}/200",
                        fontSize = (11 * scale).sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height((20 * scale).dp))

            // Tip Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding((16 * scale).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lightbulb, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size((24 * scale).dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.tip_text),
                        fontSize = (12 * scale).sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height((32 * scale).dp))

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = (32 * scale).dp)) {
                OutlinedButton(
                    onClick = { onBack() },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        if (batchName.isBlank() || breedType.isBlank() || mulberryType.isBlank() || instarStage.isBlank()) {
                            Toast.makeText(context, context.getString(R.string.fill_required), Toast.LENGTH_SHORT).show()
                        } else {
                            val newBatch = BatchHistoryItem(
                                id = batchName,
                                breed = breedType,
                                mulberryType = mulberryType,
                                startDate = startDate,
                                status = BatchStatus.ACTIVE,
                                currentStage = instarStage,
                                progress = when(instarStage) {
                                    "1st Instar" -> 0.1f
                                    "2nd Instar" -> 0.3f
                                    "3rd Instar" -> 0.5f
                                    "4th Instar" -> 0.7f
                                    "5th Instar" -> 0.9f
                                    else -> 0f
                                },
                                notes = notes,
                                timestamp = System.currentTimeMillis()
                            )
                            batchViewModel.addBatch(newBatch, {
                                Toast.makeText(context, context.getString(R.string.save_success), Toast.LENGTH_SHORT).show()
                                onSaveSuccess()
                            }, { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            })
                        }
                    },
                    modifier = Modifier.weight(1.5f).height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.save_batch), fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }

    if (showDatePicker) {
        val confirmText = stringResource(R.string.confirm)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance().apply { timeInMillis = it }
                        startDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.time)
                    }
                    showDatePicker = false
                }) { Text(confirmText, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp),
        lineHeight = 16.sp
    )
}

@Composable
private fun CustomTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, icon: ImageVector, scale: Float) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, fontSize = (14 * scale).sp) },
        leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(20.dp)) },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        singleLine = true
    )
}

@Composable
private fun CustomDropdownField(value: String, placeholder: String, options: List<String>, onOptionSelected: (String) -> Unit, iconPainter: androidx.compose.ui.graphics.painter.Painter, scale: Float) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable { expanded = true },
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(iconPainter, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (value.isEmpty()) placeholder else value,
                    color = if (value.isEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                    fontSize = (14 * scale).sp,
                    modifier = Modifier.weight(1f),
                    lineHeight = 18.sp
                )
                Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) }, 
                    onClick = { onOptionSelected(option); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun SelectionSurface(value: String, icon: ImageVector, onClick: () -> Unit, scale: Float) {
    Surface(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = value, fontSize = (14 * scale).sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
        }
    }
}
