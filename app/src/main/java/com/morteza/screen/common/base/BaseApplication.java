package com.morteza.screen.common.base;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.morteza.screen.ScreenApp;
import com.morteza.screen.SplashActivity;
import com.morteza.screen.common.Constants;

public abstract class BaseApplication extends Application {
    protected static Handler mHandler;
    protected static ScreenApp __Instance = null;
    protected static Intent screenshotPermission = null;
    protected static MediaProjection mMediaProjection = null;
    protected static MediaProjectionManager mMediaProjectionManager = null;

    /**
     * Messenger for communicating with the service.
     */
    protected Messenger mFlattingMenuService = null;
    /**
     * Flag indicating whether we have called bind on the service.
     */
    protected boolean boundFlattingMenuService;
    /**
     * Class for interacting with the main interface of the service.
     */
    protected final ServiceConnection mFlattingMenuServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mFlattingMenuService = new Messenger(service);
            boundFlattingMenuService = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mFlattingMenuService = null;
            boundFlattingMenuService = false;
        }
    };


    public static void stopMediaProjection(MediaProjection.Callback callback) {
        if (mMediaProjection != null) {
            if (callback != null) {
                mMediaProjection.unregisterCallback(callback);
            }
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    public static void setHandler(Handler handler) {
        mHandler = handler;
    }

    public static void getScreenshotPermission() {
        openScreenshotPermissionRequester();
    }

    public static void setScreenshotPermission(final Intent permissionIntent) {
        ScreenApp.screenshotPermission = permissionIntent;
        stopMediaProjection(null);

        if (permissionIntent != null) {
            mMediaProjection = mMediaProjectionManager.
                    getMediaProjection(Activity.RESULT_OK, (Intent) screenshotPermission.clone());
            Log.e("FloatingCircularMenu", "mMediaProjectionManager is " + (mMediaProjection == null));
        }
    }

    protected static void openScreenshotPermissionRequester() {
        final Intent mSplashActivityIntent =
                new Intent(getInstance().getApplicationContext(),
                        SplashActivity.class);
        mSplashActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mSplashActivityIntent.putExtra(Constants.SEND_REQUEST_MEDIA_PROJECTION, true);
        getInstance().getApplicationContext().startActivity(mSplashActivityIntent);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    public static MediaProjection getMediaProjection() {
        return mMediaProjection;
    }

    public static Handler getHandler() {
        return mHandler;
    }

    public static ScreenApp getInstance() {
        return __Instance;
    }

    public static MediaProjectionManager getMediaProjectionManager() {
        return mMediaProjectionManager;
    }

}
