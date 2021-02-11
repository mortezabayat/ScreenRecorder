package com.morteza.screen;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.morteza.screen.common.Constants;
import com.morteza.screen.services.FloatingCircularMenuService;

import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

public class ScreenApp extends Application {

    private static Handler mHandler;
    private static ScreenApp __Instance = null;
    private LocalBroadcastManager mLocalBroadcastManager;
    private static Intent screenshotPermission = null;
    private static MediaProjection mMediaProjection = null;
    private static MediaProjectionManager mMediaProjectionManager = null;
    /**
     * Messenger for communicating with the service.
     */
    Messenger mFlattingMenuService = null;
    /**
     * Flag indicating whether we have called bind on the service.
     */
    boolean boundFlattingMenuService;
    /**
     * Class for interacting with the main interface of the service.
     */
    private final ServiceConnection mFlattingMenuServiceConnection = new ServiceConnection() {
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

    public static MediaProjectionManager getMediaProjectionManager() {
        return mMediaProjectionManager;
    }

    public static void getScreenshotPermission() {
        openScreenshotPermissionRequester();
    }

    protected static void openScreenshotPermissionRequester() {
        final Intent intent =
                new Intent(getInstance().getApplicationContext(),
                        SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.SEND_REQUEST_MEDIA_PROJECTION, true);
        getInstance().getApplicationContext().startActivity(intent);
    }

    public static void setScreenshotPermission(final Intent permissionIntent) {
        ScreenApp.screenshotPermission = permissionIntent;
        stopMediaProjection(null);

        if (permissionIntent != null) {
            mMediaProjection = mMediaProjectionManager.
                    getMediaProjection(Activity.RESULT_OK, (Intent) screenshotPermission.clone());
            Log.e("FloatingCircularMenu", "mMediaProjectionManager is " + (mMediaProjection == null));
            getInstance().sendObjectToFlattingMenuService();
        }
    }

    public static MediaProjection getMediaProjection() {
        return mMediaProjection;
    }

    public static void stopMediaProjection(MediaProjection.Callback callback) {
        if (mMediaProjection != null) {
            if (callback != null) {
                mMediaProjection.unregisterCallback(callback);
            }
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    public void registerLocalReceiver(@NonNull BroadcastReceiver receiver,
                                      @NonNull IntentFilter filter) {
        mLocalBroadcastManager.registerReceiver(receiver, filter);
    }

    private void startFloatingViewService() {
        Intent intent = new Intent(this, FloatingCircularMenuService.class);
        intent.putExtra(Constants.EXTRA_CUTOUT_SAFE_AREA,
                FloatingViewManager.Companion.findCutoutSafeArea(null));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent);
//        } else {
        startService(intent);
//        }

        // Bind to the service
        bindService(intent,
                mFlattingMenuServiceConnection,
                Context.BIND_AUTO_CREATE);
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
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        startFloatingViewService();
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
