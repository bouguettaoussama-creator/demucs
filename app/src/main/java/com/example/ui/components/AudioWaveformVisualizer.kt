package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonRose
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.StemInstrumentalColor
import com.example.ui.theme.StemVocalsColor
import com.example.ui.theme.StudioCardElevated
import kotlin.math.max

@Composable
fun DualWaveformVisualizer(
    vocalWaveform: FloatArray,
    instWaveform: FloatArray,
    currentProgress: Float, // 0.0f .. 1.0f
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StudioCardElevated)
            .padding(16.dp)
            .testTag("dual_waveform_visualizer")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(StemVocalsColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "صوت الغناء / الكلام (Vocals)",
                    color = StemVocalsColor,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(StemInstrumentalColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "الموسيقى والآلات (Music)",
                    color = StemInstrumentalColor,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dual Canvas Waveform with interactive touch-seek
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F121E))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek(ratio)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val midY = canvasHeight / 2f

                val pointsCount = max(vocalWaveform.size, 1)
                val barWidth = (canvasWidth / pointsCount) * 0.75f
                val spacing = (canvasWidth / pointsCount)

                // Playhead x position
                val playheadX = canvasWidth * currentProgress

                // Draw middle separator line
                drawLine(
                    color = Color(0xFF242A44),
                    start = Offset(0f, midY),
                    end = Offset(canvasWidth, midY),
                    strokeWidth = 1f
                )

                // Draw Vocal Waveform (Top Half)
                for (i in 0 until minOf(pointsCount, vocalWaveform.size)) {
                    val x = i * spacing + spacing * 0.12f
                    val amp = vocalWaveform[i]
                    val barHeight = amp * (midY - 4.dp.toPx())
                    val isPast = x <= playheadX

                    val barColor = if (isPast) {
                        StemVocalsColor.copy(alpha = if (isPlaying) pulseAlpha else 1.0f)
                    } else {
                        StemVocalsColor.copy(alpha = 0.35f)
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, midY - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }

                // Draw Instrumental Waveform (Bottom Half)
                for (i in 0 until minOf(pointsCount, instWaveform.size)) {
                    val x = i * spacing + spacing * 0.12f
                    val amp = instWaveform[i]
                    val barHeight = amp * (midY - 4.dp.toPx())
                    val isPast = x <= playheadX

                    val barColor = if (isPast) {
                        StemInstrumentalColor.copy(alpha = if (isPlaying) pulseAlpha else 1.0f)
                    } else {
                        StemInstrumentalColor.copy(alpha = 0.35f)
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, midY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }

                // Draw Glowing Playhead
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(NeonCyan, NeonRose, NeonViolet)
                    ),
                    start = Offset(playheadX, 0f),
                    end = Offset(playheadX, canvasHeight),
                    strokeWidth = 2.5.dp.toPx()
                )

                // Top playhead bead
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(playheadX, 4.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun VuMeterBar(
    leftLevel: Float, // 0.0 .. 1.0
    rightLevel: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F121E))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("L", color = Color(0xFF64748B), fontSize = 11.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF1E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(leftLevel.coerceIn(0f, 1f))
                    .height(6.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(NeonCyan, NeonViolet, NeonRose)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text("R", color = Color(0xFF64748B), fontSize = 11.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF1E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(rightLevel.coerceIn(0f, 1f))
                    .height(6.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(NeonCyan, NeonViolet, NeonRose)
                        )
                    )
            )
        }
    }
}
