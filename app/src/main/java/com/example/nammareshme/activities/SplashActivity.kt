package com.example.nammareshme.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammareshme.R
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SplashUI {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashUI(onComplete: () -> Unit) {
    var targetProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 3000),
        label = "splash_progress_anim"
    )

    LaunchedEffect(Unit) {
        targetProgress = 1f
        delay(3200)
        onComplete()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenW = maxWidth
        val screenH = maxHeight
        val isVerySmall = screenH < 500.dp

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
            Spacer(modifier = Modifier.weight(1.2f))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .fillMaxHeight(if (isVerySmall) 0.22f else 0.28f)
                    .aspectRatio(1f, matchHeightConstraintsFirst = true)
            )

            Spacer(modifier = Modifier.weight(0.4f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val appTitle = stringResource(R.string.app_title)
                val parts = appTitle.split("-")
                Text(
                    text = parts.firstOrNull() ?: "",
                    color = Color(0xFF1B4332),
                    fontSize = (screenW.value * 0.082f).sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = (-0.5).sp
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = (screenH.value * 0.005f).dp)
                ) {
                    Box(modifier = Modifier.width(screenW * 0.16f).height(1.2.dp).background(Color(0xFF2D6A4F).copy(alpha = 0.4f)))
                    Box(modifier = Modifier.padding(horizontal = 6.dp).size(6.dp).background(Color(0xFF2D6A4F), RoundedCornerShape(3.dp)))
                    Box(modifier = Modifier.width(screenW * 0.16f).height(1.2.dp).background(Color(0xFF2D6A4F).copy(alpha = 0.4f)))
                }

                Text(
                    text = parts.getOrNull(1) ?: "",
                    color = Color(0xFF2D6A4F),
                    fontSize = (screenW.value * 0.11f).sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.offset(y = (-screenW.value * 0.02f).dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_subtitle) + " ",
                    color = Color(0xFF2D6A4F),
                    fontSize = (screenW.value * 0.038f).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_leaf),
                    contentDescription = null,
                    modifier = Modifier.size((screenW.value * 0.045f).dp)
                )
            }

            Spacer(modifier = Modifier.weight(0.6f))

            Surface(
                color = Color(0xAAF1F8E9),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth(0.86f)
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = (screenH.value * 0.012f).dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ResponsiveIconItem(R.drawable.ic_thermometer, stringResource(R.string.climate_monitoring).replace("\n", " "), screenW)
                    ResponsiveDivider(screenH)
                    ResponsiveIconItem(R.drawable.ic_silkworm, stringResource(R.string.instar_based_advice).replace("\n", " "), screenW)
                    ResponsiveDivider(screenH)
                    ResponsiveIconItem(R.drawable.ic_bell, stringResource(R.string.smart_alerts_feat).replace("\n", " "), screenW)
                    ResponsiveDivider(screenH)
                    ResponsiveIconItem(R.drawable.ic_speaker, stringResource(R.string.voice_guidance).replace("\n", " "), screenW)
                }
            }

            Spacer(modifier = Modifier.weight(0.8f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = screenW * 0.08f)
            ) {
                Text(
                    text = stringResource(R.string.slogan_text),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF1B4332),
                    fontSize = (screenW.value * 0.045f).sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    lineHeight = (screenW.value * 0.058f).sp
                )
                
                Spacer(modifier = Modifier.height((screenH.value * 0.01f).dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(screenW * 0.08f).height(1.dp).background(Color(0xFF2D6A4F).copy(alpha = 0.3f)))
                    Image(
                        painter = painterResource(id = R.drawable.ic_leaf),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size((screenW.value * 0.055f).dp)
                            .alpha(0.8f)
                    )
                    Box(modifier = Modifier.width(screenW * 0.08f).height(1.dp).background(Color(0xFF2D6A4F).copy(alpha = 0.3f)))
                }
            }
            
            Spacer(modifier = Modifier.weight(2.5f))
        }

        BottomAdaptiveSection(
            progress = animatedProgress,
            modifier = Modifier.align(Alignment.BottomCenter),
            w = screenW,
            h = screenH
        )
    }
}

@Composable
fun ResponsiveIconItem(res: Int, text: String, screenW: androidx.compose.ui.unit.Dp) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(screenW * 0.2f)
    ) {
        Image(
            painter = painterResource(id = res),
            contentDescription = null,
            modifier = Modifier.size((screenW.value * 0.07f).dp)
        )
        Text(
            text = text,
            fontSize = (screenW.value * 0.024f).sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF1B4332),
            modifier = Modifier.padding(top = 4.dp),
            lineHeight = (screenW.value * 0.03f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ResponsiveDivider(screenH: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(0.5.dp)
            .height(screenH * 0.035f)
            .background(Color(0xFF1B4332).copy(alpha = 0.2f))
    )
}

@Composable
fun BottomAdaptiveSection(
    progress: Float, 
    modifier: Modifier,
    w: androidx.compose.ui.unit.Dp,
    h: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(h * 0.10f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_leaf),
                    contentDescription = null,
                    modifier = Modifier.size((w.value * 0.045f).dp).alpha(0.9f)
                )
                Text(
                    text = " " + stringResource(R.string.made_for_silk_farmers) + " ",
                    color = Color.White,
                    fontSize = (w.value * 0.035f).sp,
                    fontWeight = FontWeight.Medium
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_leaf),
                    contentDescription = null,
                    modifier = Modifier.size((w.value * 0.045f).dp).alpha(0.9f)
                )
            }

            Spacer(modifier = Modifier.height((h.value * 0.01f).dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp)),
                color = Color.White,
                trackColor = Color(0x33FFFFFF)
            )

            Spacer(modifier = Modifier.height((h.value * 0.005f).dp))

            Text(
                text = stringResource(R.string.loading),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = (w.value * 0.028f).sp
            )
        }
    }
}
