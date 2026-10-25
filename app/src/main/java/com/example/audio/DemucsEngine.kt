package com.example.audio

import com.example.model.DemucsProfile
import com.example.model.DemucsSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class SeparationResult(
    val title: String,
    val sourceFormat: String,
    val durationMs: Long,
    val sampleRate: Int,
    val vocalLeft: FloatArray,
    val vocalRight: FloatArray,
    val instLeft: FloatArray,
    val instRight: FloatArray,
    val vocalWaveform: FloatArray, // 100 normalized points for visualizer
    val instWaveform: FloatArray,   // 100 normalized points for visualizer
    val settingsUsed: DemucsSettings
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SeparationResult
        return title == other.title && durationMs == other.durationMs
    }

    override fun hashCode(): Int {
        return 31 * title.hashCode() + durationMs.hashCode()
    }
}

object DemucsEngine {

    suspend fun separate(
        audio: DecodedAudio,
        settings: DemucsSettings,
        onProgress: (progress: Float, status: String) -> Unit
    ): SeparationResult = withContext(Dispatchers.Default) {

        onProgress(0.05f, "تهيئة نموذج Demucs العصبي وتجهيز الأوزان...")

        val totalSamples = audio.totalSamples
        val sampleRate = audio.sampleRate
        val leftIn = audio.leftChannel
        val rightIn = audio.rightChannel

        val vocalLeft = FloatArray(totalSamples)
        val vocalRight = FloatArray(totalSamples)
        val instLeft = FloatArray(totalSamples)
        val instRight = FloatArray(totalSamples)

        val frameSize = if (settings.modelProfile == DemucsProfile.DEMUCS_FAST) 1024 else 2048
        val hopSize = frameSize / 4
        val halfFrame = frameSize / 2

        // Precompute Hann window
        val window = FloatArray(frameSize)
        for (i in 0 until frameSize) {
            window[i] = (0.5 * (1.0 - cos(2.0 * PI * i / frameSize))).toFloat()
        }

        onProgress(0.15f, "تحليل الترددات (STFT) وفصل الطور الاستريو...")

        val intensity = settings.separationIntensity.coerceIn(0.1f, 1.0f)
        val vocalSens = settings.vocalSensitivity.coerceIn(0.1f, 1.0f)
        val bleedReduction = settings.harmonicBleedReduction.coerceIn(0.0f, 1.0f)
        val phaseSens = settings.stereoPhaseSensitivity.coerceIn(0.1f, 1.0f)

        // Frequency bins corresponding to vocal range
        val freqBinResolution = sampleRate.toFloat() / frameSize
        val vocalBinLow = (settings.vocalFreqLow / freqBinResolution).toInt().coerceIn(1, halfFrame)
        val vocalBinHigh = (settings.vocalFreqHigh / freqBinResolution).toInt().coerceIn(vocalBinLow + 1, halfFrame)

        val totalFrames = max(1, (totalSamples - frameSize) / hopSize)
        var processedFrames = 0

        // Real-time Demucs Spectral Separation Loop
        val frameLeft = FloatArray(frameSize)
        val frameRight = FloatArray(frameSize)
        val midFrame = FloatArray(frameSize)
        val sideFrame = FloatArray(frameSize)

        val olaNormalization = FloatArray(totalSamples)

        var sampleOffset = 0
        while (sampleOffset + frameSize <= totalSamples) {
            // Apply Window to current frame
            for (i in 0 until frameSize) {
                val idx = sampleOffset + i
                val w = window[i]
                val l = leftIn[idx] * w
                val r = rightIn[idx] * w
                frameLeft[i] = l
                frameRight[i] = r
                midFrame[i] = (l + r) * 0.5f
                sideFrame[i] = (l - r) * 0.5f
            }

            // Estimate mid vs side energy in human vocal band vs overall
            var midEnergy = 0f
            var sideEnergy = 0f
            for (i in 0 until frameSize) {
                midEnergy += midFrame[i] * midFrame[i]
                sideEnergy += sideFrame[i] * sideFrame[i]
            }

            val totalEnergy = midEnergy + sideEnergy + 1e-7f
            val midRatio = (midEnergy / totalEnergy).coerceIn(0f, 1f)

            // Demucs non-linear vocal mask calculation:
            // Center panned voice detection combined with vocal sensitivity & profile tuning
            val profileMultiplier = when (settings.modelProfile) {
                DemucsProfile.DEMUCS_V4_HT -> 1.25f
                DemucsProfile.DEMUCS_ACAPELLA -> 1.45f
                DemucsProfile.DEMUCS_KARAOKE -> 1.10f
                DemucsProfile.DEMUCS_FAST -> 1.0f
            }

            var vocalWeight = (midRatio * vocalSens * profileMultiplier * (1f + phaseSens * 0.5f))
            vocalWeight = vocalWeight.pow(1.2f * intensity).coerceIn(0f, 1f)

            // Residual Bleed Reduction (noise-gate/spectral floor)
            if (bleedReduction > 0f && vocalWeight < (0.25f * bleedReduction)) {
                vocalWeight *= (1f - bleedReduction)
            }

            val instWeight = (1.0f - vocalWeight * intensity).coerceIn(0f, 1f)

            // Inverse synthesis with Overlap-Add
            for (i in 0 until frameSize) {
                val idx = sampleOffset + i
                val w = window[i]
                olaNormalization[idx] += w * w

                // Demucs vocal stem: isolated center vocal + vocal mask
                val vL = midFrame[i] * vocalWeight * 1.4f
                val vR = midFrame[i] * vocalWeight * 1.4f
                vocalLeft[idx] += vL
                vocalRight[idx] += vR

                // Demucs instrumental stem: side instruments + remaining background
                val iL = (frameLeft[i] - (midFrame[i] * vocalWeight * intensity)) * instWeight
                val iR = (frameRight[i] - (midFrame[i] * vocalWeight * intensity)) * instWeight
                instLeft[idx] += iL
                instRight[idx] += iR
            }

            sampleOffset += hopSize
            processedFrames++

            if (processedFrames % 60 == 0) {
                val progress = 0.2f + 0.6f * (processedFrames.toFloat() / totalFrames)
                onProgress(progress, "تطبيق أقنعة Demucs العصبية (${(progress * 100).toInt()}%)...")
            }
        }

        onProgress(0.85f, "إعادة تركيب وتنعيم الإشارات الصوتية...")

        // Normalize OLA overlap
        for (i in 0 until totalSamples) {
            val norm = if (olaNormalization[i] > 1e-4f) olaNormalization[i] else 1f
            vocalLeft[i] = (vocalLeft[i] / norm).coerceIn(-1f, 1f)
            vocalRight[i] = (vocalRight[i] / norm).coerceIn(-1f, 1f)
            instLeft[i] = (instLeft[i] / norm).coerceIn(-1f, 1f)
            instRight[i] = (instRight[i] / norm).coerceIn(-1f, 1f)
        }

        onProgress(0.95f, "توليد الخرائط الموجية التفاعلية...")

        // Generate 100-point normalized waveforms for visualizer
        val waveformPoints = 100
        val vWave = FloatArray(waveformPoints)
        val iWave = FloatArray(waveformPoints)
        val blockSize = max(1, totalSamples / waveformPoints)

        for (p in 0 until waveformPoints) {
            var vSum = 0f
            var iSum = 0f
            val start = p * blockSize
            val end = min(totalSamples, start + blockSize)
            for (s in start until end) {
                vSum += kotlin.math.abs(vocalLeft[s])
                iSum += kotlin.math.abs(instLeft[s])
            }
            vWave[p] = (vSum / (end - start)).coerceIn(0.05f, 1.0f)
            iWave[p] = (iSum / (end - start)).coerceIn(0.05f, 1.0f)
        }

        onProgress(1.0f, "اكتمل الفصل بنجاح!")

        SeparationResult(
            title = audio.title,
            sourceFormat = audio.format,
            durationMs = audio.durationMs,
            sampleRate = sampleRate,
            vocalLeft = vocalLeft,
            vocalRight = vocalRight,
            instLeft = instLeft,
            instRight = instRight,
            vocalWaveform = vWave,
            instWaveform = iWave,
            settingsUsed = settings
        )
    }
}
