package com.morteza.screen.service

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.morteza.screen.MainActivity
import java.io.File

class ScreenRecordService : Service() {

    companion object {
        const val CHANNEL_ID = "screen_recording_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.morteza.screen.ACTION_START"
        const val ACTION_PAUSE = "com.morteza.screen.ACTION_PAUSE"
        const val ACTION_RESUME = "com.morteza.screen.ACTION_RESUME"
        const val ACTION_STOP = "com.morteza.screen.ACTION_STOP"
        const val ACTION_UPDATE_TIMER = "com.morteza.screen.ACTION_UPDATE_TIMER"

        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"
        const val EXTRA_OUTPUT_PATH = "extra_output_path"
        const val EXTRA_WIDTH = "extra_width"
        const val EXTRA_HEIGHT = "extra_height"
        const val EXTRA_BITRATE = "extra_bitrate"
        const val EXTRA_FRAMERATE = "extra_framerate"
        const val EXTRA_INCLUDE_AUDIO = "extra_include_audio"
        const val EXTRA_SECONDS = "extra_seconds"
        const val EXTRA_SPECS = "extra_specs"

        var isServiceRunning = false
            private set

        var isRecordingPaused = false
            private set

        var onNotificationAction: ((String) -> Unit)? = null

        fun start(
            context: Context,
            resultCode: Int = Activity.RESULT_OK,
            resultData: Intent? = null,
            outputPath: String = "",
            width: Int = 1080,
            height: Int = 1920,
            bitrate: Int = 4000000,
            framerate: Int = 30,
            includeAudio: Boolean = true,
            specs: String = "1080p • 30fps"
        ) {
            val intent = Intent(context, ScreenRecordService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RESULT_CODE, resultCode)
                resultData?.let { putExtra(EXTRA_RESULT_DATA, it) }
                putExtra(EXTRA_OUTPUT_PATH, outputPath)
                putExtra(EXTRA_WIDTH, width)
                putExtra(EXTRA_HEIGHT, height)
                putExtra(EXTRA_BITRATE, bitrate)
                putExtra(EXTRA_FRAMERATE, framerate)
                putExtra(EXTRA_INCLUDE_AUDIO, includeAudio)
                putExtra(EXTRA_SPECS, specs)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pause(context: Context) {
            val intent = Intent(context, ScreenRecordService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, ScreenRecordService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, ScreenRecordService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun updateTimer(context: Context, elapsedSeconds: Long) {
            if (!isServiceRunning) return
            val intent = Intent(context, ScreenRecordService::class.java).apply {
                action = ACTION_UPDATE_TIMER
                putExtra(EXTRA_SECONDS, elapsedSeconds)
            }
            context.startService(intent)
        }
    }

    private var currentSeconds: Long = 0L
    private var currentSpecs: String = "1080p • 30fps"

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mediaProjectionCallback: MediaProjection.Callback? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                val resultData: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_RESULT_DATA)
                }

                val outputPath = intent.getStringExtra(EXTRA_OUTPUT_PATH) ?: ""
                val width = intent.getIntExtra(EXTRA_WIDTH, 1080)
                val height = intent.getIntExtra(EXTRA_HEIGHT, 1920)
                val bitrate = intent.getIntExtra(EXTRA_BITRATE, 4000000)
                val framerate = intent.getIntExtra(EXTRA_FRAMERATE, 30)
                val includeAudio = intent.getBooleanExtra(EXTRA_INCLUDE_AUDIO, true)

                val hasProjectionData = resultCode == Activity.RESULT_OK && resultData != null && outputPath.isNotEmpty()

                isServiceRunning = true
                isRecordingPaused = false
                currentSeconds = 0L
                currentSpecs = intent.getStringExtra(EXTRA_SPECS) ?: "1080p • 30fps"

                val notification = buildRecordingNotification(currentSeconds, isPaused = false)
                startForegroundCompat(notification, hasMediaProjection = hasProjectionData)

                if (hasProjectionData) {
                    startRecordingSession(resultCode, resultData, outputPath, width, height, bitrate, framerate, includeAudio)
                }
            }

            ACTION_PAUSE -> {
                if (isServiceRunning && !isRecordingPaused) {
                    isRecordingPaused = true
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            mediaRecorder?.pause()
                        }
                    } catch (_: Exception) {}
                    updateNotification(currentSeconds, isPaused = true)
                    onNotificationAction?.invoke(ACTION_PAUSE)
                }
            }

            ACTION_RESUME -> {
                if (isServiceRunning && isRecordingPaused) {
                    isRecordingPaused = false
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            mediaRecorder?.resume()
                        }
                    } catch (_: Exception) {}
                    updateNotification(currentSeconds, isPaused = false)
                    onNotificationAction?.invoke(ACTION_RESUME)
                }
            }

            ACTION_UPDATE_TIMER -> {
                currentSeconds = intent.getLongExtra(EXTRA_SECONDS, currentSeconds)
                updateNotification(currentSeconds, isRecordingPaused)
            }

            ACTION_STOP -> {
                stopRecordingSession()
            }
        }

        return START_NOT_STICKY
    }

    private fun startRecordingSession(
        resultCode: Int,
        resultData: Intent,
        outputPath: String,
        width: Int,
        height: Int,
        bitrate: Int,
        framerate: Int,
        includeAudio: Boolean
    ) {
        try {
            // Android 11+ requirement: getMediaProjection MUST be called AFTER startForeground
            mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, resultData)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                mediaProjectionCallback = object : MediaProjection.Callback() {
                    override fun onStop() {
                        stopRecordingSession()
                    }
                }
                mediaProjectionCallback?.let { mediaProjection?.registerCallback(it, null) }
            }

            val file = File(outputPath)
            file.parentFile?.mkdirs()

            // Initialize MediaRecorder for Android 11+
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                if (includeAudio) {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                }
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(file.absolutePath)
                setVideoSize(width, height)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                if (includeAudio) {
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                }
                setVideoEncodingBitRate(bitrate)
                setVideoFrameRate(framerate)
                prepare()
            }

            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(metrics)
            val densityDpi = metrics.densityDpi

            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "ScreenRecordService",
                width,
                height,
                densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder?.surface,
                null,
                null
            )

            mediaRecorder?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecordingSession() {
        isServiceRunning = false
        isRecordingPaused = false
        onNotificationAction?.invoke(ACTION_STOP)

        try {
            mediaRecorder?.apply {
                try { stop() } catch (_: Exception) {}
                reset()
                release()
            }
        } catch (_: Exception) {}
        mediaRecorder = null

        try {
            virtualDisplay?.release()
        } catch (_: Exception) {}
        virtualDisplay = null

        try {
            mediaProjectionCallback?.let { mediaProjection?.unregisterCallback(it) }
            mediaProjection?.stop()
        } catch (_: Exception) {}
        mediaProjection = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopRecordingSession()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Active Screen Recording"
            val descriptionText = "Displays active screen recording status with pause and stop controls"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildRecordingNotification(seconds: Long, isPaused: Boolean): Notification {
        val mins = seconds / 60
        val secs = seconds % 60
        val formattedTime = String.format("%02d:%02d", mins, secs)

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, ScreenRecordService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val pauseResumeAction = if (isPaused) ACTION_RESUME else ACTION_PAUSE
        val pauseResumePendingIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, ScreenRecordService::class.java).apply { action = pauseResumeAction },
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val title = if (isPaused) "Screen Recording Paused [$formattedTime]" else "Recording Screen [$formattedTime]"
        val statusText = if (isPaused) {
            "Paused at $formattedTime • Tap Resume to continue"
        } else {
            "Active: $formattedTime • $currentSpecs • Tap Stop when done"
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle(title)
            .setContentText(statusText)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                if (isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause,
                if (isPaused) "Resume" else "Pause",
                pauseResumePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop & Save",
                stopPendingIntent
            )

        return builder.build()
    }

    private fun updateNotification(seconds: Long, isPaused: Boolean) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildRecordingNotification(seconds, isPaused))
    }

    private fun startForegroundCompat(notification: Notification, hasMediaProjection: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                var fgsTypes = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                if (hasMediaProjection) {
                    fgsTypes = fgsTypes or ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    fgsTypes = fgsTypes or ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                }
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    fgsTypes
                )
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    ServiceCompat.startForeground(
                        this,
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } catch (_: Exception) {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }
}
