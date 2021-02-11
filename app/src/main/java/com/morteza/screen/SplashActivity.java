package com.morteza.screen;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.morteza.screen.common.Constants;
import com.morteza.screen.common.ExtensionKt;
import com.morteza.screen.common.PermissionManager;

import static com.morteza.screen.ScreenApp.setScreenshotPermission;
/**
 * @author Morteza
 * @version 2019/12/3
 */
public class SplashActivity extends AppCompatActivity {

    private final String TAG = "SplashActivity";
    private static boolean sendRequest = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    // You have't permission
                    finish();
                }
            } else {
                //Todo  Need handel overlay in to 22 , 21
            }
            ExtensionKt.requestAllPermissions(this);
        }

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(TAG, "Permission callback called------- ");
        if (requestCode == Constants.REQUEST_ID_ALL_PERMISSIONS) {
            boolean isAllGranted = PermissionManager.handelAllPermissionsRequestResult(permissions, grantResults);
            if (isAllGranted) {
                init();
            } else {
                PermissionManager.shouldShowRequestPermissionRationale(this);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setFinishOnTouchOutside(true);
        Log.e(TAG, "onCreate  " + sendRequest);
        if (ExtensionKt.hasWriteStoragePermission(this)) {
            init();
        } else {
            if (ExtensionKt.hasOverlayPermission(this)) {
                ExtensionKt.requestAllPermissions(this);
            } else {
                ExtensionKt.askOverlayPermission(this);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    private void init() {
        sendRequest = getIntent().getBooleanExtra(Constants.SEND_REQUEST_MEDIA_PROJECTION, false);

        if (sendRequest) {
            MediaProjectionManager mediaProjectionManager = ScreenApp.getMediaProjectionManager();
            if (mediaProjectionManager != null) {
                Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
                startActivityForResult(captureIntent, Constants.REQUEST_MEDIA_PROJECTION);
            }
        } else {
            finish();
        }
    }

    /*
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
     */
}