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

        switch (requestCode) {
            case Constants.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(this)) {
                        // You have't permission
                        finish();
                    }
                } else {
                    //Todo  Need handel overlay in to 22 , 21
                }
                ExtensionKt.requestAllPermissions(this);
                break;
            }
            case Constants.REQUEST_MEDIA_PROJECTION: {
                if (Activity.RESULT_OK == resultCode && data != null) {
                    setScreenshotPermission((Intent) data.clone());
                } else {
                    setScreenshotPermission(null);
                }
            }
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

    private void init() {
        sendRequest = getIntent().getBooleanExtra(Constants.SEND_REQUEST_MEDIA_PROJECTION, false);
        if (sendRequest) {
            MediaProjectionManager mediaProjectionManager = ScreenApp.getMediaProjectionManager();
            if (mediaProjectionManager != null) {
                Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
                startActivityForResult(captureIntent, Constants.REQUEST_MEDIA_PROJECTION);
            }
        } else {
            ScreenApp.getInstance().startFloatingViewService(this);
            finish();
        }
    }
}