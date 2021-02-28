package com.morteza.screen.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.morteza.screen.MainAppActivity;
import com.morteza.screen.ScreenApp;

public class UiNavigationManager {

    private static final UiNavigationManager mInstance;
    private Context mContext;
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
        mContext = ScreenApp.getInstance().getApplicationContext();
        mUiIntent = new Intent(mContext, MainAppActivity.class);
    }

    public void launchHome() {
        mUiIntent.putExtra(Constants.HOME_UI,true);
        mContext.startActivity(mUiIntent);
    }

    public void launchSetting() {
        mUiIntent.putExtra(Constants.SETTING_UI,true);
        mContext.startActivity(mUiIntent);
    }

    public void launchPainter() {
        mUiIntent.putExtra(Constants.PAINTER_UI,true);
        mContext.startActivity(mUiIntent);
    }
}
