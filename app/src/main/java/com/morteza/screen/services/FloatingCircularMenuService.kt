package com.morteza.screen.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Messenger
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.morteza.screen.R
import com.morteza.screen.common.Constants
import com.morteza.screen.services.helper.FloatingUiHelperInterface
import com.morteza.screen.services.helper.IncomingHandler
import com.morteza.screen.tools.ScreenRecorderManager

/**
 * @author Morteza
 * @version 2019/12/3
 */
class FloatingCircularMenuService : Service(), IncomingHandler.IncomingHandlerCallback,
    FloatingUiHelperInterface.ServiceCallback {

    private val mScreenRecorderManager by lazy { ScreenRecorderManager(applicationContext, this) }
    private val mForegroundNotification by lazy { createNotification(this) }

    override fun doWork(msg: Int) {
        when (msg) {
            Constants.START_VIDEO_RECORDER -> {
                mScreenRecorderManager.startCapturing()
                Toast.makeText(applicationContext, "START_VIDEO_RECORDER !", Toast.LENGTH_SHORT)
                    .show()
            }
            else -> {
                Toast.makeText(applicationContext, "Out Of Handle Msg !", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private var mMessenger: Messenger? = null

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    override fun onBind(intent: Intent): IBinder {
        mMessenger = Messenger(IncomingHandler(this))
        return mMessenger!!.binder
    }

    @SuppressLint("InflateParams")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(1361, mForegroundNotification)
        mScreenRecorderManager.initializing(intent)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        mScreenRecorderManager.destroyView()
        super.onDestroy()
    }

    override fun stopService() {
        stopSelf()
        Runtime.getRuntime().halt(1361)
    }

    private fun createNotification(context: Context): Notification {

        val notificationChannel = context.getString(R.string.default_floatingview_channel_name)

        return NotificationCompat.Builder(context, notificationChannel).apply {
            setWhen(System.currentTimeMillis())
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle(context.getString(R.string.chathead_content_title))
            setContentText(context.getString(R.string.content_text))
            setOngoing(true)
            priority = NotificationCompat.PRIORITY_MIN
            setCategory(NotificationCompat.CATEGORY_SERVICE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.createNotificationChannel(
                    NotificationChannel(
                        notificationChannel,
                        "App Service",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
        }.build()
    }

}