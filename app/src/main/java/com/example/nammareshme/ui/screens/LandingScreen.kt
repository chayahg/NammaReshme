package com.example.nammareshme.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammareshme.R
import kotlinx.coroutines.delay

@Composable
fun LandingScreen(onNavigateToAuth: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 3000),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        progress = 1f
        delay(3500)
        onNavigateToAuth()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.landing_page),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.7f))
            
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val appTitle = stringResource(R.string.app_title)
            val parts = appTitle.split("-")
            
            Text(
                text = parts.firstOrNull() ?: "Reshme",
                color = Color(0xFF1B4332),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = parts.getOrNull(1) ?: "Namma Pride",
                color = Color(0xFF2D6A4F),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.offset(y = (-8).dp)
            )

            Text(
                text = stringResource(R.string.app_subtitle),
                color = Color(0xFF52796F),
                fontSize = 14.sp,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.weight(0.45f))

            Surface(
                color = Color(0x55F1F8E9), 
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FeatureIcon(R.drawable.ic_thermometer, stringResource(R.string.climate_monitoring))
                    FeatureIcon(R.drawable.ic_silkworm, stringResource(R.string.instar_based_advice))
                    FeatureIcon(R.drawable.ic_bell, stringResource(R.string.smart_alerts_feat))
                    FeatureIcon(R.drawable.ic_speaker, stringResource(R.string.voice_guidance))
                }
            }

            Spacer(modifier = Modifier.weight(0.7f))
            
            Text(
                text = stringResource(R.string.slogan_text),
                textAlign = TextAlign.Center,
                color = Color(0xFF1B4332),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Serif,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.weight(8.5f))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.made_for_silk_farmers),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.2.sp
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(0.8.dp)
                    .clip(CircleShape),
                color = Color.White.copy(alpha = 0.55f),
                trackColor = Color.White.copy(alpha = 0.04f)
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = stringResource(R.string.loading).uppercase(),
                color = Color.White.copy(alpha = 0.25f),
                fontSize = 8.5.sp,
                letterSpacing = 1.8.sp
            )
        }
    }
}

@Composable
fun FeatureIcon(resId: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF1B4332),
            lineHeight = 11.sp,
            modifier = Modifier.padding(top = 4.dp),
            fontWeight = FontWeight.Bold
        )
    }
}
