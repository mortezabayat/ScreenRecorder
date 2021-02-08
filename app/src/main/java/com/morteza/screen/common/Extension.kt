package com.morteza.screen.common

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import com.morteza.screen.R
import com.morteza.screen.common.ActivityExtension.REQUEST_PERMISSIONS

object ActivityExtension {
    val REQUEST_MEDIA_PROJECTION = 1
    val REQUEST_PERMISSIONS = 2
}

@TargetApi(23)
fun Activity.askPermission() {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:$packageName")
    )
    startActivityForResult(intent, 0)
}

fun Activity.hasPermissions(): Boolean {
    val pm = packageManager
    val packageName = packageName
    val granted = ((if (/*mAudioToggle.isChecked()*/ true) pm.checkPermission(
        Manifest.permission.RECORD_AUDIO,
        packageName
    ) else PackageManager.PERMISSION_GRANTED)
            or pm.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, packageName))
    return granted == PackageManager.PERMISSION_GRANTED
}
//Todo We Must modify this function for abi less than 23
fun Activity.requestPermissionsEX() {
    val permissions =
        if (true/*mAudioToggle.isChecked()*/) arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        ) else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    var showRationale = false
    for (perm in permissions) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            showRationale = showRationale or shouldShowRequestPermissionRationale(perm)
        }
    }
    if (!showRationale) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(
                permissions,
                REQUEST_PERMISSIONS
            )
        }
        return
    }
    AlertDialog.Builder(this)
        .setMessage(getString(R.string.using_your_mic_to_record_audio))
        .setCancelable(false)
        .setPositiveButton(
            android.R.string.ok
        ) { _: DialogInterface?, _: Int ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestPermissions(
                    permissions,
                    REQUEST_PERMISSIONS
                )
            }
        }
        .setNegativeButton(android.R.string.cancel, null)
        .create()
        .show()
}