package com.morteza.screen;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.morteza.screen.common.Constants;
import com.morteza.screen.common.ExtensionKt;
import com.morteza.screen.services.FloatingCircularMenuService;

import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

import static com.morteza.screen.ScreenApp.setScreenshotPermission;
import static com.morteza.screen.common.Constants.CREATE_SCREEN_CAPTURE_INTENT;
import static com.morteza.screen.common.Constants.MOVE_TO_BACKGROUND;
import static com.morteza.screen.common.Constants.MOVE_TO_FOREGROUND;
import static com.morteza.screen.common.Constants.START_FLOATING_VIEW_SERVICE;

public class SplashActivity extends AppCompatActivity implements Handler.Callback {

    private final String TAG = "SplashActivity";

    private void startFloatingViewService() {
        Intent intent = new Intent(this, FloatingCircularMenuService.class);
        intent.putExtra(FloatingCircularMenuService.EXTRA_CUTOUT_SAFE_AREA,
                FloatingViewManager.Companion.findCutoutSafeArea(this));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (Constants.REQUEST_MEDIA_PROJECTION == requestCode) {
            if (Activity.RESULT_OK == resultCode && data != null) {
                setScreenshotPermission((Intent) data.clone());
            }
        } else if (Activity.RESULT_CANCELED == resultCode) {
            setScreenshotPermission(null);
        }
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setFinishOnTouchOutside(true);
        Log.e(TAG, "onCreate");

        if (savedInstanceState == null)
            ScreenApp.setHandler(new Handler(Looper.getMainLooper(), this));

        if (!ExtensionKt.hasPermissions(this)) {
            ExtensionKt.requestPermissionsEX(this);
            ExtensionKt.askPermission(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean sendRequest = getIntent().getBooleanExtra(Constants.SEND_REQUEST_MEDIA_PROJECTION, false);
        Log.e(TAG, "onStart " + sendRequest);

        if (sendRequest) {

            MediaProjectionManager mediaProjectionManager = ScreenApp.getMediaProjectionManager();
            if (mediaProjectionManager != null) {
                Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
                startActivityForResult(captureIntent, Constants.REQUEST_MEDIA_PROJECTION);
            }
        } else {
            ScreenApp.getHandler().sendEmptyMessageDelayed(MOVE_TO_BACKGROUND, 1000);
        }
    }

    @Override
    protected void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {

        switch (msg.what) {
            case CREATE_SCREEN_CAPTURE_INTENT: {

                Intent intent = getIntent();
                intent.putExtra(Constants.SEND_REQUEST_MEDIA_PROJECTION, true);
                startActivity(intent);
                break;
            }
            case MOVE_TO_BACKGROUND: {
                //moveTaskToBack(true);
                finish();
                break;
            }
            case MOVE_TO_FOREGROUND: {
                //Todo start activity back
                break;
            }
            case START_FLOATING_VIEW_SERVICE: {
                startFloatingViewService();
                break;
            }
        }
        return true;
    }
}