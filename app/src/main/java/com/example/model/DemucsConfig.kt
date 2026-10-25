package com.example.model

enum class DemucsProfile(val title: String, val subtitle: String) {
    DEMUCS_V4_HT("Demucs v4 Hybrid (HT)", "أعلى جودة واستخلاص نقي للترددات العالية والمنخفضة"),
    DEMUCS_FAST("Demucs Fast (سريع)", "معالجة خفيفة وفائقة السرعة مناسبة لجميع الأجهزة"),
    DEMUCS_ACAPELLA("Demucs Acapella Focus", "تركيز كامل على استخراج الصوت البشري والتخلص من الآلات"),
    DEMUCS_KARAOKE("Demucs Karaoke Pure", "تفريغ الصوت البشري تماماً وترك الآلات الموسيقية نقية")
}

enum class DemucsPreset(val title: String, val description: String) {
    BALANCED("متوازن (الافتراضي)", "فصل متكافئ وعالي الدقة بين الموسيقى والغناء"),
    PURE_KARAOKE("كاريوكي نقي", "أقصى عزل للموسيقى مع إزالة كاملة للكلام والغناء"),
    ACAPELLA_STUDIO("عزل صوتي فائق", "استخراج الصوت البشري النقي بدون تسريب من الآلات"),
    SPEECH_PODCAST("بودكاست ومحادثات", "تنقية الكلام وإزالة الضوضاء والموسيقى الخلفية"),
    CUSTOM("مخصص", "ضبط يدوي لكافة معلمات النموذج")
}

data class DemucsSettings(
    val separationIntensity: Float = 0.75f, // 0.0f .. 1.0f
    val vocalSensitivity: Float = 0.80f,   // 0.0f .. 1.0f
    val harmonicBleedReduction: Float = 0.65f, // 0.0f .. 1.0f
    val stereoPhaseSensitivity: Float = 0.70f, // 0.0f .. 1.0f
    val vocalFreqLow: Int = 120, // Hz
    val vocalFreqHigh: Int = 7200, // Hz
    val modelProfile: DemucsProfile = DemucsProfile.DEMUCS_V4_HT,
    val mp3BitrateKbps: Int = 256, // 128, 192, 256, 320
    val activePreset: DemucsPreset = DemucsPreset.BALANCED
) {
    companion object {
        val DEFAULT = DemucsSettings()

        fun fromPreset(preset: DemucsPreset): DemucsSettings {
            return when (preset) {
                DemucsPreset.BALANCED -> DemucsSettings(
                    separationIntensity = 0.75f,
                    vocalSensitivity = 0.80f,
                    harmonicBleedReduction = 0.65f,
                    stereoPhaseSensitivity = 0.70f,
                    vocalFreqLow = 120,
                    vocalFreqHigh = 7200,
                    modelProfile = DemucsProfile.DEMUCS_V4_HT,
                    mp3BitrateKbps = 256,
                    activePreset = DemucsPreset.BALANCED
                )
                DemucsPreset.PURE_KARAOKE -> DemucsSettings(
                    separationIntensity = 0.95f,
                    vocalSensitivity = 0.90f,
                    harmonicBleedReduction = 0.85f,
                    stereoPhaseSensitivity = 0.85f,
                    vocalFreqLow = 100,
                    vocalFreqHigh = 8000,
                    modelProfile = DemucsProfile.DEMUCS_KARAOKE,
                    mp3BitrateKbps = 320,
                    activePreset = DemucsPreset.PURE_KARAOKE
                )
                DemucsPreset.ACAPELLA_STUDIO -> DemucsSettings(
                    separationIntensity = 0.90f,
                    vocalSensitivity = 0.95f,
                    harmonicBleedReduction = 0.80f,
                    stereoPhaseSensitivity = 0.75f,
                    vocalFreqLow = 110,
                    vocalFreqHigh = 7500,
                    modelProfile = DemucsProfile.DEMUCS_ACAPELLA,
                    mp3BitrateKbps = 320,
                    activePreset = DemucsPreset.ACAPELLA_STUDIO
                )
                DemucsPreset.SPEECH_PODCAST -> DemucsSettings(
                    separationIntensity = 0.85f,
                    vocalSensitivity = 0.85f,
                    harmonicBleedReduction = 0.75f,
                    stereoPhaseSensitivity = 0.60f,
                    vocalFreqLow = 140,
                    vocalFreqHigh = 6500,
                    modelProfile = DemucsProfile.DEMUCS_FAST,
                    mp3BitrateKbps = 192,
                    activePreset = DemucsPreset.SPEECH_PODCAST
                )
                DemucsPreset.CUSTOM -> DemucsSettings(
                    activePreset = DemucsPreset.CUSTOM
                )
            }
        }
    }
}
