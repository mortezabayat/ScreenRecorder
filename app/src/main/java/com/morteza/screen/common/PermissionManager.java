package com.morteza.screen.common;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
/**
 * @author Morteza
 * @version 2019/12/3
 */
public class PermissionManager {

    private static final String TAG = "PermissionManager";
    public static final String[] APP_PERMISSIONS_LIST = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void shouldShowRequestPermissionRationale(Activity activity) {
        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
        // shouldShowRequestPermissionRationale will return true
        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
        boolean shouldShowRequestPermissionRationale = false;
        for (String per : APP_PERMISSIONS_LIST) {
            shouldShowRequestPermissionRationale |= ActivityCompat.shouldShowRequestPermissionRationale(activity, per);
        }
        if (shouldShowRequestPermissionRationale) {
            ExtensionKt.showDialog(activity, "Record Audio and Write External Storage Permission required for this app",
                    (dialog, which) -> {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                ExtensionKt.requestUnGrantedPermissions(activity);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                // proceed with logic by disabling the related features or quit the app.
                                break;
                        }
                    });
        }
        //permission is denied (and never ask again is  checked)
        //shouldShowRequestPermissionRationale will return false
        else {
            Toast.makeText(activity, "Go to settings and enable permissions",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    public static boolean handelAllPermissionsRequestResult(@NonNull String[] permissions,
                                                            @NonNull int[] grantResults) {
        if (grantResults.length <= 0) {
            return false;
        }
        boolean isAllPermissionGranted = true;
        for (int i = 0; i < permissions.length; i++) {
            isAllPermissionGranted &= grantResults[i] == PackageManager.PERMISSION_GRANTED;
        }
        return isAllPermissionGranted;
    }

}
