package com.morteza.screen.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morteza.screen.model.*
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

    private val _countdownDuration = MutableStateFlow(5)
    val countdownDuration: StateFlow<Int> = _countdownDuration.asStateFlow()

    private val _overlayWatermarkEnabled = MutableStateFlow(true)
    val overlayWatermarkEnabled: StateFlow<Boolean> = _overlayWatermarkEnabled.asStateFlow()

    private val _isFloatingMenuVisible = MutableStateFlow(true)
    val isFloatingMenuVisible: StateFlow<Boolean> = _isFloatingMenuVisible.asStateFlow()

    private val _isPainterActive = MutableStateFlow(false)
    val isPainterActive: StateFlow<Boolean> = _isPainterActive.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _selectedVideoForPreview = MutableStateFlow<VideoItem?>(null)
    val selectedVideoForPreview: StateFlow<VideoItem?> = _selectedVideoForPreview.asStateFlow()

    // Recorded videos repository
    private val _videos = MutableStateFlow<List<VideoItem>>(
        listOf(
            VideoItem(
                id = "sample-1",
                name = "ScreenRecorder-20261005-1920x1080.mp4",
                path = "/storage/emulated/0/Movies/ScreenRecorder/sample-1.mp4",
                durationSeconds = 125,
                sizeBytes = 18_450_000,
                resolution = "1920x1080",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 20
            ),
            VideoItem(
                id = "sample-2",
                name = "ScreenRecorder-20261005-1280x720.mp4",
                path = "/storage/emulated/0/Movies/ScreenRecorder/sample-2.mp4",
                durationSeconds = 48,
                sizeBytes = 6_200_000,
                resolution = "1280x720",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 95
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

    private fun actuallyStartRecording() {
        _recordingStatus.value = RecordingStatus.RECORDING
        _elapsedSeconds.value = 0L
        showToast("Recording started! Screen capture active.")

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_recordingStatus.value == RecordingStatus.RECORDING) {
                delay(1000)
                _elapsedSeconds.value += 1
            }
        }
    }

    fun pauseRecording() {
        if (_recordingStatus.value == RecordingStatus.RECORDING) {
            _recordingStatus.value = RecordingStatus.PAUSED
            showToast("Recording paused")
        }
    }

    fun resumeRecording() {
        if (_recordingStatus.value == RecordingStatus.PAUSED) {
            _recordingStatus.value = RecordingStatus.RECORDING
            showToast("Recording resumed")
            timerJob = viewModelScope.launch {
                while (_recordingStatus.value == RecordingStatus.RECORDING) {
                    delay(1000)
                    _elapsedSeconds.value += 1
                }
            }
        }
    }

    fun stopRecording() {
        if (_recordingStatus.value == RecordingStatus.RECORDING || _recordingStatus.value == RecordingStatus.PAUSED) {
            val duration = _elapsedSeconds.value
            _recordingStatus.value = RecordingStatus.IDLE
            timerJob?.cancel()

            val config = _videoConfig.value
            val newVideo = VideoItem(
                id = "rec-${System.currentTimeMillis()}",
                name = "ScreenRecorder-${System.currentTimeMillis()}-${config.width}x${config.height}.mp4",
                path = "/storage/emulated/0/Movies/ScreenRecorder/rec-${System.currentTimeMillis()}.mp4",
                durationSeconds = duration,
                sizeBytes = (duration * 1_200_000).coerceAtLeast(1_500_000),
                resolution = config.resolution,
                timestamp = System.currentTimeMillis()
            )

            _videos.value = listOf(newVideo) + _videos.value
            _selectedVideoForPreview.value = newVideo
            showToast("Recording saved: ${newVideo.name}")
        }
    }

    fun deleteVideo(video: VideoItem) {
        _videos.value = _videos.value.filter { it.id != video.id }
        if (_selectedVideoForPreview.value?.id == video.id) {
            _selectedVideoForPreview.value = null
        }
        showToast("Video deleted")
    }

    fun selectVideoForPreview(video: VideoItem?) {
        _selectedVideoForPreview.value = video
    }

    fun updateVideoConfig(config: VideoConfig) {
        _videoConfig.value = config
    }

    fun updateAudioConfig(config: AudioConfig) {
        _audioConfig.value = config
    }

    fun setCountdownDuration(seconds: Int) {
        _countdownDuration.value = seconds
    }

    fun setOverlayWatermark(enabled: Boolean) {
        _overlayWatermarkEnabled.value = enabled
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
