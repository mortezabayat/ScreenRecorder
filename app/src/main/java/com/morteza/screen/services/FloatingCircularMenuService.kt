package com.morteza.screen.services

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.CodecCapabilities
import android.media.MediaCodecList
import android.media.projection.MediaProjection
import android.net.Uri
import android.os.*
import android.os.StrictMode.VmPolicy
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.doOnEnd
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.isGone
import com.morteza.screen.BuildConfig
import com.morteza.screen.R
import com.morteza.screen.ScreenApp
import com.morteza.screen.common.Constants
import com.morteza.screen.recorder.*
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author Morteza
 * @version 2019/12/3
 */
class FloatingCircularMenuService : Service(), FloatingViewListener {

    private val windowManger by lazy {
        getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private var circularMenuParams: WindowManager.LayoutParams? = null
    private var actionButton: AppCompatImageView? = null
    private lateinit var circularMenu: ConstraintLayout
    private lateinit var flatAppBtn: AppCompatImageView
    private lateinit var toolsBtn: AppCompatImageView
    private lateinit var startRecordBtn: AppCompatImageView
    private lateinit var settingsBtn: AppCompatImageView
    private lateinit var homeBtn: AppCompatImageView
    private val constraintSetRTL = ConstraintSet()
    private val constraintSetLTR = ConstraintSet()
    private val radius by lazy { resources.getDimensionPixelSize(R.dimen.radius) }
    private val actionButtonSize by lazy { resources.getDimensionPixelSize(R.dimen.floating_icon_size) }
    private lateinit var gestureDetector: GestureDetector
    private var isHideCircularMenu = true
    private val metrics = DisplayMetrics()
    private var floatingViewManager: FloatingViewManager? = null
    private var xPosition: Int = 0
    private var yPosition: Int = 0
    private var mVideoRecorder: ScreenRecorder? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mAvcCodecInfos: Array<MediaCodecInfo> = emptyArray()
    private var mAacCodecInfos: Array<MediaCodecInfo> = emptyArray()
    private var mNotifications: Notifications? = null
    private val overMargin by lazy { (2 * metrics.density).toInt() }
    private val isMoveToEdge = true // For FloatingViewManager.MOVE_DIRECTION_THROWN

    /**
     * Handler of incoming messages from clients.
     */
    internal class IncomingHandler(context: Context, private val ss: FloatingCircularMenuService) :
        Handler() {
        private val applicationContext: Context = context.applicationContext

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.START_VIDEO_RECORDER -> {
                    ss.startCapturing()
                    Toast.makeText(applicationContext, "START_VIDEO_RECORDER !", Toast.LENGTH_SHORT)
                        .show()

                }
                else -> super.handleMessage(msg)
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    var mMessenger: Messenger? = null

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    override fun onBind(intent: Intent): IBinder {
        Toast.makeText(applicationContext, "binding", Toast.LENGTH_SHORT).show()
        mMessenger = Messenger(IncomingHandler(this, this))
        return mMessenger!!.binder
    }

    /**
     * Print information of all MediaCodec on this device.
     */
    private fun logCodecInfos(
        codecInfos: Array<MediaCodecInfo>,
        mimeType: String
    ) {
        for (info in codecInfos) {
            val builder = StringBuilder(512)
            val caps = info.getCapabilitiesForType(mimeType)
            builder.append("Encoder '").append(info.name).append('\'')
                .append("\n  supported : ")
                .append(Arrays.toString(info.supportedTypes))
            val videoCaps = caps.videoCapabilities
            if (videoCaps != null) {
                builder.append("\n  Video capabilities:")
                    .append("\n  Widths: ").append(videoCaps.supportedWidths)
                    .append("\n  Heights: ").append(videoCaps.supportedHeights)
                    .append("\n  Frame Rates: ").append(videoCaps.supportedFrameRates)
                    .append("\n  Bitrate: ").append(videoCaps.bitrateRange)
                if (ScreenRecorder.VIDEO_AVC == mimeType) {
                    val levels = caps.profileLevels
                    builder.append("\n  Profile-levels: ")
                    for (level in levels) {
                        builder.append("\n  ")
                            .append(Utils.avcProfileLevelToString(level))
                    }
                }
                builder.append("\n  Color-formats: ")
                for (c in caps.colorFormats) {
                    builder.append("\n  ")
                        .append(Utils.toHumanReadable(c))
                }
            }
            val audioCaps = caps.audioCapabilities
            if (audioCaps != null) {
                builder.append("\n Audio capabilities:")
                    .append("\n Sample Rates: ")
                    .append(Arrays.toString(audioCaps.supportedSampleRates))
                    .append("\n Bit Rates: ").append(audioCaps.bitrateRange)
                    .append("\n Max channels: ").append(audioCaps.maxInputChannelCount)
            }
            Log.i(TAG, builder.toString())
        }
    }

    @SuppressLint("InflateParams")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //startForeground(1361, createNotification(this))

        mNotifications = Notifications(this)
        if (!isViewAddToWindomanager) {
            Utils.findEncodersByTypeAsync(
                ScreenRecorder.VIDEO_AVC,
                Utils.Callback { info_s: Array<MediaCodecInfo> ->
                    logCodecInfos(
                        info_s,
                        ScreenRecorder.VIDEO_AVC
                    )
                    mAvcCodecInfos = info_s
                }
            )
            Utils.findEncodersByTypeAsync(
                ScreenRecorder.AUDIO_AAC,
                Utils.Callback { infos: Array<MediaCodecInfo> ->
                    logCodecInfos(
                        infos,
                        ScreenRecorder.AUDIO_AAC
                    )
                    mAacCodecInfos = infos
                }
            )


            initFloatingView(intent)
            initCircularMenu()
            createNotification(this)
            isViewAddToWindomanager = true

        } else {
            closeOpenCircleMenu()
        }
        return START_STICKY
    }

    private fun initFloatingView(intent: Intent) {
        windowManger.defaultDisplay.getMetrics(metrics)
        val inflater = LayoutInflater.from(this)
        actionButton =
            inflater.inflate(R.layout.floating_action_button, null, false) as AppCompatImageView
        actionButton?.setOnClickListener {
            closeOpenCircleMenu()
        }


        val rect = intent.getParcelableExtra<Rect>(EXTRA_CUTOUT_SAFE_AREA)
        floatingViewManager = FloatingViewManager(this, this).apply {
            setFixedTrashIconImage(R.drawable.ic_trash_fixed)
            setActionTrashIconImage(R.drawable.ic_trash_action)
            setSafeInsetRect(rect)

            val options = FloatingViewManager.Options().apply {
                moveDirection = FloatingViewManager.MOVE_DIRECTION_THROWN
                overMargin = this@FloatingCircularMenuService.overMargin
                floatingViewX = metrics.widthPixels - actionButtonSize - radius + overMargin
                floatingViewY = (metrics.heightPixels - actionButtonSize) / 2
                isTrashViewEnabled = false
                setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS)
            }
            actionButton?.let {
                addViewToWindow(it, options)
            }
        }
    }

    override fun onDestroy() {
        destroy()
        super.onDestroy()
    }

    private val mAudioToggle = true

    private fun createAudioConfig(): AudioEncodeConfig {
        val samplerate: Int = 48000
        val channelCount: Int = 1
        val profile: Int = 0

        mAacCodecInfos =
            Utils.findEncodersByType(ScreenRecorder.AUDIO_AAC)

        return AudioEncodeConfig(
            mAacCodecInfos[0].name,
            ScreenRecorder.AUDIO_AAC,
            2000000,
            samplerate,
            channelCount,
            profile
        )
    }

    private fun createVideoConfig(): VideoEncodeConfig {//Default
//        val codec: String = getSelectedVideoCodec()
//            ?: // no selected codec ??
//            return null
//        // video size
//        val selectedWithHeight: IntArray = getSelectedWithHeight()
//        val isLandscape: Boolean = isLandscape()
//        val width = selectedWithHeight[if (isLandscape) 0 else 1]
//        val height = selectedWithHeight[if (isLandscape) 1 else 0]
//        val framerate: Int = getSelectedFramerate()
//        val iframe: Int = getSelectedIFrameInterval()
//        val bitrate: Int = getSelectedVideoBitrate()
//        val profileLevel: CodecProfileLevel = getSelectedProfileLevel()

        mAvcCodecInfos =
            Utils.findEncodersByType(ScreenRecorder.VIDEO_AVC)

        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)


        val capabilities: CodecCapabilities =
            mAvcCodecInfos[0].getCapabilitiesForType(ScreenRecorder.VIDEO_AVC)

        val profiles = capabilities.profileLevels
        val profileLevels =
            arrayOfNulls<String>(profiles.size + 1)
        profileLevels[0] = "Default"

        for (i in profiles.indices) {
            profileLevels[i + 1] =
                Utils.avcProfileLevelToString(profiles[i])
        }


        val display = windowManger.defaultDisplay
        var size = Point()
        display.getSize(size)

        return VideoEncodeConfig(
            size.x,
            size.y,
            4000000,
            metrics.densityDpi,
            30,
            10,
            mAvcCodecInfos[0].name,
            ScreenRecorder.VIDEO_AVC,
            Utils.toProfileLevel(profileLevels[0])
        )
    }

    private fun toast(message: String, vararg args: Any) {
        val length_toast =
            if (Locale.getDefault().country == "BR") Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        // In Brazilian Portuguese this may take longer to read
        val toast = Toast.makeText(
            this,
            if (args.isEmpty()) message else String.format(
                Locale.US,
                message,
                *args
            ),
            length_toast
        )
        toast.show()
    }

    private fun getSavingDir(): File? {
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "Screenshots"
        )
    }

    fun startCapturing() {

        val mediaProjection = ScreenApp.getMediaProjection()

        mediaProjection?.registerCallback(mProjectionCallback,null);

        val video: VideoEncodeConfig = createVideoConfig()
        val audio: AudioEncodeConfig = createAudioConfig() // audio can be null

        val dir = getSavingDir()

        if (!dir!!.exists() && !dir.mkdirs()) {
            //cancelRecorder()
            Log.e(TAG, "Create recorder with [dir!!.exists()]:$video")

            return
        }
        val format =
            SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
        val file = File(
            dir, "Screenshots-" + format.format(Date())
                    + "-" + video.width + "x" + video.height + ".mp4"
        )
        Log.e(TAG, "Create recorder with :$video \n \n $file")
        mVideoRecorder = newRecorder(mediaProjection, video, audio, file)
        mVideoRecorder!!.start()
    }

    private fun getOrCreateVirtualDisplay(
        mediaProjection: MediaProjection,
        config: VideoEncodeConfig
    ): VirtualDisplay? {
        if (mVirtualDisplay == null) {
            mVirtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenRecorder-display0",
                config.width, config.height, config.dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                null /*surface*/, null, null
            )
        } else { // resize if size not matched
            val size = Point()
            mVirtualDisplay!!.display.getSize(size)
            if (size.x != config.width || size.y != config.height) {
                mVirtualDisplay!!.resize(config.width, config.height, config.dpi)
            }
        }
        return mVirtualDisplay
    }

    private fun newRecorder(
        mediaProjection: MediaProjection, video: VideoEncodeConfig,
        audio: AudioEncodeConfig?, output: File
    ): ScreenRecorder {
        val display: VirtualDisplay? = getOrCreateVirtualDisplay(mediaProjection, video)
        val r = ScreenRecorder(video, audio, display, output.absolutePath)

        r.setCallback(object : ScreenRecorder.Callback {
            var startTime: Long = 0
            override fun onStop(error: Throwable) {
                // runOnUiThread(Runnable { stopRecorder() })

                if (error != null) {
                    toast("Recorder error ! See logcat for more details")
                    error.printStackTrace()
                    output.delete()
                } else {
                    val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                        .addCategory(Intent.CATEGORY_DEFAULT)
                        .setData(Uri.fromFile(output))
                    sendBroadcast(intent)
                }
            }

            override fun onStart() {
                mNotifications!!.recording(0)
            }

            override fun onRecording(presentationTimeUs: Long) {
                if (startTime <= 0) {
                    startTime = presentationTimeUs
                }
                val time = (presentationTimeUs - startTime) / 1000
                mNotifications!!.recording(time)
            }
        })
        return r
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    private fun initCircularMenu() {

        //Init Git Repo as Private Mode.
        registerReceiver(mStopActionReceiver, IntentFilter(ACTION_STOP))

        val inflater = LayoutInflater.from(this)

        circularMenu =
            inflater.inflate(R.layout.floating_action_circle_menu, null, false) as ConstraintLayout

        constraintSetLTR.apply {
            clone(circularMenu)
            connect(
                R.id.btnClose,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            connect(R.id.btnClose, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(
                R.id.btnClose,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
        }

        constraintSetRTL.apply {
            clone(circularMenu)
            connect(R.id.btnClose, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            connect(R.id.btnClose, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(
                R.id.btnClose,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
        }

        flatAppBtn = circularMenu.findViewById(R.id.btnClose)
        toolsBtn = circularMenu.findViewById(R.id.tools_btn)
        startRecordBtn = circularMenu.findViewById(R.id.start_record_btn)
        settingsBtn = circularMenu.findViewById(R.id.settings_btn)
        homeBtn = circularMenu.findViewById(R.id.home_btn)

        flatAppBtn.setOnClickListener {
            closeOpenCircleMenu()
        }

        toolsBtn.setOnClickListener {
            Toast.makeText(it.context, "Exit the app", Toast.LENGTH_LONG).show()
            flatAppBtn.callOnClick()
            onFinishFloatingView()
        }

        startRecordBtn.setOnClickListener {
            ScreenApp.getScreenshotPermission();
            //ScreenApp.getHandler().sendEmptyMessage(Constants.CREATE_SCREEN_CAPTURE_INTENT)
            flatAppBtn.callOnClick()
        }

        settingsBtn.setOnClickListener {
            Toast.makeText(it.context, "button4", Toast.LENGTH_LONG).show()

            //stopRecorder()

            flatAppBtn.callOnClick()
        }

        homeBtn.setOnClickListener {
            Toast.makeText(it.context, "stop recording and open the file", Toast.LENGTH_LONG).show()

            //mNotifications?.stopAction()

            flatAppBtn.callOnClick()

            stopRecordingAndOpenFile(applicationContext)
        }

        val gesture = object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                closeOpenCircleMenu()
                return super.onSingleTapConfirmed(e)
            }
        }

        gestureDetector = GestureDetector(this, gesture)

        circularMenu.setOnTouchListener(FloatingButtonTouchListener())

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PRIORITY_PHONE
        }

        circularMenuParams = WindowManager.LayoutParams(
            /*radius + actionButtonSize*/ViewGroup.LayoutParams.MATCH_PARENT,
            /*2 * radius + actionButtonSize*/ViewGroup.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    //or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSPARENT
        )

        circularMenuParams!!.gravity = Gravity.TOP or Gravity.START
    }


    private fun closeOpenCircleMenu() {

        if (isHideCircularMenu) {

            actionButton?.let {
                it.animate().apply {
                    cancel()
                }.alpha(0f)
                    .setDuration(0L)
                    .setStartDelay(0L)
                    .start()

                circularMenuParams!!.y =
                    yPosition + (it.height - circularMenuParams!!.height) / 2
            }

            isHideCircularMenu = false

            circularMenuParams!!.x = if (!isMoveToEdge) {
                if (isRTL()) xPosition - circularMenuParams!!.width + actionButtonSize else xPosition
            } else {
                if (isRTL()) getScreenWidth() - circularMenuParams!!.width + overMargin else -overMargin
            }



            if (isRTL()) {
                constraintSetRTL.clear(R.id.btnClose, ConstraintSet.START)
                constraintSetRTL.applyTo(circularMenu)
            } else {
                constraintSetLTR.clear(R.id.btnClose, ConstraintSet.END)
                constraintSetLTR.applyTo(circularMenu)
            }

            if (!ViewCompat.isAttachedToWindow(circularMenu)) {
                windowManger.addView(circularMenu, circularMenuParams)
            } else {
                circularMenu.isGone = false
                windowManger.updateViewLayout(circularMenu, circularMenuParams)
            }
        } else {
            isHideCircularMenu = true
        }

        val angle = if (isHideCircularMenu) 0f else 45f
        flatAppBtn.animate().apply {
            cancel()
        }
            .rotation(angle)
            .setDuration(100L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withStartAction {
                val count = circularMenu.childCount - 1
                circularMenu.forEachIndexed { index, view ->
                    if (view.id != R.id.btnClose) {
                        if (!isHideCircularMenu) view.isGone = isHideCircularMenu
                        view.side(isHideCircularMenu, radius, isRTL(), index == count - 1)
                    }
                }
            }
            .withEndAction {
                if (isHideCircularMenu) {
                    actionButton?.let {
                        it.animate().apply {
                            cancel()
                        }.alpha(1f)
                            .setDuration(100L)
                            .setStartDelay(0L)
                            .withEndAction {
                                it.animate().alpha(0.25f).setDuration(3000L).start()
                            }
                            .start()

                    }
                }
            }
            .start()
    }

    private fun View.side(
        isGone: Boolean,
        radius: Int,
        isRTL: Boolean = false,
        isLastItem: Boolean = false
    ) {
        val layoutParams = this.layoutParams as ConstraintLayout.LayoutParams
        val angle = layoutParams.circleAngle.toDouble()
        var radian = Math.toRadians(angle)
        if (isRTL) radian = -radian
        var from = 0
        var to = radius
        if (isGone) {
            from = radius
            to = 0
        }
        ValueAnimator.ofInt(from, to).apply {
            duration = 200L
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val animatedValue = it.animatedValue as Int
                val x = animatedValue * sin(radian)
                val y = animatedValue * cos(radian)
                this@side.translationX = x.toFloat()
                this@side.translationY = y.toFloat()
            }
            doOnEnd {
                if (isGone) {
                    this@side.isGone = isGone
                    if (isLastItem) {
                        /*if (isHideCircularMenu) {
                            actionButton.animate().apply {
                                cancel()
                            }.alpha(1f)
                            .setStartDelay(0L)
                            .setStartDelay(0L)
                            .withEndAction {
                                actionButton.animate().alpha(0.45f).setDuration(3000L).start()
                            }
                            .start()
                        }*/
                        circularMenu.isGone = true
                    }
                }
            }
        }.start()
    }

    private fun isRTL() = xPosition > metrics.widthPixels / 2

    private fun getScreenWidth() =
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            metrics.heightPixels
        else
            metrics.widthPixels

    private inner class FloatingButtonTouchListener : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            gestureDetector.onTouchEvent(event)
            when (event.actionMasked) {
                MotionEvent.ACTION_OUTSIDE -> {
                    if (!circularMenu.isGone) {
                        flatAppBtn.callOnClick()
                    }
                }
            }
            return true
        }
    }

    override fun onFinishFloatingView() {
        stopSelf()
    }

    /**
     * {@inheritDoc}
     */
    override fun onTouchFinished(isFinishing: Boolean, x: Int, y: Int) {
        if (isFinishing) {
            Log.d(TAG, getString(R.string.deleted_soon))
        } else {
            xPosition = x
            yPosition = y
            Log.d(TAG, getString(R.string.touch_finished_position, x, y))
        }
    }

    /**
     * Viewを破棄します。
     */
    private fun destroy() {

        stopRecorder()

        mVirtualDisplay?.apply {
            surface = null
            release()
        }
        mVirtualDisplay = null

//        mMediaProjection?.apply {
//            unregisterCallback(mProjectionCallback)
//            stop()
//        }
//        mMediaProjection = null

        floatingViewManager?.removeAllViewToWindow()
        try {
            windowManger.removeView(circularMenu)
        } catch (e: IllegalArgumentException) {
        }
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

    private fun stopRecorder() {
        mNotifications?.clear()

        mVideoRecorder?.quit()
        mVideoRecorder = null
        try {
            unregisterReceiver(mStopActionReceiver)
        } catch (e: Exception) {
            //ignored
        }
    }

    private fun viewResult(file: File) {
        val view = Intent(Intent.ACTION_VIEW)
        view.addCategory(Intent.CATEGORY_DEFAULT)
        view.setDataAndType(Uri.fromFile(file), ScreenRecorder.VIDEO_AVC)
        view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(view)
        } catch (e: ActivityNotFoundException) { // no activity can open this video
        }
    }

    private fun stopRecordingAndOpenFile(context: Context) {
        val file = File(mVideoRecorder!!.savedPath)
        stopRecorder()
        Toast.makeText(
            context,
            getString(R.string.recorder_stopped_saved_file) + " " + file,
            Toast.LENGTH_LONG
        ).show()
        val vmPolicy = StrictMode.getVmPolicy()
        try { // disable detecting FileUriExposure on public file
            StrictMode.setVmPolicy(VmPolicy.Builder().build())
            viewResult(file)
        } finally {
            StrictMode.setVmPolicy(vmPolicy)
        }
    }

    val ACTION_STOP: String = BuildConfig.APPLICATION_ID + ".action.STOP"

    private val mStopActionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_STOP == intent.action) {
                stopRecordingAndOpenFile(context)
            }
        }
    }

    private fun cancelRecorder() {
        if (mVideoRecorder == null) return
        Toast.makeText(
            this,
            getString(R.string.permission_denied_screen_recorder_cancel),
            Toast.LENGTH_SHORT
        ).show()
        stopRecorder()
    }

    companion object {

        val TAG: String = FloatingCircularMenuService::class.java.simpleName

        @JvmStatic
        private var isViewAddToWindomanager = false

        @JvmStatic
        val mProjectionCallback: MediaProjection.Callback =
            object : MediaProjection.Callback() {
                override fun onStop() {
//                    if (mRecorder != null) {
//                        stopRecorder()
//                    }
                }
            }

        /**
         * Intent key (Cutout safe area)
         */
        const val EXTRA_CUTOUT_SAFE_AREA = "cutout_safe_area"
    }
}
