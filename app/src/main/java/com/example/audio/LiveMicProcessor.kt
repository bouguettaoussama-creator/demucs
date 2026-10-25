package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin

data class LiveMicState(
    val isRunning: Boolean = false,
    val isMonitoringAudio: Boolean = false,
    val inputLevel: Float = 0f,
    val vocalLevel: Float = 0f,
    val noiseReduction: Float = 0.75f,
    val vocalBoost: Float = 1.2f,
    val detectedPitchHz: Float = 0f,
    val recentWaveform: FloatArray = FloatArray(40) { 0f }
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LiveMicState
        return isRunning == other.isRunning && isMonitoringAudio == other.isMonitoringAudio
    }

    override fun hashCode(): Int {
        var result = isRunning.hashCode()
        result = 31 * result + isMonitoringAudio.hashCode()
        return result
    }
}

class LiveMicProcessor {
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var processJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _state = MutableStateFlow(LiveMicState())
    val state: StateFlow<LiveMicState> = _state.asStateFlow()

    @SuppressLint("MissingPermission")
    fun start(monitorOutput: Boolean = false) {
        if (_state.value.isRunning) return

        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val bufferSize = max(minBufSize, 4096)

        try {
            val record = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                // Try standard mic
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )
            } else {
                audioRecord = record
            }

            audioRecord?.startRecording()

            if (monitorOutput) {
                val trackBufSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, audioFormat)
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(audioFormat)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(max(trackBufSize, 4096))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                audioTrack?.play()
            }

            _state.value = _state.value.copy(
                isRunning = true,
                isMonitoringAudio = monitorOutput
            )

            processJob = scope.launch {
                val readBuffer = ShortArray(1024)
                val outBuffer = ShortArray(1024)
                val waveformHistory = FloatArray(40) { 0f }
                var waveIdx = 0

                // Real-time Bandpass Filter coefficients (Voice Formant 150Hz - 3400Hz)
                var prevInput = 0f
                var prevOutput = 0f

                while (isActive && _state.value.isRunning) {
                    val readSamples = audioRecord?.read(readBuffer, 0, readBuffer.size) ?: 0
                    if (readSamples > 0) {
                        var maxInput = 0f
                        var maxVocal = 0f

                        val boost = _state.value.vocalBoost
                        val noiseRed = _state.value.noiseReduction

                        for (i in 0 until readSamples) {
                            val inVal = readBuffer[i].toFloat() / 32768f
                            maxInput = max(maxInput, abs(inVal))

                            // High-pass DC filter + Voice Formant bandpass
                            val filtered = inVal - 0.95f * prevInput + 0.93f * prevOutput
                            prevInput = inVal
                            prevOutput = filtered

                            // Soft Demucs noise gate
                            val gateThreshold = 0.05f * noiseRed
                            val vocalSample = if (abs(filtered) > gateThreshold) {
                                (filtered * boost).coerceIn(-1f, 1f)
                            } else {
                                (filtered * (1f - noiseRed)).coerceIn(-1f, 1f)
                            }

                            maxVocal = max(maxVocal, abs(vocalSample))
                            outBuffer[i] = (vocalSample * 32767f).toInt().toShort()
                        }

                        if (monitorOutput) {
                            audioTrack?.write(outBuffer, 0, readSamples)
                        }

                        waveformHistory[waveIdx % 40] = maxVocal
                        waveIdx++

                        _state.value = _state.value.copy(
                            inputLevel = maxInput,
                            vocalLevel = maxVocal,
                            recentWaveform = waveformHistory.clone()
                        )
                    }
                }
            }
        } catch (_: Exception) {
            stop()
        }
    }

    fun setNoiseReduction(value: Float) {
        _state.value = _state.value.copy(noiseReduction = value.coerceIn(0f, 1f))
    }

    fun setVocalBoost(value: Float) {
        _state.value = _state.value.copy(vocalBoost = value.coerceIn(0.5f, 2.5f))
    }

    fun stop() {
        _state.value = _state.value.copy(isRunning = false, inputLevel = 0f, vocalLevel = 0f)
        processJob?.cancel()
        processJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null

        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
