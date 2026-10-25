package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DemoTrackType
import com.example.model.DemucsPreset
import com.example.ui.components.DualWaveformVisualizer
import com.example.ui.components.VuMeterBar
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRose
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonVioletDark
import com.example.ui.theme.SliderTrackColor
import com.example.ui.theme.StemInstrumentalColor
import com.example.ui.theme.StemVocalsColor
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ExportState
import com.example.viewmodel.MainViewModel

@Composable
fun SeparateScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedAudio by viewModel.selectedAudio.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val processingProgress by viewModel.processingProgress.collectAsState()
    val processingStatus by viewModel.processingStatus.collectAsState()
    val currentResult by viewModel.currentResult.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val playerState by viewModel.player.state.collectAsState()
    val exportState by viewModel.exportState.collectAsState()

    // Audio picker for WAV, FLAC, MP3
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = queryFileName(context, it) ?: "audio_track.mp3"
            viewModel.loadAudioFromUri(context, it, fileName)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Hero Title Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCard)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1E1B4B).copy(alpha = 0.8f),
                                    Color(0xFF0F172A).copy(alpha = 0.9f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(listOf(NeonViolet, NeonCyan))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Audiotrack,
                                        contentDescription = "Demucs Audio",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "فصل الصوت والكلام (Demucs)",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "محرك ذكاء اصطناعي محلي 100% بدون إنترنت",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeonCyan
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Supported Format Badges
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FormatBadge("MP3", NeonCyan)
                            FormatBadge("WAV", NeonViolet)
                            FormatBadge("FLAC", NeonEmerald)
                            FormatBadge("حفظ كـ MP3", NeonRose)
                        }
                    }
                }
            }
        }

        // Import Section Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("import_audio_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "اختر الملف الصوتي للمُعالجة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "يدعم استيراد صيغ MP3 و WAV و FLAC بجودة أصلية",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("pick_audio_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "استيراد",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "استيراد ملف صوتي (WAV / FLAC / MP3)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "أو اختر عينة تجريبية فورية:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DemoTrackChip(
                            title = "بوب شرقي",
                            format = "MP3",
                            isSelected = selectedAudio?.format == "MP3",
                            onClick = { viewModel.loadDemoTrack(DemoTrackType.ORIENTAL_POP) },
                            modifier = Modifier.weight(1f)
                        )
                        DemoTrackChip(
                            title = "جيتار أكوستيك",
                            format = "WAV",
                            isSelected = selectedAudio?.format == "WAV",
                            onClick = { viewModel.loadDemoTrack(DemoTrackType.ACOUSTIC_BALLAD) },
                            modifier = Modifier.weight(1f)
                        )
                        DemoTrackChip(
                            title = "بودكاست",
                            format = "FLAC",
                            isSelected = selectedAudio?.format == "FLAC",
                            onClick = { viewModel.loadDemoTrack(DemoTrackType.PODCAST_LOFI) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    selectedAudio?.let { audio ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StudioCardElevated)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Audiotrack,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = audio.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "الصيغة: ${audio.format} | المدة: ${(audio.durationMs / 1000)} ثانية | ${audio.sampleRate} Hz",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                FormatBadge(audio.format, NeonEmerald)
                            }
                        }
                    }
                }
            }
        }

        // Demucs Model Settings & Separation Controls
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("demucs_controls_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "معلمات نموذج Demucs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = settings.modelProfile.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonViolet
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Chips
                    Text(
                        text = "النمط المسبق المختار:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DemucsPreset.values().take(4).forEach { preset ->
                            FilterChip(
                                selected = settings.activePreset == preset,
                                onClick = { viewModel.setPreset(preset) },
                                label = { Text(preset.title.substringBefore(" ("), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonVioletDark,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Separation Intensity Slider (مستوى فصل الآلات الموسيقية عن الصوت أو الغناء)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مستوى فصل الآلات الموسيقية عن الغناء:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "${(settings.separationIntensity * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }

                    Slider(
                        value = settings.separationIntensity,
                        onValueChange = { viewModel.setSeparationIntensity(it) },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = SliderTrackColor
                        ),
                        modifier = Modifier.testTag("separation_intensity_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("عزل ناعم (طبيعي)", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text("عزل فائق (أقصى تجريد)", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big Separation CTA Button
                    Button(
                        onClick = { viewModel.startSeparation(context) },
                        enabled = !isProcessing && selectedAudio != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("start_separation_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        if (isProcessing) listOf(Color.Gray, Color.DarkGray)
                                        else listOf(NeonViolet, NeonCyan)
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isProcessing) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "جاري الفصل بنموذج Demucs...",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Audiotrack,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ابدأ فصل الصوت عن الموسيقى (Demucs)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = isProcessing) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            LinearProgressIndicator(
                                progress = { processingProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = NeonCyan,
                                trackColor = SliderTrackColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = processingStatus,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeonCyan
                                )
                                Text(
                                    text = "${(processingProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Results & Live Stem Mixer Card (when separation completed)
        currentResult?.let { result ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("results_mixer_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تم الفصل بنجاح: ${result.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            FormatBadge("MP3 جاهز", NeonRose)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dual Waveform Visualizer
                        val currentProgress = if (result.durationMs > 0) {
                            (playerState.currentPositionMs.toFloat() / result.durationMs).coerceIn(0f, 1f)
                        } else 0f

                        DualWaveformVisualizer(
                            vocalWaveform = result.vocalWaveform,
                            instWaveform = result.instWaveform,
                            currentProgress = currentProgress,
                            isPlaying = playerState.isPlaying,
                            onSeek = { ratio ->
                                viewModel.player.seekTo((ratio * result.durationMs).toLong())
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // VU meter
                        VuMeterBar(
                            leftLevel = playerState.leftVU,
                            rightLevel = playerState.rightVU
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Play / Pause and Position Seek Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatDuration(playerState.currentPositionMs),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )

                            IconButton(
                                onClick = {
                                    if (playerState.isPlaying) viewModel.player.pause()
                                    else viewModel.player.play()
                                },
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(NeonCyan, NeonViolet))
                                    )
                                    .testTag("play_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playerState.isPlaying) "إيقاف مؤقت" else "تشغيل",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Text(
                                text = formatDuration(result.durationMs),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stem 1: Vocals Fader (صوت الغناء)
                        StemFaderCard(
                            stemName = "صوت الغناء / الكلام (Vocals)",
                            icon = Icons.Default.Mic,
                            color = StemVocalsColor,
                            volume = playerState.vocalVolume,
                            isMuted = playerState.isVocalMuted,
                            isSolo = playerState.isVocalSolo,
                            onVolumeChange = { viewModel.player.setVocalVolume(it) },
                            onToggleMute = { viewModel.player.toggleVocalMute() },
                            onToggleSolo = { viewModel.player.toggleVocalSolo() }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stem 2: Instrumental Fader (الموسيقى والآلات)
                        StemFaderCard(
                            stemName = "الموسيقى والآلات (Accompaniment)",
                            icon = Icons.Default.MusicNote,
                            color = StemInstrumentalColor,
                            volume = playerState.instVolume,
                            isMuted = playerState.isInstMuted,
                            isSolo = playerState.isInstSolo,
                            onVolumeChange = { viewModel.player.setInstVolume(it) },
                            onToggleMute = { viewModel.player.toggleInstMute() },
                            onToggleSolo = { viewModel.player.toggleInstSolo() }
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // MP3 Export Buttons
                        Text(
                            text = "تحويل وحفظ المخرجات بصيغة MP3:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.exportStem(context, "الغناء") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("export_vocals_mp3_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = StemVocalsColor),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("صوت الغناء .MP3", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }

                            Button(
                                onClick = { viewModel.exportStem(context, "الموسيقى") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("export_music_mp3_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = StemInstrumentalColor),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("الموسيقى كاريوكي .MP3", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { viewModel.exportCustomMix(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("export_custom_mix_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = NeonViolet)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير المزيج المخصص بالمستويات الحالية بصيغة MP3", color = NeonViolet, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Export State Feedback Alert
        if (exportState !is ExportState.Idle) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (exportState) {
                            is ExportState.Success -> Color(0xFF064E3B)
                            is ExportState.Error -> Color(0xFF881337)
                            is ExportState.Exporting -> Color(0xFF1E1B4B)
                            else -> StudioCard
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        when (val state = exportState) {
                            is ExportState.Exporting -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(state.progressText, color = Color.White, fontSize = 13.sp)
                                }
                            }
                            is ExportState.Success -> {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("تم التصدير بنجاح!", fontWeight = FontWeight.Bold, color = NeonEmerald)
                                    Text(state.message, color = Color.White, fontSize = 12.sp)
                                }
                                IconButton(
                                    onClick = { viewModel.shareFile(context, state.file) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(NeonEmerald)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = Color.Black, modifier = Modifier.size(18.dp))
                                }
                            }
                            is ExportState.Error -> {
                                Text(state.error, color = Color.White, fontSize = 13.sp)
                            }
                            else -> {}
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StemFaderCard(
    stemName: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    volume: Float,
    isMuted: Boolean,
    isSolo: Boolean,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onToggleSolo: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F121E))
            .border(
                1.dp,
                if (isSolo) color else Color(0xFF242A44),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stemName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isMuted) TextMuted else TextPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Mute Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isMuted) NeonRose else Color(0xFF1E293B))
                            .clickable { onToggleMute() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isMuted) "مكتوم" else "كتم",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMuted) Color.White else TextSecondary
                        )
                    }

                    // Solo Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSolo) NeonAmber else Color(0xFF1E293B))
                            .clickable { onToggleSolo() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "منفرد (Solo)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSolo) Color.Black else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (volume == 0f || isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = if (isMuted) TextMuted else color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = if (isMuted) 0f else volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = color,
                        activeTrackColor = color,
                        inactiveTrackColor = SliderTrackColor
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(volume * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
    }
}

@Composable
fun FormatBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text = label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DemoTrackChip(
    title: String,
    format: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) NeonVioletDark else StudioCardElevated)
            .border(
                1.dp,
                if (isSelected) NeonViolet else Color(0xFF242A44),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else TextPrimary
            )
            Text(
                text = format,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) NeonCyan else TextMuted
            )
        }
    }
}

fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun queryFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = cursor.getString(index)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}
