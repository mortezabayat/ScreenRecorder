package com.morteza.screen.tools.recorder;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.morteza.screen.BuildConfig;
import com.morteza.screen.R;
import com.morteza.screen.services.helper.FloatingUiHelperInterface;

import static android.os.Build.VERSION_CODES.O;

/**
 * @author Morteza
 * @version 2019/12/3
 */
public class Notifications extends ContextWrapper {
    private static final int id = 0x1fff;
    private static final String CHANNEL_ID = "Recording";
    private static final String CHANNEL_NAME = "Screen Recorder Notifications";

    private long mLastFiredTime = 0;
    private NotificationManager mManager;
    private NotificationCompat.Action mStopAction;
    private NotificationCompat.Builder mBuilder;
    private Notification mNotification;
    private final RemoteViews normalContentView;
    private FloatingUiHelperInterface.FloatingUiControl mScreenRecordControl;


    public Notifications(Context context
            ,FloatingUiHelperInterface.FloatingUiControl callback) {
        super(context);
        mScreenRecordControl = callback;
        normalContentView = new RemoteViews(getPackageName(), R.layout.notification_normal);


        if (Build.VERSION.SDK_INT >= O) {
            createNotificationChannel();
        }

        getNotification();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            normalContentView.setOnClickResponse( , new RemoteViews.RemoteResponse());
        }
    }

    public Notification getNotification() {
        if (mNotification == null) {
            mNotification = getBuilder()
//                    .setContentText(getString(R.string.length_video))
                    .build();
        }
        return mNotification;
    }

    public void recording(long timeMs) {
        if (SystemClock.elapsedRealtime() - mLastFiredTime < 1000) {
            return;
        }
        if (mNotification == null) {
            mNotification = getBuilder()
//                    .setContentText(getString(R.string.length_video) + " " + DateUtils.formatElapsedTime(timeMs / 1000))
                    .build();
        }
        getNotificationManager().notify(id, mNotification);
        mLastFiredTime = SystemClock.elapsedRealtime();
    }

    private NotificationCompat.Builder getBuilder() {
        if (mBuilder == null) {
            mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setOngoing(true)
                    .setLocalOnly(true)
                    .setOnlyAlertOnce(true)
                    .setContent(normalContentView)
//                    .addAction(stopAction())
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher);
        }
        return mBuilder;
    }

    @TargetApi(O)
    private void createNotificationChannel() {
        NotificationChannel channel =
                new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        channel.setShowBadge(false);
        getNotificationManager().createNotificationChannel(channel);
    }

    static final String ACTION_STOP = BuildConfig.APPLICATION_ID + ".action.STOP";

    public NotificationCompat.Action stopAction() {
        if (mStopAction == null) {
            Intent intent = new Intent(ACTION_STOP).setPackage(getPackageName());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1,
                    intent, PendingIntent.FLAG_ONE_SHOT);
            mStopAction = new NotificationCompat.Action(android.R.drawable.ic_media_pause, getString(R.string.stop), pendingIntent);
        }
        return mStopAction;
    }

    public void clear() {
        mLastFiredTime = 0;
        mBuilder = null;
        mStopAction = null;
        getNotificationManager().cancelAll();
    }

    NotificationManager getNotificationManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

}
