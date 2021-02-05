package com.morteza.screen

import android.Manifest.permission
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.morteza.screen.ui.floatingcircularmenu.FloatingCircularMenuService
import com.morteza.screen.ui.floatingcircularmenu.FloatingCircularMenuService.Companion.mMediaProjection
import com.morteza.screen.ui.floatingcircularmenu.FloatingCircularMenuService.Companion.mMediaProjectionManager
import com.morteza.screen.ui.floatingcircularmenu.FloatingCircularMenuService.Companion.mProjectionCallback
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    @TargetApi(23)
    fun askPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, 0)
    }

    private val REQUEST_MEDIA_PROJECTION = 1
    private val REQUEST_PERMISSIONS = 2


    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {

            // NOTE: Should pass this result data into a Service to run ScreenRecorder.

            data?.apply {

                val mediaProjection: MediaProjection? =
                    mMediaProjectionManager?.getMediaProjection(resultCode, data)

                if (mediaProjection == null) {
                    Log.e("@@", "media projection is null")
                    return
                }
                mMediaProjection = mediaProjection

            }
            mMediaProjection?.registerCallback(mProjectionCallback, Handler())

            startFloatingViewService(this)

        }
    }


    private fun hasPermissions(): Boolean {
        val pm = packageManager
        val packageName = packageName
        val granted = ((if (/*mAudioToggle.isChecked()*/ true) pm.checkPermission(
            permission.RECORD_AUDIO,
            packageName
        ) else PackageManager.PERMISSION_GRANTED)
                or pm.checkPermission(permission.WRITE_EXTERNAL_STORAGE, packageName))
        return granted == PackageManager.PERMISSION_GRANTED
    }

    @TargetApi(VERSION_CODES.M)
    private fun requestPermissions() {
        val permissions =
            if (true/*mAudioToggle.isChecked()*/) arrayOf(
                permission.WRITE_EXTERNAL_STORAGE,
                permission.RECORD_AUDIO
            ) else arrayOf(permission.WRITE_EXTERNAL_STORAGE)
        var showRationale = false
        for (perm in permissions) {
            showRationale = showRationale or shouldShowRequestPermissionRationale(perm)
        }
        if (!showRationale) {
            requestPermissions(
                permissions,
                REQUEST_PERMISSIONS
            )
            return
        }
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.using_your_mic_to_record_audio))
            .setCancelable(false)
            .setPositiveButton(
                android.R.string.ok
            ) { _: DialogInterface?, _: Int ->
                requestPermissions(
                    permissions,
                    REQUEST_PERMISSIONS
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

//        askPermission()
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            startFloatingViewService(this)

//            val overlayIntent = Intent()
//            overlayIntent.setClass(this, OverlayService::class.java)
//            startService(overlayIntent)

        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        appBarConfiguration = AppBarConfiguration( setOf(
//                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
//                R.id.nav_tools, R.id.nav_share, R.id.nav_send
//            ), drawerLayout
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        //      navView.setupWithNavController(navController)

        // askPermission()


        if (!hasPermissions()) {
            requestPermissions()
        }
        mMediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager


        val captureIntent = mMediaProjectionManager!!.createScreenCaptureIntent()
        startActivityForResult(
            captureIntent,
            REQUEST_MEDIA_PROJECTION
        )

    }

    private fun startFloatingViewService(activity: AppCompatActivity) {

        // *** You must follow these rules when obtain the cutout(FloatingViewManager.findCutoutSafeArea) ***
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 1. 'windowLayoutInDisplayCutoutMode' do not be set to 'never'
            if (activity.window.attributes.layoutInDisplayCutoutMode == WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS) {
                throw RuntimeException("'windowLayoutInDisplayCutoutMode' do not be set to 'never'")
            }
            // 2. Do not set Activity to landscape
            if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                throw RuntimeException("Do not set Activity to landscape")
            }
        }
        // launch service
        val service = FloatingCircularMenuService::class.java
        val key: String = FloatingCircularMenuService.EXTRA_CUTOUT_SAFE_AREA
        val intent = Intent(activity, service).putExtra(
            key,
            FloatingViewManager.findCutoutSafeArea(activity)
        )
        activity.startService(intent)
        activity.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment)
//        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//    }
}
