package com.morteza.screen;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

class ScreenApp extends Application {

    private static Handler mHandler;
    private static ScreenApp __Instance = null;
    private LocalBroadcastManager mLocalBroadcastManager;


    public void registerLocalReceiver(@NonNull BroadcastReceiver receiver,
                                      @NonNull IntentFilter filter) {
        mLocalBroadcastManager.registerReceiver(receiver, filter);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        __Instance = this;
    }

    public static void setHandler(Handler handler) {
        mHandler = handler;
    }

    public static Handler getHandler() {
        return mHandler;
    }

    public static ScreenApp getInstance() {
        return __Instance;
    }
}
