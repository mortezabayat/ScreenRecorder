package com.morteza.screen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Message;
import android.os.RemoteException;

import com.morteza.screen.common.BaseApplication;
import com.morteza.screen.common.Constants;
import com.morteza.screen.services.FloatingCircularMenuService;

import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

public class ScreenApp extends BaseApplication {


    public void sendObjectToFlattingMenuService() {
        if (!boundFlattingMenuService) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, Constants.START_VIDEO_RECORDER, 0, 0);
        try {
            mFlattingMenuService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void startFloatingViewService(Activity activity) {
        Intent intent = new Intent(this, FloatingCircularMenuService.class);
        intent.putExtra(Constants.EXTRA_CUTOUT_SAFE_AREA,
                FloatingViewManager.Companion.findCutoutSafeArea(activity));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        // Bind to the service
        bindService(intent, mFlattingMenuServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onTerminate() {
        // Unbind from the service
        if (boundFlattingMenuService) {
            unbindService(mFlattingMenuServiceConnection);
            boundFlattingMenuService = false;
        }
        super.onTerminate();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        __Instance = this;
    }

}
