package com.morteza.screen.common;

import android.content.Intent;

import com.morteza.screen.ScreenApp;
import com.morteza.screen.UIManagerActivity;
import com.morteza.screen.ui.dialogs.DialogActivity;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class UiNavigationManager {

    private static final UiNavigationManager mInstance;
    private Intent mUiIntent;

    UiNavigationManager() {
        initMainUi();
    }

    static {
        mInstance = new UiNavigationManager();
    }

    public static UiNavigationManager getInstance() {
        return mInstance;
    }

    private void initMainUi() {
        mUiIntent = new Intent(ScreenApp.getInstance().getApplicationContext(), UIManagerActivity.class);
        mUiIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
    }

    public void launchHome() {
        mUiIntent.putExtra(Constants.CURRENT_UI, Constants.HOME_UI);
        ScreenApp.getInstance().getApplicationContext().startActivity(mUiIntent);
    }

    public void launchSetting() {
        mUiIntent.putExtra(Constants.CURRENT_UI, Constants.SETTING_UI);
        ScreenApp.getInstance().getApplicationContext().startActivity(mUiIntent);
    }

    public void launchPainter() {
        mUiIntent.putExtra(Constants.CURRENT_UI, Constants.PAINTER_UI);
        ScreenApp.getInstance().getApplicationContext().startActivity(mUiIntent);
    }

    public void showRecorderResult(String absolutePath) {
        Intent intent = new Intent(ScreenApp.getInstance().getApplicationContext(), DialogActivity.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.VIDEO_PATH, absolutePath);
        ScreenApp.getInstance().getApplicationContext().startActivity(intent);
    }
}
