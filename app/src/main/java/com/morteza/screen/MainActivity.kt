package com.morteza.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.morteza.screen.common.ActivityExtension.REQUEST_MEDIA_PROJECTION
import com.morteza.screen.common.BaseActivity
import com.morteza.screen.common.Constants.createScreenCaptureIntent
import com.morteza.screen.common.askPermission
import com.morteza.screen.common.hasPermissions
import com.morteza.screen.common.requestPermissionsEX
import com.morteza.screen.ui.floatingcircularmenu.FloatingCircularMenuService
import com.morteza.screen.ui.floatingcircularmenu.FloatingCircularMenuService.Companion.mMediaProjection
import com.morteza.screen.ui.floatingcircularmenu.FloatingCircularMenuService.Companion.mMediaProjectionManager
import com.morteza.screen.ui.floatingcircularmenu.FloatingCircularMenuService.Companion.mProjectionCallback
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager

/**
 * @author Morteza
 * @version 2019/12/3
 */
class MainActivity : BaseActivity(), Handler.Callback {

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            data?.apply {
                val mediaProjection: MediaProjection? =
                    mMediaProjectionManager?.getMediaProjection(resultCode, data)
                if (mediaProjection == null) {
                    Log.e("@@", "media projection is null")
                    return
                }
                mMediaProjection = mediaProjection
            }
            mMediaProjection?.registerCallback(mProjectionCallback, Handler())
            startFloatingViewService(this)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main)
        ScreenApp.setHandler(Handler(Looper.getMainLooper(), this))

        if (!hasPermissions()) {
            requestPermissionsEX()
            askPermission()
        }
        mMediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager


    }

    private fun startFloatingViewService(activity: FragmentActivity) {

        // *** You must follow these rules when obtain the cutout(FloatingViewManager.findCutoutSafeArea) ***
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 1. 'windowLayoutInDisplayCutoutMode' do not be set to 'never'
            if (activity.window.attributes.layoutInDisplayCutoutMode == WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS) {
                throw RuntimeException("'windowLayoutInDisplayCutoutMode' do not be set to 'never'")
            }
            // 2. Do not set Activity to landscape
            if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                throw RuntimeException("Do not set Activity to landscape")
            }
        }
        // launch service
        val service = FloatingCircularMenuService::class.java
        val key: String = FloatingCircularMenuService.EXTRA_CUTOUT_SAFE_AREA
        val intent = Intent(activity, service).putExtra(
            key,
            FloatingViewManager.findCutoutSafeArea(activity)
        )
        activity.startService(intent)
        moveTaskToBack(true);
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            createScreenCaptureIntent -> {
                val captureIntent = mMediaProjectionManager!!.createScreenCaptureIntent()
                startActivityForResult(
                    captureIntent,
                    REQUEST_MEDIA_PROJECTION
                )
            }
        }

        return true
    }

//    private val intentFilter = IntentFilter(Constants.ACTION_SCREEN_CAPTURE_INTENT)
//    private val mEventBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//
//        }
//    }
}


