package com.morteza.screen.tools

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.projection.MediaProjection
import android.net.Uri
import android.os.Environment
import android.os.StrictMode
import com.morteza.screen.R
import com.morteza.screen.ScreenApp
import com.morteza.screen.common.Constants.VIDEO_OUT_DIR_NAME
import com.morteza.screen.common.toast
import com.morteza.screen.services.helper.FloatingUiHelper
import com.morteza.screen.services.helper.FloatingUiHelperInterface
import com.morteza.screen.tools.recorder.AudioEncodeConfig
import com.morteza.screen.tools.recorder.Notifications
import com.morteza.screen.tools.recorder.ScreenRecorder
import com.morteza.screen.tools.recorder.VideoEncodeConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ScreenRecorderManager(
    private val mContext: Context,
    private val callbacks: FloatingUiHelperInterface.ServiceCallback
) : FloatingUiHelperInterface.FloatingUiControl, ScreenRecorder.Callback {

    private val TAG = "ScreenRecorderManager"
    private val mAudioToggle = true
    private var mVideoPath: File? = null
    private var mStartTime: Long = 0

    private var mNotifications: Notifications? = null
    private val mFloatingUiHelper by lazy { FloatingUiHelper(mContext, this) }

    private var mVideoRecorder: ScreenRecorder? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mAvcCodecInfoList: Array<MediaCodecInfo> = emptyArray()
    private var mAacCodecInfoList: Array<MediaCodecInfo> = emptyArray()
    private val mProjectionCallback: MediaProjection.Callback =
        object : MediaProjection.Callback() {
            override fun onStop() {
//                    if (mRecorder != null) {
//                        stopRecorder()
//                    }
            }
        }

    fun initializing(intent: Intent) {

        mNotifications = Notifications(mContext)

        if (!mFloatingUiHelper.mIsViewAddToWindowManager) {
            Utils.findEncodersByTypeAsync(ScreenRecorder.VIDEO_AVC) {
                Utils.logCodecInfoList(it, ScreenRecorder.VIDEO_AVC, TAG)
                mAvcCodecInfoList = it
            }
            Utils.findEncodersByTypeAsync(ScreenRecorder.AUDIO_AAC) {
                Utils.logCodecInfoList(it, ScreenRecorder.AUDIO_AAC, TAG)
                mAacCodecInfoList = it
            }
            mFloatingUiHelper.initFloatingView(intent)
        } else {
            mFloatingUiHelper.closeOpenCircleMenu()
        }
    }


    fun destroyView() {
        destroy()
        mFloatingUiHelper.destroy()
    }

    private fun destroy() {
        stopRecorder()
        mVirtualDisplay?.apply {
            surface = null
            release()
        }
        mVirtualDisplay = null
        ScreenApp.stopMediaProjection(mProjectionCallback)
    }

    private fun createAudioConfig(): AudioEncodeConfig {
        val sampleRate = 44100
        val channelCount: Int = 1
        val profile: Int = 0

        mAacCodecInfoList =
            Utils.findEncodersByType(ScreenRecorder.AUDIO_AAC)

        return AudioEncodeConfig(
            mAacCodecInfoList[0].name,
            ScreenRecorder.AUDIO_AAC,
            124000,
            sampleRate,
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

        mAvcCodecInfoList =
            Utils.findEncodersByType(ScreenRecorder.VIDEO_AVC)

        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)


        val capabilities: MediaCodecInfo.CodecCapabilities =
            mAvcCodecInfoList[0].getCapabilitiesForType(ScreenRecorder.VIDEO_AVC)

        val profiles = capabilities.profileLevels
        val profileLevels =
            arrayOfNulls<String>(profiles.size + 1)
        profileLevels[0] = "Default"

        for (i in profiles.indices) {
            profileLevels[i + 1] =
                Utils.avcProfileLevelToString(profiles[i])
        }

        return VideoEncodeConfig(
            mFloatingUiHelper.mDisplayInfo.x,
            mFloatingUiHelper.mDisplayInfo.y,//1520
            4000000,
            mFloatingUiHelper.mDisplayInfo.metrics.densityDpi,
            30,
            10,
            mAvcCodecInfoList[0].name,
            ScreenRecorder.VIDEO_AVC,
            Utils.toProfileLevel(profileLevels[0])
        )
    }

    fun startCapturing() {

        val mediaProjection = ScreenApp.getMediaProjection()

        mediaProjection?.registerCallback(mProjectionCallback, null)

        val video: VideoEncodeConfig = createVideoConfig()
        val audio: AudioEncodeConfig = createAudioConfig() // audio can be null
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            VIDEO_OUT_DIR_NAME
        )

        if (!dir.exists() && !dir.mkdirs()) {
            //Todo Con't create File path
            cancelRecorder()
            return;
        }
        val format = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
        mVideoPath = File(
            dir, VIDEO_OUT_DIR_NAME + "-" + format.format(Date())
                    + "-" + video.width + "x" + video.height + ".mp4"
        )
        mVideoRecorder = newRecorder(mediaProjection, video, audio)
        mVideoRecorder?.start()
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
        audio: AudioEncodeConfig?
    ): ScreenRecorder? {
        val display: VirtualDisplay? = getOrCreateVirtualDisplay(mediaProjection, video)
        mVideoPath?.let {
            val r = ScreenRecorder(video, audio, display, it.absolutePath)
            r.setCallback(this)
            return r
        }
        return null
    }

    private fun cancelRecorder() {
        if (mVideoRecorder == null) return
        mContext.toast(mContext.getString(R.string.permission_denied_screen_recorder_cancel))
        stopRecorder()
    }

    private fun viewResult(file: File) {
        val view = Intent(Intent.ACTION_VIEW)
        view.addCategory(Intent.CATEGORY_DEFAULT)
        view.setDataAndType(Uri.fromFile(file), ScreenRecorder.VIDEO_AVC)
        view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            mContext.startActivity(view)
        } catch (e: ActivityNotFoundException) { // no activity can open this video
        }
    }

    override fun onFinishFloatingView() {
        callbacks.stopService()
    }

    override fun stopRecording() {

        ScreenApp.getMediaProjection()?.apply {
            unregisterCallback(mProjectionCallback)
            stop()
        }
        mContext.toast(mContext.getString(R.string.recorder_stopped_saved_file) + " " + mVideoPath)

        val vmPolicy = StrictMode.getVmPolicy()
        try { // disable detecting FileUriExposure on public file
            mVideoPath?.let {
                StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
                viewResult(it)
            }
        } finally {
            StrictMode.setVmPolicy(vmPolicy)
        }
        destroy()
    }

    override fun pauseRecording() {
        //TODO("Not yet implemented")
        mContext.toast("We Not Support This Option Now ... ")
    }

    private fun stopRecorder() {
        mFloatingUiHelper.mHaveActiveRecording = false
        mVideoRecorder?.quit()
        mVideoRecorder = null
        mNotifications?.clear()
        mFloatingUiHelper.unregisterReceiver()
    }

    override fun onStop(error: Throwable?) {
        mStartTime = 0
        mNotifications?.clear()

        if (error != null) {
            mContext.toast("Recorder error ! See logcat for more details")
            error.printStackTrace()
            mVideoPath?.delete()
        } else {
            mVideoPath?.let {
                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .setData(Uri.fromFile(it))
                mContext.sendBroadcast(intent)
            }
        }
    }

    override fun onStart() {
        mFloatingUiHelper.mHaveActiveRecording = true
        mNotifications?.stopAction()
        mStartTime = 0
        mNotifications!!.recording(mStartTime)
    }

    override fun onRecording(presentationTimeUs: Long) {
        if (mStartTime <= 0) {
            mStartTime = presentationTimeUs
        }
        val time = (presentationTimeUs - mStartTime) / 1000
        mNotifications!!.recording(time)
    }
}