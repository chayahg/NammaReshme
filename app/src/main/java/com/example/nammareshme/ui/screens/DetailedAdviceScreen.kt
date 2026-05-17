package com.example.nammareshme.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun DetailedAdviceScreen(
    appViewModel: AppViewModel,
    type: String,
    onBack: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenW = configuration.screenWidthDp.dp
    val scale = (screenW.value / 400f).coerceIn(0.85f, 1.2f)

    val currentLanguage by appViewModel.currentLanguage.collectAsState()
    val unreadNotifications by appViewModel.unreadNotifications.collectAsState()
    
    val detail = getAdviceDetail(type)

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
        containerColor = Color(0xFFF9F7F2)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = (24 * scale).dp)
        ) {
            Spacer(modifier = Modifier.height((16 * scale).dp))

            // Topic Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size((56 * scale).dp),
                    shape = RoundedCornerShape(16.dp),
                    color = detail.color.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(detail.icon, null, tint = detail.color, modifier = Modifier.size((28 * scale).dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = stringResource(detail.titleRes), fontSize = (22 * scale).sp, fontWeight = FontWeight.Black, color = Color(0xFF1B4332), lineHeight = (28 * scale).sp)
                    Text(text = stringResource(detail.taglineRes), fontSize = (13 * scale).sp, color = Color(0xFF52796F), lineHeight = (18 * scale).sp)
                }
            }

            Spacer(modifier = Modifier.height((28 * scale).dp))

            // Expert Guidance Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding((20 * scale).dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = detail.color, modifier = Modifier.size((18 * scale).dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.expert_guidance), fontSize = (16 * scale).sp, fontWeight = FontWeight.Bold, color = detail.color)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(detail.contentRes),
                        fontSize = (15 * scale).sp,
                        lineHeight = (22 * scale).sp,
                        color = Color(0xFF1B4332).copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height((28 * scale).dp))

            // Actionable Steps
            Text(text = stringResource(R.string.actionable_steps), fontSize = (17 * scale).sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B4332), modifier = Modifier.padding(bottom = 14.dp))
            
            detail.tipsRes.forEach { tipRes ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0).copy(alpha = 0.5f)),
                    modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding((16 * scale).dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = detail.color) {}
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(text = stringResource(tipRes), fontSize = (14 * scale).sp, color = Color(0xFF1B4332), fontWeight = FontWeight.Medium, lineHeight = (18 * scale).sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height((20 * scale).dp))

            // Quality Assurance Note
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = detail.color.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, detail.color.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding((16 * scale).dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, null, tint = detail.color, modifier = Modifier.size((20 * scale).dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.qa_note),
                        fontSize = (13 * scale).sp,
                        color = detail.color,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (18 * scale).sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height((32 * scale).dp))
        }
    }
}

data class AdviceTopic(
    val titleRes: Int,
    val taglineRes: Int,
    val icon: ImageVector,
    val color: Color,
    val contentRes: Int,
    val tipsRes: List<Int>
)

private fun getAdviceDetail(type: String): AdviceTopic {
    return when (type) {
        "Ventilation" -> AdviceTopic(
            R.string.adv_vent_title,
            R.string.adv_vent_tagline,
            Icons.Default.Air,
            Color(0xFF3498DB),
            R.string.adv_vent_content,
            listOf(
                R.string.adv_vent_tip1,
                R.string.adv_vent_tip2,
                R.string.adv_vent_tip3,
                R.string.adv_vent_tip4
            )
        )
        "Cleaning" -> AdviceTopic(
            R.string.adv_clean_title,
            R.string.adv_clean_tagline,
            Icons.Default.CleaningServices,
            Color(0xFFE67E22),
            R.string.adv_clean_content,
            listOf(
                R.string.adv_clean_tip1,
                R.string.adv_clean_tip2,
                R.string.adv_clean_tip3,
                R.string.adv_clean_tip4
            )
        )
        "Mulberry Leaves" -> AdviceTopic(
            R.string.adv_feed_title,
            R.string.adv_feed_tagline,
            Icons.Default.Spa,
            Color(0xFF9C27B0),
            R.string.adv_feed_content,
            listOf(
                R.string.adv_feed_tip1,
                R.string.adv_feed_tip2,
                R.string.adv_feed_tip3,
                R.string.adv_feed_tip4
            )
        )
        else -> AdviceTopic(
            R.string.adv_ideal_title,
            R.string.adv_ideal_tagline,
            Icons.Default.Eco,
            Color(0xFF2D6A4F),
            R.string.adv_ideal_content,
            listOf(
                R.string.adv_ideal_tip1,
                R.string.adv_ideal_tip2,
                R.string.adv_ideal_tip3,
                R.string.adv_ideal_tip4
            )
        )
    }
}
