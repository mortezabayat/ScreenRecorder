package com.morteza.screen.services.helper

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.morteza.screen.common.Constants
/**
 * @author Morteza
 * @version 2019/12/3
 */
class IncomingHandler(private val callback: IncomingHandlerCallback) :
    Handler(Looper.getMainLooper()) {

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            Constants.START_VIDEO_RECORDER -> {
                callback.doWork(Constants.START_VIDEO_RECORDER);
            }
            else -> super.handleMessage(msg)
        }
    }

    interface IncomingHandlerCallback {
        fun doWork(msg: Int)
    }
}