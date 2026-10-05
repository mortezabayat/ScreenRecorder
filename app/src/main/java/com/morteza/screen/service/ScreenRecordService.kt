package com.morteza.screen.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.morteza.screen.MainActivity

class ScreenRecordService : Service() {

    companion object {
        const val CHANNEL_ID = "screen_recording_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.morteza.screen.ACTION_START"
        const val ACTION_PAUSE = "com.morteza.screen.ACTION_PAUSE"
        const val ACTION_RESUME = "com.morteza.screen.ACTION_RESUME"
        const val ACTION_STOP = "com.morteza.screen.ACTION_STOP"
        const val ACTION_UPDATE_TIMER = "com.morteza.screen.ACTION_UPDATE_TIMER"

        const val EXTRA_SECONDS = "extra_seconds"
        const val EXTRA_SPECS = "extra_specs"

        var isServiceRunning = false
            private set

        var isRecordingPaused = false
            private set

        var onNotificationAction: ((String) -> Unit)? = null

        fun start(context: Context, specs: String = "1080p • 30fps") {
            val intent = Intent(context, ScreenRecordService::class.java).apply {
                action = ACTION_START
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

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_START -> {
                isServiceRunning = true
                isRecordingPaused = false
                currentSeconds = 0L
                currentSpecs = intent.getStringExtra(EXTRA_SPECS) ?: "1080p • 30fps"
                val notification = buildRecordingNotification(currentSeconds, isPaused = false)
                startForegroundCompat(notification)
            }

            ACTION_PAUSE -> {
                isRecordingPaused = true
                updateNotification(currentSeconds, isPaused = true)
                onNotificationAction?.invoke(ACTION_PAUSE)
            }

            ACTION_RESUME -> {
                isRecordingPaused = false
                updateNotification(currentSeconds, isPaused = false)
                onNotificationAction?.invoke(ACTION_RESUME)
            }

            ACTION_UPDATE_TIMER -> {
                currentSeconds = intent.getLongExtra(EXTRA_SECONDS, currentSeconds)
                updateNotification(currentSeconds, isRecordingPaused)
            }

            ACTION_STOP -> {
                isServiceRunning = false
                isRecordingPaused = false
                onNotificationAction?.invoke(ACTION_STOP)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isServiceRunning = false
        isRecordingPaused = false
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
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildRecordingNotification(seconds: Long, isPaused: Boolean): Notification {
        val mins = seconds / 60
        val secs = seconds % 60
        val formattedTime = String.format("%02d:%02d", mins, secs)

        // PendingIntent to launch MainActivity on notification body tap
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Stop Action Intent
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, ScreenRecordService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Pause/Resume Action Intent
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
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildRecordingNotification(seconds, isPaused))
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } catch (_: SecurityException) {
                try {
                    val specialUseType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    } else {
                        0
                    }
                    ServiceCompat.startForeground(
                        this,
                        NOTIFICATION_ID,
                        notification,
                        specialUseType
                    )
                } catch (_: Exception) {
                    startForeground(NOTIFICATION_ID, notification)
                }
            } catch (_: Exception) {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }
}
