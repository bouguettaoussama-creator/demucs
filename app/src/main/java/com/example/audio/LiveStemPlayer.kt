package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
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
import kotlin.math.min

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0,
    val durationMs: Long = 0,
    val vocalVolume: Float = 1.0f,
    val instVolume: Float = 1.0f,
    val isVocalMuted: Boolean = false,
    val isInstMuted: Boolean = false,
    val isVocalSolo: Boolean = false,
    val isInstSolo: Boolean = false,
    val leftVU: Float = 0f,
    val rightVU: Float = 0f
)

class LiveStemPlayer {
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private var currentResult: SeparationResult? = null
    private var currentSampleIndex = 0

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    fun loadResult(result: SeparationResult) {
        stop()
        currentResult = result
        currentSampleIndex = 0
        _state.value = _state.value.copy(
            isPlaying = false,
            currentPositionMs = 0,
            durationMs = result.durationMs,
            leftVU = 0f,
            rightVU = 0f
        )
    }

    fun play() {
        val result = currentResult ?: return
        if (_state.value.isPlaying) return

        val sampleRate = result.sampleRate
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = max(minBufferSize, 8192)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack = track
        track.play()
        _state.value = _state.value.copy(isPlaying = true)

        playbackJob = scope.launch {
            val totalSamples = result.vocalLeft.size
            val chunkFrames = 2048
            val pcmChunk = ShortArray(chunkFrames * 2)

            while (isActive && _state.value.isPlaying && currentSampleIndex < totalSamples) {
                val s = _state.value
                val vocalVol = when {
                    s.isInstSolo -> 0f
                    s.isVocalSolo -> s.vocalVolume * 1.2f
                    s.isVocalMuted -> 0f
                    else -> s.vocalVolume
                }
                val instVol = when {
                    s.isVocalSolo -> 0f
                    s.isInstSolo -> s.instVolume * 1.2f
                    s.isInstMuted -> 0f
                    else -> s.instVolume
                }

                var chunkLMax = 0f
                var chunkRMax = 0f
                var framesWritten = 0

                for (f in 0 until chunkFrames) {
                    val sIdx = currentSampleIndex + f
                    if (sIdx >= totalSamples) break

                    val vL = result.vocalLeft[sIdx] * vocalVol
                    val vR = result.vocalRight[sIdx] * vocalVol
                    val iL = result.instLeft[sIdx] * instVol
                    val iR = result.instRight[sIdx] * instVol

                    val mixedL = (vL + iL).coerceIn(-1f, 1f)
                    val mixedR = (vR + iR).coerceIn(-1f, 1f)

                    pcmChunk[f * 2] = (mixedL * 32767f).toInt().toShort()
                    pcmChunk[f * 2 + 1] = (mixedR * 32767f).toInt().toShort()

                    chunkLMax = max(chunkLMax, abs(mixedL))
                    chunkRMax = max(chunkRMax, abs(mixedR))
                    framesWritten++
                }

                if (framesWritten > 0) {
                    track.write(pcmChunk, 0, framesWritten * 2)
                    currentSampleIndex += framesWritten
                    val curPosMs = (currentSampleIndex.toLong() * 1000L) / sampleRate

                    _state.value = _state.value.copy(
                        currentPositionMs = curPosMs,
                        leftVU = chunkLMax,
                        rightVU = chunkRMax
                    )
                }

                if (currentSampleIndex >= totalSamples) {
                    // Loop or stop
                    currentSampleIndex = 0
                }
            }

            _state.value = _state.value.copy(isPlaying = false, leftVU = 0f, rightVU = 0f)
        }
    }

    fun pause() {
        _state.value = _state.value.copy(isPlaying = false, leftVU = 0f, rightVU = 0f)
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (_: Exception) {}
    }

    fun stop() {
        pause()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        currentSampleIndex = 0
        _state.value = _state.value.copy(currentPositionMs = 0, leftVU = 0f, rightVU = 0f)
    }

    fun seekTo(positionMs: Long) {
        val result = currentResult ?: return
        val sampleRate = result.sampleRate
        val totalSamples = result.vocalLeft.size
        val targetSample = ((positionMs * sampleRate) / 1000L).toInt().coerceIn(0, totalSamples - 1)
        currentSampleIndex = targetSample
        _state.value = _state.value.copy(currentPositionMs = positionMs)
    }

    fun setVocalVolume(volume: Float) {
        _state.value = _state.value.copy(vocalVolume = volume.coerceIn(0f, 2f))
    }

    fun setInstVolume(volume: Float) {
        _state.value = _state.value.copy(instVolume = volume.coerceIn(0f, 2f))
    }

    fun toggleVocalMute() {
        val cur = _state.value.isVocalMuted
        _state.value = _state.value.copy(isVocalMuted = !cur, isVocalSolo = false)
    }

    fun toggleInstMute() {
        val cur = _state.value.isInstMuted
        _state.value = _state.value.copy(isInstMuted = !cur, isInstSolo = false)
    }

    fun toggleVocalSolo() {
        val cur = _state.value.isVocalSolo
        _state.value = _state.value.copy(
            isVocalSolo = !cur,
            isInstSolo = false,
            isVocalMuted = false
        )
    }

    fun toggleInstSolo() {
        val cur = _state.value.isInstSolo
        _state.value = _state.value.copy(
            isInstSolo = !cur,
            isVocalSolo = false,
            isInstMuted = false
        )
    }

    fun release() {
        stop()
    }
}
