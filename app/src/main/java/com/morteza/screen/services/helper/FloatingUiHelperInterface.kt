package com.morteza.screen.services.helper

object FloatingUiHelperInterface {//ScreenRecorder.Callback

    interface FloatingUiControl {
        fun stopRecording()
        fun onFinishFloatingView()
    }

    interface ServiceCallback {
        fun stopService()
    }
}