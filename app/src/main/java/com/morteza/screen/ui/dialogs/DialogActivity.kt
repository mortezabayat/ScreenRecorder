package com.morteza.screen.ui.dialogs

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import com.morteza.screen.R
import com.morteza.screen.common.BaseActivity
import com.morteza.screen.common.BaseFragment
import com.morteza.screen.common.Constants
import com.morteza.screen.ui.previewsvideo.PreviewsVideo

class DialogActivity : BaseActivity() {

    private fun startFragment(f: BaseFragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, f, f.getFragmentName())
            .commitAllowingStateLoss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)

        setFinishOnTouchOutside(false)
//        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        val videoPath = intent.extras?.getString(Constants.VIDEO_PATH)

        if (VERBOSE) {
            Log.e(
                TAG(), "Bundle Info ${intent.extras?.getString(Constants.CURRENT_UI)} \n $videoPath"
            )
        }
        videoPath?.let {
            startFragment(PreviewsVideo.newInstance(videoPath))
        }
    }

    override fun TAG() = "DialogActivity"
}