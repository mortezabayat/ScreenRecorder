package com.morteza.screen.common;
/**
 * @author Morteza
 * @version 2019/12/3
 */
public class Constants {



    public static final String VIDEO_OUT_DIR_NAME = "ScreenRecorder";

    public static final int CREATE_SCREEN_CAPTURE_INTENT = 0XA1;
    public static final int MOVE_TO_BACKGROUND = 0XA2;
    public static final int MOVE_TO_FOREGROUND = 0XA3;
    public static final int START_FLOATING_VIEW_SERVICE = 0XA4;
    public static final int REQUEST_MEDIA_PROJECTION = 12;
    public static final int START_VIDEO_RECORDER = 0XA6;

    public static final int REQUEST_ID_ALL_PERMISSIONS = 100;
    public static final int REQUEST_ID_RECORD_AUDIO_PERMISSIONS = 101;
    public static final int REQUEST_ID_WRITE_EXTERNAL_STORAGE_PERMISSIONS = 102;

    public static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 103;

    public static final String SEND_REQUEST_MEDIA_PROJECTION = "SEND_REQUEST_MEDIA_PROJECTION";
    public static final String START_COUNT_DOWN = "START_COUNT_DOWN";

    /**
     * Intent key (Cutout safe area)
     */
    public static final String EXTRA_CUTOUT_SAFE_AREA = "cutout_safe_area";
    public static final String HOME_UI = "HOME_UI";
    public static final String SETTING_UI = "SETTING_UI";
    public static final String PAINTER_UI = "PAINTER_UI";
}
