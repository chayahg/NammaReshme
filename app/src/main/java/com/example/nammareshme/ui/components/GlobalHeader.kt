package com.example.nammareshme.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammareshme.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalHeader(
    scale: Float = 1f,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    currentLanguage: String = "English",
    onLanguageChange: (String) -> Unit = {},
    unreadNotificationCount: Int = 0,
    onNotificationClick: () -> Unit = {}
) {
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = Modifier.height(70.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxHeight().clickable { 
                    if (showBackButton) onBackClick() else onSettingsClick() 
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size((38 * scale).dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = stringResource(R.string.app_title),
                        fontSize = (15 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.app_subtitle),
                        fontSize = (9 * scale).sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Box {
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = stringResource(R.string.menu),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.account_settings)) },
                            leadingIcon = { Icon(Icons.Default.Settings, null, Modifier.size(20.dp)) },
                            onClick = {
                                showOptionsMenu = false
                                onSettingsClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.help_support)) },
                            leadingIcon = { Icon(Icons.Default.HelpOutline, null, Modifier.size(20.dp)) },
                            onClick = {
                                showOptionsMenu = false
                                onHelpClick()
                            }
                        )
                    }
                }
            }
        },
        actions = {
            // Language Selector
            Box {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .height((32 * scale).dp)
                        .clickable { showLanguageMenu = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Icon(
                            Icons.Default.Language,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentLanguage == "English") stringResource(R.string.english) else stringResource(R.string.kannada),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                DropdownMenu(
                    expanded = showLanguageMenu,
                    onDismissRequest = { showLanguageMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.english)) },
                        onClick = {
                            onLanguageChange("English")
                            showLanguageMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.kannada)) },
                        onClick = {
                            onLanguageChange("Kannada")
                            showLanguageMenu = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Notification Bell
            IconButton(onClick = onNotificationClick) {
                Box {
                    Icon(
                        Icons.Outlined.Notifications,
                        stringResource(R.string.notifications),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    if (unreadNotificationCount > 0) {
                        Surface(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp),
                            shape = CircleShape,
                            color = Color(0xFFD32F2F)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = unreadNotificationCount.toString(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}
