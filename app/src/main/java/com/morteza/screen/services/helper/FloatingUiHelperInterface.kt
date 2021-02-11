package com.morteza.screen.services.helper

import android.content.Context
import com.morteza.screen.tools.recorder.ScreenRecorder

object FloatingUiHelperInterface {//ScreenRecorder.Callback

    interface FloatingUiControl{
        fun stopRecording()
        fun onFinishFloatingView()
    }

    interface ScreenRecorderControl : ScreenRecorder.Callback{
        fun stopRecorder()

    }
}