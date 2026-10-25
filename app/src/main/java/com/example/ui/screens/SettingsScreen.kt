package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DemucsPreset
import com.example.model.DemucsProfile
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonRose
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonVioletDark
import com.example.ui.theme.SliderTrackColor
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Card(
            modifier = Modifier.fillMaxWidth().testTag("settings_header_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCard)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF3B0764).copy(alpha = 0.8f), Color(0xFF0F172A).copy(alpha = 0.9f))
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonViolet),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "إعدادات Demucs المتقدمة",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "ضبط معلمات الذكاء الاصطناعي ومعالجة الإشارات الصوتية",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCyan
                        )
                    }
                }
            }
        }

        // Presets Section (الأنماط الافتراضية المناسبة)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("presets_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الأنماط المسبقة الجاهزة (Presets):",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                DemucsPreset.values().forEach { preset ->
                    val isSelected = settings.activePreset == preset
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NeonVioletDark.copy(alpha = 0.4f) else StudioCardElevated)
                            .border(
                                1.dp,
                                if (isSelected) NeonViolet else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setPreset(preset) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) NeonCyan else TextPrimary
                                )
                                Text(
                                    text = preset.description,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fine Parameter Tuning (ضبط معلمات النموذج بالتفصيل)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("parameters_tuning_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "معلمات النموذج الدقيقة:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Separation Intensity (مستوى فصل الآلات الموسيقية عن الصوت أو الغناء)
                ParameterSliderItem(
                    title = "مستوى فصل الآلات الموسيقية عن الغناء",
                    description = "يحدد مدى قوة وسرعة عزل الآلات وترك مسار الكاريوكي بدون صوت غنائي",
                    value = settings.separationIntensity,
                    valueText = "${(settings.separationIntensity * 100).toInt()}%",
                    range = 0.1f..1.0f,
                    color = NeonCyan,
                    onValueChange = { viewModel.setSeparationIntensity(it) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Vocal Sensitivity
                ParameterSliderItem(
                    title = "حساسية التقاط الترددات الصوتية (Vocal Sensitivity)",
                    description = "عتبة كشف الصوت البشري ونغمات الغناء في المنتصف",
                    value = settings.vocalSensitivity,
                    valueText = "${(settings.vocalSensitivity * 100).toInt()}%",
                    range = 0.1f..1.0f,
                    color = NeonViolet,
                    onValueChange = { viewModel.setVocalSensitivity(it) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Bleed Reduction
                ParameterSliderItem(
                    title = "تقليل تسريب الموسيقى (Harmonic Bleed Reduction)",
                    description = "إزالة الصدى والترددات الموسيقية المتسربة لمسار الغناء",
                    value = settings.harmonicBleedReduction,
                    valueText = "${(settings.harmonicBleedReduction * 100).toInt()}%",
                    range = 0f..1.0f,
                    color = NeonRose,
                    onValueChange = { viewModel.setHarmonicBleedReduction(it) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Stereo Phase Sensitivity
                ParameterSliderItem(
                    title = "إلغاء الطور الاستريو (Stereo Phase Cancellation)",
                    description = "عزل المسار الأوسط (Mid-Side) وإزالة المؤثرات المجسمة الجانبية",
                    value = settings.stereoPhaseSensitivity,
                    valueText = "${(settings.stereoPhaseSensitivity * 100).toInt()}%",
                    range = 0.1f..1.0f,
                    color = NeonAmber,
                    onValueChange = { viewModel.setStereoPhaseSensitivity(it) }
                )
            }
        }

        // Model Profile Selection
        Card(
            modifier = Modifier.fillMaxWidth().testTag("profiles_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "بنية النموذج ومستوى المعالجة:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                DemucsProfile.values().forEach { profile ->
                    val isSelected = settings.modelProfile == profile
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setModelProfile(profile) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.setModelProfile(profile) },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = profile.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonCyan else TextPrimary
                            )
                            Text(
                                text = profile.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // MP3 Export Quality Settings
        Card(
            modifier = Modifier.fillMaxWidth().testTag("mp3_settings_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "جودة تصدير ملفات MP3:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "يتم حفظ جميع المسارات المنفصلة تلقائياً بصيغة MP3 المعيارية",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val bitrates = listOf(128, 192, 256, 320)
                    bitrates.forEach { br ->
                        val isSel = settings.mp3BitrateKbps == br
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) NeonVioletDark else StudioCardElevated)
                                .border(1.dp, if (isSel) NeonViolet else Color(0xFF242A44), RoundedCornerShape(10.dp))
                                .clickable { viewModel.setMp3Bitrate(br) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$br k",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSel) Color.White else TextPrimary
                                )
                                Text(
                                    text = if (br == 320) "جودة قصوى" else if (br == 256) "موصى به" else "متوازن",
                                    fontSize = 9.sp,
                                    color = if (isSel) NeonCyan else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reset to Defaults Button
        OutlinedButton(
            onClick = { viewModel.resetSettingsToDefault() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("reset_settings_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRose)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = NeonRose)
            Spacer(modifier = Modifier.width(8.dp))
            Text("استعادة كافة الإعدادات إلى الوضع الافتراضي", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ParameterSliderItem(
    title: String,
    description: String,
    value: Float,
    valueText: String,
    range: ClosedFloatingPointRange<Float>,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            fontSize = 11.sp
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = SliderTrackColor
            )
        )
    }
}
