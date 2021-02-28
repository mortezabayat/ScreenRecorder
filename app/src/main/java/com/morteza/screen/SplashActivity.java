package com.morteza.screen;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.morteza.screen.common.Constants;
import com.morteza.screen.common.ExtensionKt;
import com.morteza.screen.common.PermissionManager;
import com.morteza.screen.tools.CountDownAnimation;

import static com.morteza.screen.ScreenApp.setScreenshotPermission;

/**
 * @author Morteza
 * @version 2019/12/3
 */
public class SplashActivity extends AppCompatActivity implements CountDownAnimation.CountDownListener {

    private final String TAG = "SplashActivity";
    private CountDownAnimation countDownAnimation;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE: {

                if (Activity.RESULT_OK != resultCode) {
                    //Note OverLayer Not Access From User Launch Home Ui.
                    break;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(this)) {
                        //Note You have't permission
                        finish();
                    }
                } else {
                    //Todo  Need handel overlay in to 22 , 21
                }
                ExtensionKt.requestAllPermissions(this);
                finish();
            }
            case Constants.REQUEST_MEDIA_PROJECTION: {
                if (Activity.RESULT_OK == resultCode && data != null) {
                    setScreenshotPermission((Intent) data.clone());
                    startCountDownAnimation();
                } else {
                    setScreenshotPermission(null);
                    finish();
                }
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        boolean sendRequest = getIntent().getBooleanExtra(Constants.SEND_REQUEST_MEDIA_PROJECTION, false);
        if (sendRequest) {
            MediaProjectionManager mediaProjectionManager = ScreenApp.getMediaProjectionManager();
            if (mediaProjectionManager != null) {
                Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
                startActivityForResult(captureIntent, Constants.REQUEST_MEDIA_PROJECTION);
            }
            initCountDownAnimation();
        } else {
            ScreenApp.getInstance().startFloatingViewService(this);
            finish();
        }
    }

    private void initCountDownAnimation() {
        TextView textView = new TextView(this);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        textView.setTextColor(getResources().getColor(R.color.colorAccent));
        textView.setTextSize(250);
        textView.setGravity(Gravity.CENTER);
        setContentView(textView, params);
        countDownAnimation = new CountDownAnimation(textView, 5);
        countDownAnimation.setCountDownListener(this);
    }

    private void startCountDownAnimation() {
        // Set (Scale +
        // Alpha)
        // Use a set of animations
        runOnUiThread(() -> {
            Animation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f,
                    0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            AnimationSet animationSet = new AnimationSet(false);
            animationSet.addAnimation(scaleAnimation);
            animationSet.addAnimation(alphaAnimation);
            countDownAnimation.setAnimation(animationSet);

            // Customizable start count
//            countDownAnimation.setStartCount(/*getStartCount()*/4);
            countDownAnimation.start();
        });
    }

    @Override
    public void onCountDownEnd(CountDownAnimation animation) {
        ScreenApp.getInstance().sendObjectToFlattingMenuService();
        finish();
    }
}