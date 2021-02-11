package com.morteza.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.*
import android.view.Menu
import android.view.WindowManager
import com.morteza.screen.common.BaseActivity
import com.morteza.screen.common.Constants.*
import com.morteza.screen.services.FloatingCircularMenuService
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager

/**
 * @author Morteza
 * @version 2019/12/3
 */

@Deprecated("This Activity Will Be Removed ...")
class MainActivity : BaseActivity(), Handler.Callback {

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(rCode: Int, resultCode: Int, data: Intent?) {
//        if (rCode == REQUEST_MEDIA_PROJECTION) {
//            data?.apply {
//                val mediaProjection: MediaProjection? =
//                    mMediaProjectionManager?.getMediaProjection(resultCode, data)
//                if (mediaProjection == null) {
//                    Log.e("@@", "media projection is null")
//                    return
//                }
//                mMediaProjection = mediaProjection
//            }
//            mMediaProjection?.registerCallback(mProjectionCallback, ScreenApp.getHandler())
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // *** You must follow these rules when obtain the cutout(FloatingViewManager.findCutoutSafeArea) ***
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 1. 'windowLayoutInDisplayCutoutMode' do not be set to 'never'
            if (window.attributes.layoutInDisplayCutoutMode == WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS) {
                throw RuntimeException("'windowLayoutInDisplayCutoutMode' do not be set to 'never'")
            }
            // 2. Do not set Activity to landscape
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                throw RuntimeException("Do not set Activity to landscape")
            }
        }
        ScreenApp.setHandler(Handler(Looper.getMainLooper(), this))

//        if (!hasPermissions()) {
//            requestPermissionsEX()
//            askOverlayPermission()
//        }

//        mMediaProjectionManager =
//            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        ScreenApp.getHandler().sendEmptyMessage(START_FLOATING_VIEW_SERVICE)

//        ScreenApp.getHandler().sendEmptyMessageDelayed(MOVE_TO_BACKGROUND, 0)
    }

    private fun startFloatingViewService() {


        // launch service
        val service = FloatingCircularMenuService::class.java
        val key: String = EXTRA_CUTOUT_SAFE_AREA
        val intent = Intent(this, service).putExtra(
            key,
            FloatingViewManager.findCutoutSafeArea(this)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            CREATE_SCREEN_CAPTURE_INTENT -> {
//                mMediaProjectionManager?.let {
//                    val captureIntent = it.createScreenCaptureIntent()
//                    this.startActivityForResult(captureIntent, REQUEST_MEDIA_PROJECTION)
//                }
            }
            MOVE_TO_BACKGROUND -> {
                moveTaskToBack(true);
            }
            MOVE_TO_FOREGROUND -> {
                //Todo start activity back
            }
            START_FLOATING_VIEW_SERVICE -> {
                startFloatingViewService()
            }
        }

        return true
    }
}


