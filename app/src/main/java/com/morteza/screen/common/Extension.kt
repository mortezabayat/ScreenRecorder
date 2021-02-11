package com.morteza.screen.common

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.morteza.screen.R
import com.morteza.screen.common.Constants.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Morteza
 * @version 2019/12/3
 */
@TargetApi(23)
fun Activity.askOverlayPermission() {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:$packageName")
    )
    startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
}

@TargetApi(23)
fun Activity.hasOverlayPermission() = Settings.canDrawOverlays(this)

fun Activity.hasAudioRecordPermission(): Boolean {
    val pm = packageManager
    val packageName = packageName
    val granted = pm.checkPermission(Manifest.permission.RECORD_AUDIO, packageName)
    return granted == PackageManager.PERMISSION_GRANTED
}

fun Activity.hasWriteStoragePermission(): Boolean {
    val pm = packageManager
    val packageName = packageName
    val granted = pm.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, packageName)
    return granted == PackageManager.PERMISSION_GRANTED
}

fun Activity.requestAudioRecordPermission() = ActivityCompat.requestPermissions(
    this,
    arrayOf(Manifest.permission.RECORD_AUDIO),
    REQUEST_ID_RECORD_AUDIO_PERMISSIONS
)

fun Activity.requestWriteStoragePermission() = ActivityCompat.requestPermissions(
    this,
    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
    REQUEST_ID_WRITE_EXTERNAL_STORAGE_PERMISSIONS
)

fun Activity.requestAllPermissions() {
    ActivityCompat.requestPermissions(
        this,
        PermissionManager.APP_PERMISSIONS_LIST,
        REQUEST_ID_ALL_PERMISSIONS
    )
}

fun Activity.requestUnGrantedPermissions() {

    val listPermissionsNeeded: MutableList<String> = ArrayList()
    for (s: String in PermissionManager.APP_PERMISSIONS_LIST) {
        val isUnGranted =
            ContextCompat.checkSelfPermission(this, s) == PackageManager.PERMISSION_DENIED
        if (isUnGranted) {
            listPermissionsNeeded.add(s)
        }
    }
    if (listPermissionsNeeded.isNotEmpty()) {
        ActivityCompat.requestPermissions(
            this,
            listPermissionsNeeded.toTypedArray(),
            REQUEST_ID_ALL_PERMISSIONS
        )
    }
}

fun Context.showDialog(message: String?, okListener: DialogInterface.OnClickListener) {
    androidx.appcompat.app.AlertDialog.Builder(this)
        .setMessage(message ?: getString(R.string.using_your_mic_to_record_audio))
        .setCancelable(false)
        .setPositiveButton(android.R.string.ok, okListener)
        .setNegativeButton(android.R.string.cancel, null)
        .create()
        .show()
}

fun Context.toast(message: String, vararg args: Any) {
    // In Brazilian Portuguese this may take longer to read
    val toast = Toast.makeText(
        this,
        if (args.isEmpty()) message else String.format(
            Locale.US,
            message,
            *args
        ),
        Toast.LENGTH_LONG
    )
    toast.show()
}