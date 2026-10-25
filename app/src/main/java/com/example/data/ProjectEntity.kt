package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "separated_projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val originalFileName: String,
    val fileFormat: String, // "MP3", "WAV", "FLAC"
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val vocalMp3Path: String,
    val instrumentalMp3Path: String,
    val originalAudioPath: String,
    val separationIntensity: Float,
    val modelProfileName: String,
    val activePresetName: String
)
