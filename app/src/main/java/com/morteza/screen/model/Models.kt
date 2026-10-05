package com.morteza.screen.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class VideoItem(
    val id: String,
    val name: String,
    val path: String,
    val durationSeconds: Long,
    val sizeBytes: Long,
    val resolution: String,
    val timestamp: Long
)

data class VideoConfig(
    val resolution: String = "1920x1080",
    val width: Int = 1920,
    val height: Int = 1080,
    val bitrate: Int = 4000000,
    val framerate: Int = 30,
    val iframeInterval: Int = 10,
    val orientation: String = "Landscape"
)

data class AudioConfig(
    val sampleRate: Int = 44100,
    val channelCount: Int = 1,
    val includeMic: Boolean = true
)

enum class RecordingStatus {
    IDLE,
    COUNTDOWN,
    RECORDING,
    PAUSED
}

enum class AppScreen(val title: String) {
    HOME("Screen"),
    GALLERY("Gallery"),
    SLIDESHOW("Slideshow"),
    TOOLS("Tools"),
    PAINTER("Painter"),
    SETTINGS("Settings"),
    SHARE("Share"),
    SEND("Send")
}

data class DrawPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val isHighlighter: Boolean = false,
    val isEraser: Boolean = false
)
