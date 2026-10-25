package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioDecoder
import com.example.audio.DecodedAudio
import com.example.audio.DemoTrackType
import com.example.audio.DemucsEngine
import com.example.audio.LiveMicProcessor
import com.example.audio.LiveStemPlayer
import com.example.audio.Mp3Encoder
import com.example.audio.SeparationResult
import com.example.data.AppDatabase
import com.example.data.ProjectEntity
import com.example.model.DemucsPreset
import com.example.model.DemucsProfile
import com.example.model.DemucsSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed interface ExportState {
    object Idle : ExportState
    data class Exporting(val progressText: String) : ExportState
    data class Success(val message: String, val file: File) : ExportState
    data class Error(val error: String) : ExportState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val projectDao = db.projectDao()

    val projects = projectDao.getAllProjects().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Demucs Model Settings
    private val _settings = MutableStateFlow(DemucsSettings.DEFAULT)
    val settings: StateFlow<DemucsSettings> = _settings.asStateFlow()

    // Separation & Audio State
    private val _selectedAudio = MutableStateFlow<DecodedAudio?>(null)
    val selectedAudio: StateFlow<DecodedAudio?> = _selectedAudio.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _processingProgress = MutableStateFlow(0f)
    val processingProgress: StateFlow<Float> = _processingProgress.asStateFlow()

    private val _processingStatus = MutableStateFlow("")
    val processingStatus: StateFlow<String> = _processingStatus.asStateFlow()

    private val _currentResult = MutableStateFlow<SeparationResult?>(null)
    val currentResult: StateFlow<SeparationResult?> = _currentResult.asStateFlow()

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    // Live Audio Components
    val player = LiveStemPlayer()
    val liveMic = LiveMicProcessor()

    init {
        // Automatically preload the default Oriental Pop demo track so user can test immediately!
        loadDemoTrack(DemoTrackType.ORIENTAL_POP)
    }

    fun loadDemoTrack(demoType: DemoTrackType) {
        viewModelScope.launch {
            val audio = AudioDecoder.generateDemoTrack(demoType)
            _selectedAudio.value = audio
            _currentResult.value = null
            player.stop()
        }
    }

    fun loadAudioFromUri(context: Context, uri: Uri, originalName: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            _processingStatus.value = "جاري قراءة وفك تشفير الملف الصوتي..."
            _processingProgress.value = 0.05f
            try {
                val decoded = AudioDecoder.decodeFromUri(context, uri, originalName)
                _selectedAudio.value = decoded
                _currentResult.value = null
                player.stop()
            } catch (e: Exception) {
                _exportState.value = ExportState.Error("تعذر قراءة الملف: ${e.localizedMessage}")
            } finally {
                _isProcessing.value = false
                _processingProgress.value = 0f
            }
        }
    }

    fun startSeparation(context: Context) {
        val audio = _selectedAudio.value ?: return
        if (_isProcessing.value) return

        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val result = DemucsEngine.separate(
                    audio = audio,
                    settings = _settings.value
                ) { progress, status ->
                    _processingProgress.value = progress
                    _processingStatus.value = status
                }

                _currentResult.value = result
                player.loadResult(result)

                // Export initial MP3 stems and save project to local Room database
                _processingStatus.value = "توليد ملفات MP3 للمسارات وحفظ المشروع..."
                _processingProgress.value = 0.98f

                val vocalMp3 = Mp3Encoder.encodeToMp3File(
                    context = context,
                    fileName = "${audio.title}_Vocals.mp3",
                    leftChannel = result.vocalLeft,
                    rightChannel = result.vocalRight,
                    sampleRate = result.sampleRate,
                    bitrateKbps = _settings.value.mp3BitrateKbps,
                    trackTitle = audio.title,
                    stemType = "صوت الغناء"
                )

                val instMp3 = Mp3Encoder.encodeToMp3File(
                    context = context,
                    fileName = "${audio.title}_Instrumental.mp3",
                    leftChannel = result.instLeft,
                    rightChannel = result.instRight,
                    sampleRate = result.sampleRate,
                    bitrateKbps = _settings.value.mp3BitrateKbps,
                    trackTitle = audio.title,
                    stemType = "الموسيقى والآلات"
                )

                projectDao.insertProject(
                    ProjectEntity(
                        title = audio.title,
                        originalFileName = "${audio.title}.${audio.format.lowercase()}",
                        fileFormat = audio.format,
                        durationMs = audio.durationMs,
                        vocalMp3Path = vocalMp3.absolutePath,
                        instrumentalMp3Path = instMp3.absolutePath,
                        originalAudioPath = "",
                        separationIntensity = _settings.value.separationIntensity,
                        modelProfileName = _settings.value.modelProfile.title,
                        activePresetName = _settings.value.activePreset.title
                    )
                )

                _processingProgress.value = 1f
                _processingStatus.value = "اكتمل الفصل بنجاح!"
            } catch (e: Exception) {
                _exportState.value = ExportState.Error("خطأ أثناء المعالجة: ${e.localizedMessage}")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun exportStem(context: Context, stemType: String) {
        val result = _currentResult.value ?: return
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting("جاري تصدير $stemType بصيغة MP3...")
            try {
                val left = if (stemType == "الغناء") result.vocalLeft else result.instLeft
                val right = if (stemType == "الغناء") result.vocalRight else result.instRight
                val suffix = if (stemType == "الغناء") "Vocals" else "Instrumental"

                val file = Mp3Encoder.encodeToMp3File(
                    context = context,
                    fileName = "${result.title}_$suffix.mp3",
                    leftChannel = left,
                    rightChannel = right,
                    sampleRate = result.sampleRate,
                    bitrateKbps = _settings.value.mp3BitrateKbps,
                    trackTitle = result.title,
                    stemType = stemType
                )

                _exportState.value = ExportState.Success(
                    message = "تم تصدير $stemType بنجاح بصيغة MP3 بحجم ${(file.length() / 1024)} كيلوبايت",
                    file = file
                )
            } catch (e: Exception) {
                _exportState.value = ExportState.Error("فشل التصدير: ${e.localizedMessage}")
            }
        }
    }

    fun exportCustomMix(context: Context) {
        val result = _currentResult.value ?: return
        val playerState = player.state.value

        viewModelScope.launch {
            _exportState.value = ExportState.Exporting("جاري تصدير المزيج المخصص بصيغة MP3...")
            try {
                val total = result.vocalLeft.size
                val mixedL = FloatArray(total)
                val mixedR = FloatArray(total)
                val vVol = if (playerState.isVocalMuted) 0f else playerState.vocalVolume
                val iVol = if (playerState.isInstMuted) 0f else playerState.instVolume

                for (i in 0 until total) {
                    mixedL[i] = (result.vocalLeft[i] * vVol + result.instLeft[i] * iVol).coerceIn(-1f, 1f)
                    mixedR[i] = (result.vocalRight[i] * vVol + result.instRight[i] * iVol).coerceIn(-1f, 1f)
                }

                val file = Mp3Encoder.encodeToMp3File(
                    context = context,
                    fileName = "${result.title}_CustomMix.mp3",
                    leftChannel = mixedL,
                    rightChannel = mixedR,
                    sampleRate = result.sampleRate,
                    bitrateKbps = _settings.value.mp3BitrateKbps,
                    trackTitle = result.title,
                    stemType = "Custom Mix"
                )

                _exportState.value = ExportState.Success(
                    message = "تم تصدير المزيج المخصص بنجاح بصيغة MP3",
                    file = file
                )
            } catch (e: Exception) {
                _exportState.value = ExportState.Error("فشل تصدير المزيج: ${e.localizedMessage}")
            }
        }
    }

    fun shareFile(context: Context, file: File) {
        try {
            val uri: Uri = try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (_: Exception) {
                Uri.fromFile(file)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/mpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                putExtra(Intent.EXTRA_TEXT, "تم فصل هذا الملف الصوتي بواسطة Demucs Audio")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "مشاركة الملف الصوتي")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            _exportState.value = ExportState.Error("تعذر فتح شاشة المشاركة: ${e.localizedMessage}")
        }
    }

    fun dismissExportState() {
        _exportState.value = ExportState.Idle
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            projectDao.deleteProject(project)
            try { File(project.vocalMp3Path).delete() } catch (_: Exception) {}
            try { File(project.instrumentalMp3Path).delete() } catch (_: Exception) {}
        }
    }

    // Demucs Settings Mutations
    fun setPreset(preset: DemucsPreset) {
        _settings.value = DemucsSettings.fromPreset(preset)
    }

    fun setSeparationIntensity(value: Float) {
        _settings.value = _settings.value.copy(
            separationIntensity = value,
            activePreset = DemucsPreset.CUSTOM
        )
    }

    fun setVocalSensitivity(value: Float) {
        _settings.value = _settings.value.copy(
            vocalSensitivity = value,
            activePreset = DemucsPreset.CUSTOM
        )
    }

    fun setHarmonicBleedReduction(value: Float) {
        _settings.value = _settings.value.copy(
            harmonicBleedReduction = value,
            activePreset = DemucsPreset.CUSTOM
        )
    }

    fun setStereoPhaseSensitivity(value: Float) {
        _settings.value = _settings.value.copy(
            stereoPhaseSensitivity = value,
            activePreset = DemucsPreset.CUSTOM
        )
    }

    fun setModelProfile(profile: DemucsProfile) {
        _settings.value = _settings.value.copy(
            modelProfile = profile,
            activePreset = DemucsPreset.CUSTOM
        )
    }

    fun setMp3Bitrate(bitrate: Int) {
        _settings.value = _settings.value.copy(mp3BitrateKbps = bitrate)
    }

    fun setVocalFrequencyRange(low: Int, high: Int) {
        _settings.value = _settings.value.copy(
            vocalFreqLow = low,
            vocalFreqHigh = high,
            activePreset = DemucsPreset.CUSTOM
        )
    }

    fun resetSettingsToDefault() {
        _settings.value = DemucsSettings.DEFAULT
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
        liveMic.stop()
    }
}
