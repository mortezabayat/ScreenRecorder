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
    val timestamp: Long,
    val isStarred: Boolean = false
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

enum class AudioSourceOption(val title: String, val shortLabel: String, val description: String) {
    MIC("Microphone (Mic)", "Mic", "Records microphone and surroundings"),
    INTERNAL("Internal Audio", "Internal", "Records system, games, and media sound directly (Android 10+)"),
    MIC_AND_INTERNAL("Mic + Internal Audio", "Mic + Internal", "Records commentary & system audio simultaneously"),
    MUTE("Mute (No Audio)", "Muted", "Captures silent video without audio track")
}

data class AudioConfig(
    val sampleRate: Int = 44100,
    val channelCount: Int = 2,
    val audioSource: AudioSourceOption = AudioSourceOption.MIC,
    val includeMic: Boolean = true,
    val internalAudioVolume: Float = 1.0f,
    val micAudioVolume: Float = 1.0f
)

enum class RecordingStatus {
    IDLE,
    COUNTDOWN,
    RECORDING,
    PAUSED
}

enum class AppThemeMode(val title: String) {
    DARK("Dark Mode"),
    LIGHT("Light Mode"),
    SYSTEM("System Default")
}

data class StorageFolderConfig(
    val displayName: String = "Movies/ScreenRecorder",
    val path: String = "/storage/emulated/0/Movies/ScreenRecorder",
    val uriString: String? = null,
    val organizeByDate: Boolean = false
)

data class RecordingLimitsConfig(
    val durationLimitEnabled: Boolean = false,
    val maxDurationMinutes: Int = 10,
    val fileSizeLimitEnabled: Boolean = false,
    val maxFileSizeMB: Int = 500,
    val warnBeforeSeconds: Int = 15,
    val autoStopAction: String = "Stop and Save"
)

data class AutoCleanupConfig(
    val autoDeleteEnabled: Boolean = false,
    val retentionDays: Int = 30,
    val protectStarredVideos: Boolean = true
)

data class BatterySaverConfig(
    val isEnabled: Boolean = true,
    val thresholdPercent: Int = 15,
    val targetResolutionPreset: String = "480p",
    val targetFramerate: Int = 30,
    val autoStopAtCritical: Boolean = true,
    val criticalThresholdPercent: Int = 5,
    val isBatterySaverActive: Boolean = false
)

data class BatteryState(
    val levelPercent: Int = 82,
    val isCharging: Boolean = false,
    val isLowBattery: Boolean = false,
    val isSimulated: Boolean = false
)

enum class AppScreen(val title: String) {
    HOME("Screen"),
    GALLERY("Gallery"),
    STATISTICS("Statistics"),
    SLIDESHOW("Slideshow"),
    TOOLS("Tools"),
    LIMITS("Recording Limits"),
    SCHEDULE("Schedule Recording"),
    PAINTER("Painter"),
    SETTINGS("Settings"),
    SHARE("Share"),
    SEND("Send")
}

data class ScheduledRecording(
    val id: String,
    val title: String,
    val targetEpochMillis: Long,
    val maxDurationMinutes: Int = 15,
    val qualityPreset: String = "1080p",
    val audioEnabled: Boolean = true,
    val isEnabled: Boolean = true,
    val status: ScheduledStatus = ScheduledStatus.PENDING
)

enum class ScheduledStatus(val label: String) {
    PENDING("Pending"),
    ARMED("Armed"),
    TRIGGERED("Triggered"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class DrawPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val isHighlighter: Boolean = false,
    val isEraser: Boolean = false
)
