package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonRose
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SliderTrackColor
import com.example.ui.theme.StemVocalsColor
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainViewModel

@Composable
fun LiveScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val liveState by viewModel.liveMic.state.collectAsState()
    var monitorHeadphones by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val micRingScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringScale"
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.liveMic.start(monitorOutput = monitorHeadphones)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Live Processing Header Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("live_header_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCard)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0C4A6E).copy(alpha = 0.8f), Color(0xFF0F172A).copy(alpha = 0.9f))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NeonCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "المعالجة المباشرة (Live DSP)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "عزل وتنقية الصوت البشري فورياً عبر الميكروفون",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }
        }

        // Live Mic Control Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("live_mic_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCard)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Glowing Microphone Button
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            if (liveState.isRunning) {
                                Brush.radialGradient(listOf(NeonCyan.copy(alpha = 0.4f), Color.Transparent))
                            } else {
                                Brush.radialGradient(listOf(Color(0xFF1E293B), Color.Transparent))
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (liveState.isRunning) (80 * micRingScale).dp else 80.dp)
                            .clip(CircleShape)
                            .background(
                                if (liveState.isRunning) {
                                    Brush.linearGradient(listOf(NeonRose, NeonCyan))
                                } else {
                                    Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                                }
                            )
                            .border(
                                2.dp,
                                if (liveState.isRunning) NeonCyan else Color(0xFF475569),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (liveState.isRunning) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = if (liveState.isRunning) "إيقاف الميكروفون" else "تشغيل الميكروفون",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (liveState.isRunning) {
                            viewModel.liveMic.stop()
                        } else {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                viewModel.liveMic.start(monitorOutput = monitorHeadphones)
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("toggle_live_mic_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (liveState.isRunning) NeonRose else NeonCyan
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (liveState.isRunning) "إيقاف المعالجة المباشرة" else "بدء التقاط وعزل الصوت المباشر",
                        fontWeight = FontWeight.Bold,
                        color = if (liveState.isRunning) Color.White else Color.Black,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Real-time Oscilloscope Waveform Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F121E))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasW = size.width
                        val canvasH = size.height
                        val midY = canvasH / 2f
                        val wave = liveState.recentWaveform
                        val count = maxOf(wave.size, 1)
                        val step = canvasW / count
                        val barWidth = step * 0.7f

                        for (i in wave.indices) {
                            val amp = wave[i]
                            val h = amp * (midY - 4.dp.toPx())
                            val x = i * step

                            drawRoundRect(
                                color = if (liveState.isRunning) StemVocalsColor else Color(0xFF334155),
                                topLeft = Offset(x, midY - h),
                                size = Size(barWidth, h * 2f),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Vocal Energy VU level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "مستوى نقاء الصوت البشري:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "${(liveState.vocalLevel * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(liveState.vocalLevel.coerceIn(0f, 1f))
                            .height(10.dp)
                            .background(Brush.horizontalGradient(listOf(NeonCyan, NeonViolet)))
                    )
                }
            }
        }

        // Live Settings & Filters Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("live_settings_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "مرشحات المعالجة المباشرة:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Noise Gate Fader
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("تقليل الضوضاء المحيطة:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("${(liveState.noiseReduction * 100).toInt()}%", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = liveState.noiseReduction,
                    onValueChange = { viewModel.liveMic.setNoiseReduction(it) },
                    valueRange = 0f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan, inactiveTrackColor = SliderTrackColor)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Vocal Boost Fader
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("تعزيز نبرة الكلام والغناء:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("${(liveState.vocalBoost * 100).toInt()}%", color = NeonViolet, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = liveState.vocalBoost,
                    onValueChange = { viewModel.liveMic.setVocalBoost(it) },
                    valueRange = 0.5f..2.5f,
                    colors = SliderDefaults.colors(thumbColor = NeonViolet, activeTrackColor = NeonViolet, inactiveTrackColor = SliderTrackColor)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Headphone Monitor Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Headphones, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("استماع مباشر عبر السماعات", color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text("مراقبة المخرجات المعزولة فورياً (Monitors)", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    Switch(
                        checked = monitorHeadphones,
                        onCheckedChange = {
                            monitorHeadphones = it
                            if (liveState.isRunning) {
                                viewModel.liveMic.stop()
                                viewModel.liveMic.start(monitorOutput = it)
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NeonAmber)
                    )
                }
            }
        }
    }
}
