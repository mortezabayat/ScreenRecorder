package com.morteza.screen.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morteza.screen.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScreenRecorderViewModel : ViewModel() {

    private val _recordingStatus = MutableStateFlow(RecordingStatus.IDLE)
    val recordingStatus: StateFlow<RecordingStatus> = _recordingStatus.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _countdownValue = MutableStateFlow<Int?>(null)
    val countdownValue: StateFlow<Int?> = _countdownValue.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _videoConfig = MutableStateFlow(VideoConfig())
    val videoConfig: StateFlow<VideoConfig> = _videoConfig.asStateFlow()

    private val _audioConfig = MutableStateFlow(AudioConfig())
    val audioConfig: StateFlow<AudioConfig> = _audioConfig.asStateFlow()

    private val _recordingLimitsConfig = MutableStateFlow(RecordingLimitsConfig())
    val recordingLimitsConfig: StateFlow<RecordingLimitsConfig> = _recordingLimitsConfig.asStateFlow()

    private val _batterySaverConfig = MutableStateFlow(BatterySaverConfig())
    val batterySaverConfig: StateFlow<BatterySaverConfig> = _batterySaverConfig.asStateFlow()

    private val _batteryState = MutableStateFlow(BatteryState())
    val batteryState: StateFlow<BatteryState> = _batteryState.asStateFlow()

    private var previousVideoConfigBeforeBatterySaver: VideoConfig? = null

    private val _storageFolder = MutableStateFlow(StorageFolderConfig())
    val storageFolder: StateFlow<StorageFolderConfig> = _storageFolder.asStateFlow()

    private val _autoCleanupConfig = MutableStateFlow(AutoCleanupConfig())
    val autoCleanupConfig: StateFlow<AutoCleanupConfig> = _autoCleanupConfig.asStateFlow()

    private val _countdownDuration = MutableStateFlow(5)
    val countdownDuration: StateFlow<Int> = _countdownDuration.asStateFlow()

    private val _overlayWatermarkEnabled = MutableStateFlow(true)
    val overlayWatermarkEnabled: StateFlow<Boolean> = _overlayWatermarkEnabled.asStateFlow()

    private val _isFloatingMenuVisible = MutableStateFlow(true)
    val isFloatingMenuVisible: StateFlow<Boolean> = _isFloatingMenuVisible.asStateFlow()

    private val _isPainterActive = MutableStateFlow(false)
    val isPainterActive: StateFlow<Boolean> = _isPainterActive.asStateFlow()

    private val _showTouchesEnabled = MutableStateFlow(true)
    val showTouchesEnabled: StateFlow<Boolean> = _showTouchesEnabled.asStateFlow()

    private val _touchColor = MutableStateFlow("Teal")
    val touchColor: StateFlow<String> = _touchColor.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.DARK)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _scheduledRecordings = MutableStateFlow<List<ScheduledRecording>>(
        listOf(
            ScheduledRecording(
                id = "sched-1",
                title = "Live Product Demo & Q&A",
                targetEpochMillis = System.currentTimeMillis() + 1000L * 60 * 35, // 35 min from now
                maxDurationMinutes = 20,
                qualityPreset = "1080p",
                audioEnabled = true,
                status = ScheduledStatus.ARMED
            ),
            ScheduledRecording(
                id = "sched-2",
                title = "Mobile App Walkthrough",
                targetEpochMillis = System.currentTimeMillis() + 1000L * 60 * 180, // 3 hours from now
                maxDurationMinutes = 10,
                qualityPreset = "720p",
                audioEnabled = true,
                status = ScheduledStatus.PENDING
            )
        )
    )
    val scheduledRecordings: StateFlow<List<ScheduledRecording>> = _scheduledRecordings.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _selectedVideoForPreview = MutableStateFlow<VideoItem?>(null)
    val selectedVideoForPreview: StateFlow<VideoItem?> = _selectedVideoForPreview.asStateFlow()

    // Recorded videos repository with sample timestamps for testing auto-cleanup
    private val _videos = MutableStateFlow<List<VideoItem>>(
        listOf(
            VideoItem(
                id = "sample-1",
                name = "ScreenRecorder-Today-1920x1080.mp4",
                path = "/storage/emulated/0/Movies/ScreenRecorder/sample-1.mp4",
                durationSeconds = 125,
                sizeBytes = 18_450_000,
                resolution = "1920x1080",
                timestamp = System.currentTimeMillis() - 1000L * 60 * 20,
                isStarred = true
            ),
            VideoItem(
                id = "sample-2",
                name = "ScreenRecorder-8DaysAgo-1280x720.mp4",
                path = "/storage/emulated/0/Movies/ScreenRecorder/sample-2.mp4",
                durationSeconds = 48,
                sizeBytes = 6_200_000,
                resolution = "1280x720",
                timestamp = System.currentTimeMillis() - 8L * 86_400_000L,
                isStarred = false
            ),
            VideoItem(
                id = "sample-3",
                name = "ScreenRecorder-45DaysAgo-Archive.mp4",
                path = "/storage/emulated/0/Movies/ScreenRecorder/sample-3.mp4",
                durationSeconds = 240,
                sizeBytes = 35_100_000,
                resolution = "1920x1080",
                timestamp = System.currentTimeMillis() - 45L * 86_400_000L,
                isStarred = false
            )
        )
    )
    val videos: StateFlow<List<VideoItem>> = _videos.asStateFlow()

    // Painter state
    private val _drawPaths = MutableStateFlow<List<DrawPath>>(emptyList())
    val drawPaths: StateFlow<List<DrawPath>> = _drawPaths.asStateFlow()

    private val _currentPath = MutableStateFlow<DrawPath?>(null)
    val currentPath: StateFlow<DrawPath?> = _currentPath.asStateFlow()

    private var timerJob: Job? = null
    private var countdownJob: Job? = null

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                checkScheduledRecordings()
            }
        }
    }

    private fun checkScheduledRecordings() {
        val now = System.currentTimeMillis()
        val list = _scheduledRecordings.value
        for (item in list) {
            if ((item.status == ScheduledStatus.ARMED || item.status == ScheduledStatus.PENDING) && item.isEnabled) {
                if (now >= item.targetEpochMillis) {
                    _scheduledRecordings.value = list.map {
                        if (it.id == item.id) it.copy(status = ScheduledStatus.TRIGGERED) else it
                    }
                    showToast("⏰ Scheduled Recording Triggered: ${item.title}!")
                    selectQualityPreset(item.qualityPreset)
                    updateAudioConfig(_audioConfig.value.copy(includeMic = item.audioEnabled))
                    if (item.maxDurationMinutes > 0) {
                        _recordingLimitsConfig.value = _recordingLimitsConfig.value.copy(
                            durationLimitEnabled = true,
                            maxDurationMinutes = item.maxDurationMinutes
                        )
                    }
                    if (_recordingStatus.value == RecordingStatus.IDLE) {
                        startRecordingFlow()
                    }
                    break
                }
            }
        }
    }

    fun scheduleRecording(
        title: String,
        targetEpochMillis: Long,
        maxDurationMinutes: Int = 15,
        qualityPreset: String = "1080p",
        audioEnabled: Boolean = true
    ) {
        val newScheduled = ScheduledRecording(
            id = "sched-${System.currentTimeMillis()}",
            title = title.ifBlank { "Scheduled Session" },
            targetEpochMillis = targetEpochMillis,
            maxDurationMinutes = maxDurationMinutes,
            qualityPreset = qualityPreset,
            audioEnabled = audioEnabled,
            status = ScheduledStatus.ARMED
        )
        _scheduledRecordings.value = listOf(newScheduled) + _scheduledRecordings.value
        val diffMinutes = ((targetEpochMillis - System.currentTimeMillis()) / (1000 * 60)).coerceAtLeast(0)
        showToast("Recording scheduled: '$title' in ~$diffMinutes min")
    }

    fun cancelScheduledRecording(id: String) {
        _scheduledRecordings.value = _scheduledRecordings.value.map {
            if (it.id == id) it.copy(status = ScheduledStatus.CANCELLED) else it
        }
        showToast("Scheduled recording cancelled")
    }

    fun deleteScheduledRecording(id: String) {
        _scheduledRecordings.value = _scheduledRecordings.value.filter { it.id != id }
        showToast("Scheduled item deleted")
    }

    fun toggleScheduledArmed(id: String) {
        _scheduledRecordings.value = _scheduledRecordings.value.map {
            if (it.id == id) {
                val nextStatus = if (it.status == ScheduledStatus.ARMED) ScheduledStatus.PENDING else ScheduledStatus.ARMED
                it.copy(status = nextStatus)
            } else it
        }
    }

    fun startScheduledNow(scheduled: ScheduledRecording) {
        selectQualityPreset(scheduled.qualityPreset)
        updateAudioConfig(_audioConfig.value.copy(includeMic = scheduled.audioEnabled))
        if (scheduled.maxDurationMinutes > 0) {
            _recordingLimitsConfig.value = _recordingLimitsConfig.value.copy(
                durationLimitEnabled = true,
                maxDurationMinutes = scheduled.maxDurationMinutes
            )
        }
        _scheduledRecordings.value = _scheduledRecordings.value.map {
            if (it.id == scheduled.id) it.copy(status = ScheduledStatus.TRIGGERED) else it
        }
        showToast("Starting scheduled recording now: ${scheduled.title}")
        startRecordingFlow()
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _toastMessage.value = message
            delay(3000)
            if (_toastMessage.value == message) {
                _toastMessage.value = null
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun toggleFloatingMenu() {
        _isFloatingMenuVisible.value = !_isFloatingMenuVisible.value
        showToast(if (_isFloatingMenuVisible.value) "Floating Circular Menu enabled" else "Floating Menu hidden")
    }

    fun togglePainter() {
        _isPainterActive.value = !_isPainterActive.value
        showToast(if (_isPainterActive.value) "Painter markup active" else "Painter closed")
    }

    fun startRecordingFlow() {
        if (_recordingStatus.value != RecordingStatus.IDLE) return

        val duration = _countdownDuration.value
        if (duration > 0) {
            _recordingStatus.value = RecordingStatus.COUNTDOWN
            countdownJob?.cancel()
            countdownJob = viewModelScope.launch {
                for (i in duration downTo 1) {
                    _countdownValue.value = i
                    delay(1000)
                }
                _countdownValue.value = null
                actuallyStartRecording()
            }
        } else {
            actuallyStartRecording()
        }
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        _countdownValue.value = null
        _recordingStatus.value = RecordingStatus.IDLE
        showToast("Screen recording countdown cancelled")
    }

    fun updateLimitsConfig(config: RecordingLimitsConfig) {
        _recordingLimitsConfig.value = config
        showToast("Recording limits updated")
    }

    fun calculateEstimatedBytes(seconds: Long, bitrate: Int): Long {
        val audioBitrate = 128_000L
        val totalBitrate = bitrate.toLong() + audioBitrate
        return (seconds * (totalBitrate / 8L)).coerceAtLeast(seconds * 100_000L)
    }

    private fun checkLimitsAndAutoStop(): Boolean {
        val limits = _recordingLimitsConfig.value
        val elapsed = _elapsedSeconds.value

        if (limits.durationLimitEnabled) {
            val maxSec = limits.maxDurationMinutes * 60L
            if (elapsed >= maxSec) {
                stopRecording()
                showToast("Duration limit reached (${limits.maxDurationMinutes}m). Saved automatically.")
                return true
            }
        }

        if (limits.fileSizeLimitEnabled) {
            val estBytes = calculateEstimatedBytes(elapsed, _videoConfig.value.bitrate)
            val maxBytes = limits.maxFileSizeMB * 1024L * 1024L
            if (estBytes >= maxBytes) {
                stopRecording()
                showToast("File size limit reached (${limits.maxFileSizeMB} MB). Saved automatically.")
                return true
            }
        }

        // Critical battery auto-stop check
        if (_batterySaverConfig.value.isEnabled && _batterySaverConfig.value.autoStopAtCritical) {
            val batt = _batteryState.value
            if (batt.levelPercent <= _batterySaverConfig.value.criticalThresholdPercent && !batt.isCharging) {
                stopRecording()
                showToast("⚠️ Critical battery (${batt.levelPercent}%): Auto-saved to protect file.")
                return true
            }
        }
        return false
    }

    private fun actuallyStartRecording() {
        evaluateBatterySaver()
        _recordingStatus.value = RecordingStatus.RECORDING
        _elapsedSeconds.value = 0L
        showToast("Recording started! Screen capture active.")

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_recordingStatus.value == RecordingStatus.RECORDING) {
                delay(1000)
                _elapsedSeconds.value += 1
                evaluateBatterySaver()
                if (checkLimitsAndAutoStop()) break
            }
        }
    }

    fun pauseRecording() {
        if (_recordingStatus.value == RecordingStatus.RECORDING) {
            _recordingStatus.value = RecordingStatus.PAUSED
            showToast("Recording paused — video session preserved")
        }
    }

    fun resumeRecording() {
        if (_recordingStatus.value == RecordingStatus.PAUSED) {
            _recordingStatus.value = RecordingStatus.RECORDING
            showToast("Recording resumed — continuing session")
            timerJob = viewModelScope.launch {
                while (_recordingStatus.value == RecordingStatus.RECORDING) {
                    delay(1000)
                    _elapsedSeconds.value += 1
                    if (checkLimitsAndAutoStop()) break
                }
            }
        }
    }

    fun setDestinationFolder(path: String, displayName: String, uriString: String? = null) {
        _storageFolder.value = _storageFolder.value.copy(
            path = path,
            displayName = displayName,
            uriString = uriString
        )
        showToast("Destination folder set: $displayName")
    }

    fun toggleOrganizeByDate(enabled: Boolean) {
        _storageFolder.value = _storageFolder.value.copy(organizeByDate = enabled)
        showToast(if (enabled) "Organize by date (YYYY-MM) enabled" else "Organize by date disabled")
    }

    fun stopRecording() {
        if (_recordingStatus.value == RecordingStatus.RECORDING || _recordingStatus.value == RecordingStatus.PAUSED) {
            val duration = _elapsedSeconds.value
            _recordingStatus.value = RecordingStatus.IDLE
            timerJob?.cancel()

            val config = _videoConfig.value
            val folder = _storageFolder.value
            val timeMillis = System.currentTimeMillis()
            val fileName = "ScreenRecorder-$timeMillis-${config.width}x${config.height}.mp4"
            val filePath = if (folder.organizeByDate) {
                val dateFolder = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(timeMillis))
                "${folder.path}/$dateFolder/$fileName"
            } else {
                "${folder.path}/$fileName"
            }

            val newVideo = VideoItem(
                id = "rec-$timeMillis",
                name = fileName,
                path = filePath,
                durationSeconds = duration,
                sizeBytes = (duration * 1_200_000).coerceAtLeast(1_500_000),
                resolution = config.resolution,
                timestamp = timeMillis
            )

            _videos.value = listOf(newVideo) + _videos.value
            _selectedVideoForPreview.value = newVideo
            showToast("Saved to ${folder.displayName}: $fileName")

            if (_autoCleanupConfig.value.autoDeleteEnabled) {
                performAutoCleanup(manualRun = false)
            }
        }
    }

    fun updateAutoCleanupConfig(config: AutoCleanupConfig) {
        _autoCleanupConfig.value = config
        showToast("Storage auto-cleanup settings updated")
        if (config.autoDeleteEnabled) {
            performAutoCleanup(manualRun = false)
        }
    }

    fun toggleStarVideo(video: VideoItem) {
        _videos.value = _videos.value.map {
            if (it.id == video.id) it.copy(isStarred = !it.isStarred) else it
        }
        val isNowStarred = _videos.value.find { it.id == video.id }?.isStarred == true
        showToast(if (isNowStarred) "Recording starred & protected from auto-cleanup" else "Recording unstarred")
    }

    fun performAutoCleanup(manualRun: Boolean = false): Int {
        val config = _autoCleanupConfig.value
        val cutoffTimestamp = System.currentTimeMillis() - (config.retentionDays * 86_400_000L)
        val currentList = _videos.value
        val toDelete = currentList.filter { video ->
            val isOld = video.timestamp < cutoffTimestamp
            val isProtected = config.protectStarredVideos && video.isStarred
            isOld && !isProtected
        }

        if (toDelete.isNotEmpty()) {
            val totalReclaimedBytes = toDelete.sumOf { it.sizeBytes }
            val reclaimedMB = totalReclaimedBytes / (1024 * 1024)
            _videos.value = currentList.filterNot { toDelete.contains(it) }
            showToast("Auto-cleanup pruned ${toDelete.size} recording(s) older than ${config.retentionDays}d (reclaimed ~${reclaimedMB} MB)")
        } else if (manualRun) {
            showToast("No recordings older than ${config.retentionDays} days found to clean up.")
        }
        return toDelete.size
    }

    fun deleteVideo(video: VideoItem) {
        _videos.value = _videos.value.filter { it.id != video.id }
        if (_selectedVideoForPreview.value?.id == video.id) {
            _selectedVideoForPreview.value = null
        }
        showToast("Video deleted")
    }

    fun trimVideo(originalVideo: VideoItem, startSeconds: Long, endSeconds: Long, saveAsNew: Boolean = true) {
        val trimmedDuration = (endSeconds - startSeconds).coerceAtLeast(1L)
        val ratio = trimmedDuration.toDouble() / originalVideo.durationSeconds.coerceAtLeast(1L)
        val trimmedSizeBytes = (originalVideo.sizeBytes * ratio).toLong().coerceAtLeast(500_000L)

        val baseName = originalVideo.name.substringBeforeLast('.')
        val ext = originalVideo.name.substringAfterLast('.', "mp4")
        val timestamp = System.currentTimeMillis()
        val newName = if (saveAsNew) {
            "${baseName}_trim_${startSeconds}s-${endSeconds}s.$ext"
        } else {
            originalVideo.name
        }

        val basePath = originalVideo.path.substringBeforeLast('/')
        val newPath = "$basePath/$newName"

        val trimmedVideo = VideoItem(
            id = if (saveAsNew) "rec-trim-$timestamp" else originalVideo.id,
            name = newName,
            path = newPath,
            durationSeconds = trimmedDuration,
            sizeBytes = trimmedSizeBytes,
            resolution = originalVideo.resolution,
            timestamp = timestamp,
            isStarred = originalVideo.isStarred
        )

        if (saveAsNew) {
            _videos.value = listOf(trimmedVideo) + _videos.value
        } else {
            _videos.value = _videos.value.map { if (it.id == originalVideo.id) trimmedVideo else it }
        }

        _selectedVideoForPreview.value = trimmedVideo
        val mins = trimmedDuration / 60
        val secs = trimmedDuration % 60
        showToast("Trimmed video saved! New duration: ${String.format("%02d:%02d", mins, secs)}")
    }

    fun selectVideoForPreview(video: VideoItem?) {
        _selectedVideoForPreview.value = video
    }

    fun updateVideoConfig(config: VideoConfig) {
        _videoConfig.value = config
    }

    fun selectQualityPreset(preset: String) {
        val current = _videoConfig.value
        val isPortrait = current.orientation == "Portrait"
        val (width, height, baseBitrate) = when (preset) {
            "480p" -> Triple(if (isPortrait) 480 else 854, if (isPortrait) 854 else 480, 1_500_000)
            "720p" -> Triple(if (isPortrait) 720 else 1280, if (isPortrait) 1280 else 720, 2_500_000)
            "1440p" -> Triple(if (isPortrait) 1440 else 2560, if (isPortrait) 2560 else 1440, 8_000_000)
            else -> Triple(if (isPortrait) 1080 else 1920, if (isPortrait) 1920 else 1080, 4_000_000) // 1080p
        }
        val adjustedBitrate = if (current.framerate == 60) (baseBitrate * 1.5).toInt() else baseBitrate

        _videoConfig.value = current.copy(
            resolution = "${width}x${height}",
            width = width,
            height = height,
            bitrate = adjustedBitrate
        )
        showToast("Video quality set to $preset (${width}x${height} @ ${current.framerate}fps)")
    }

    fun selectFramerate(fps: Int) {
        val current = _videoConfig.value
        val baseBitrate = when {
            current.height <= 480 || current.width <= 480 -> 1_500_000
            current.height <= 720 || current.width <= 720 -> 2_500_000
            current.height <= 1080 || current.width <= 1080 -> 4_000_000
            else -> 8_000_000
        }
        val adjustedBitrate = if (fps == 60) (baseBitrate * 1.5).toInt() else baseBitrate

        _videoConfig.value = current.copy(
            framerate = fps,
            bitrate = adjustedBitrate
        )
        showToast("Framerate set to ${fps} FPS (${if (fps == 60) "Ultra-Smooth" else "Standard"})")
    }

    fun setOrientation(orientation: String) {
        val current = _videoConfig.value
        if (current.orientation == orientation) return
        val w = current.width
        val h = current.height
        val newWidth = if (orientation == "Portrait") minOf(w, h) else maxOf(w, h)
        val newHeight = if (orientation == "Portrait") maxOf(w, h) else minOf(w, h)
        _videoConfig.value = current.copy(
            orientation = orientation,
            width = newWidth,
            height = newHeight,
            resolution = "${newWidth}x${newHeight}"
        )
        showToast("Orientation set to $orientation (${newWidth}x${newHeight})")
    }

    fun updateAudioConfig(config: AudioConfig) {
        _audioConfig.value = config
    }

    fun setAudioSource(source: AudioSourceOption) {
        _audioConfig.value = _audioConfig.value.copy(
            audioSource = source,
            includeMic = source != AudioSourceOption.MUTE
        )
        showToast("Audio source: ${source.title}")
    }

    fun setAudioVolumes(internalVol: Float, micVol: Float) {
        _audioConfig.value = _audioConfig.value.copy(
            internalAudioVolume = internalVol,
            micAudioVolume = micVol
        )
    }

    fun setAudioSampleRate(sampleRate: Int) {
        _audioConfig.value = _audioConfig.value.copy(sampleRate = sampleRate)
        showToast("Audio sample rate: ${sampleRate / 1000} kHz")
    }

    fun setAudioChannels(channels: Int) {
        _audioConfig.value = _audioConfig.value.copy(channelCount = channels)
        showToast(if (channels == 2) "Stereo audio (2 channels)" else "Mono audio (1 channel)")
    }

    fun setCountdownDuration(seconds: Int) {
        _countdownDuration.value = seconds
    }

    fun setOverlayWatermark(enabled: Boolean) {
        _overlayWatermarkEnabled.value = enabled
    }

    fun setShowTouchesEnabled(enabled: Boolean) {
        _showTouchesEnabled.value = enabled
        showToast(if (enabled) "Visual touch indicator overlay enabled" else "Visual touch indicator overlay disabled")
    }

    fun setTouchColor(colorName: String) {
        _touchColor.value = colorName
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        showToast("Theme switched to ${mode.title}")
    }

    // Battery-Saver Management
    fun updateBatteryState(level: Int, isCharging: Boolean) {
        val isLow = level <= _batterySaverConfig.value.thresholdPercent && !isCharging
        _batteryState.value = BatteryState(
            levelPercent = level,
            isCharging = isCharging,
            isLowBattery = isLow,
            isSimulated = false
        )
        evaluateBatterySaver()
    }

    fun setSimulatedBattery(level: Int, isCharging: Boolean) {
        val isLow = level <= _batterySaverConfig.value.thresholdPercent && !isCharging
        _batteryState.value = BatteryState(
            levelPercent = level,
            isCharging = isCharging,
            isLowBattery = isLow,
            isSimulated = true
        )
        evaluateBatterySaver()
    }

    fun updateBatterySaverConfig(config: BatterySaverConfig) {
        _batterySaverConfig.value = config
        evaluateBatterySaver()
    }

    fun setBatterySaverEnabled(enabled: Boolean) {
        _batterySaverConfig.value = _batterySaverConfig.value.copy(isEnabled = enabled)
        if (!enabled && _batterySaverConfig.value.isBatterySaverActive) {
            restorePreBatterySaverConfig()
        } else {
            evaluateBatterySaver()
        }
        showToast(if (enabled) "Battery-Saver enabled (threshold: ${_batterySaverConfig.value.thresholdPercent}%)" else "Battery-Saver mode disabled")
    }

    fun setBatterySaverThreshold(threshold: Int) {
        _batterySaverConfig.value = _batterySaverConfig.value.copy(thresholdPercent = threshold)
        evaluateBatterySaver()
        showToast("Battery threshold set to $threshold%")
    }

    fun setBatterySaverTargetPreset(preset: String) {
        _batterySaverConfig.value = _batterySaverConfig.value.copy(targetResolutionPreset = preset)
        if (_batterySaverConfig.value.isBatterySaverActive) {
            applyBatterySaverDegradation()
        }
    }

    fun setBatterySaverTargetFramerate(fps: Int) {
        _batterySaverConfig.value = _batterySaverConfig.value.copy(targetFramerate = fps)
        if (_batterySaverConfig.value.isBatterySaverActive) {
            applyBatterySaverDegradation()
        }
    }

    fun setBatterySaverAutoStopAtCritical(autoStop: Boolean) {
        _batterySaverConfig.value = _batterySaverConfig.value.copy(autoStopAtCritical = autoStop)
    }

    fun evaluateBatterySaver() {
        val cfg = _batterySaverConfig.value
        val batt = _batteryState.value
        if (!cfg.isEnabled) {
            if (cfg.isBatterySaverActive) {
                restorePreBatterySaverConfig()
            }
            return
        }

        val shouldBeActive = batt.levelPercent <= cfg.thresholdPercent && !batt.isCharging
        if (shouldBeActive && !cfg.isBatterySaverActive) {
            applyBatterySaverDegradation()
        } else if (!shouldBeActive && cfg.isBatterySaverActive) {
            restorePreBatterySaverConfig()
        }
    }

    private fun applyBatterySaverDegradation() {
        val cfg = _batterySaverConfig.value
        val currentVideo = _videoConfig.value

        if (previousVideoConfigBeforeBatterySaver == null) {
            previousVideoConfigBeforeBatterySaver = currentVideo
        }

        val isLandscape = currentVideo.orientation == "Landscape"
        val (w, h) = when (cfg.targetResolutionPreset) {
            "480p" -> if (isLandscape) 854 to 480 else 480 to 854
            "720p" -> if (isLandscape) 1280 to 720 else 720 to 1280
            else -> if (isLandscape) 854 to 480 else 480 to 854
        }
        val bitrate = when (cfg.targetResolutionPreset) {
            "480p" -> 1500000
            "720p" -> 2500000
            else -> 1500000
        }

        _videoConfig.value = currentVideo.copy(
            resolution = "${w}x${h}",
            width = w,
            height = h,
            bitrate = bitrate,
            framerate = cfg.targetFramerate
        )
        _batterySaverConfig.value = cfg.copy(isBatterySaverActive = true)
        showToast("⚡ Low Battery (${_batteryState.value.levelPercent}%): Switched to ${cfg.targetResolutionPreset} @ ${cfg.targetFramerate}fps")
    }

    private fun restorePreBatterySaverConfig() {
        previousVideoConfigBeforeBatterySaver?.let { prev ->
            _videoConfig.value = prev
            previousVideoConfigBeforeBatterySaver = null
        }
        _batterySaverConfig.value = _batterySaverConfig.value.copy(isBatterySaverActive = false)
        showToast("⚡ Battery normal: Restored video settings")
    }

    // Painter methods
    fun startDrawing(point: Offset, color: Color, strokeWidth: Float, isHighlighter: Boolean, isEraser: Boolean) {
        _currentPath.value = DrawPath(
            points = listOf(point),
            color = color,
            strokeWidth = strokeWidth,
            isHighlighter = isHighlighter,
            isEraser = isEraser
        )
    }

    fun continueDrawing(point: Offset) {
        val curr = _currentPath.value ?: return
        _currentPath.value = curr.copy(points = curr.points + point)
    }

    fun endDrawing() {
        val curr = _currentPath.value ?: return
        if (curr.points.size > 1) {
            _drawPaths.value = _drawPaths.value + curr
        }
        _currentPath.value = null
    }

    fun undoDrawing() {
        val list = _drawPaths.value
        if (list.isNotEmpty()) {
            _drawPaths.value = list.dropLast(1)
        }
    }

    fun clearDrawing() {
        _drawPaths.value = emptyList()
        _currentPath.value = null
        showToast("Canvas cleared")
    }

    fun clearAllVideos() {
        _videos.value = emptyList()
        showToast("All recordings cleared")
    }
}
