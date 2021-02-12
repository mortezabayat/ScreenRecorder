package com.morteza.screen.services.helper

object FloatingUiHelperInterface {//ScreenRecorder.Callback

    interface FloatingUiControl {
        fun stopRecording()
        fun pauseRecording()
        fun onFinishFloatingView()
    }

    interface ServiceCallback {
        fun stopService()
    }
}