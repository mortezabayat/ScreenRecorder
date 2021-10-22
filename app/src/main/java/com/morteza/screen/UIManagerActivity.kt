package com.morteza.screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import com.morteza.screen.common.base.BaseActivity
import com.morteza.screen.common.base.BaseFragment
import com.morteza.screen.common.Constants
import com.morteza.screen.ui.gallery.GalleryFragment
import com.morteza.screen.ui.home.HomeFragment
import com.morteza.screen.ui.tools.ToolsFragment

/**
 * @author Morteza
 * @version 2019/12/3
 */
class UIManagerActivity : BaseActivity() {


    //fragment_layout
    override fun TAG(): String = "UIManagerActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ui_manager)
        if (VERBOSE) {
            Log.e(
                TAG(), "Bundle Info ${intent.extras?.getString(Constants.CURRENT_UI)} \n ${
                    intent.extras?.getString(
                        Constants.VIDEO_PATH
                    )
                }"
            )
        }
        intent?.let { navigateToView(it) }
    }

    private fun startFragment(f: BaseFragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_layout, f, f.getFragmentName())
            .commitAllowingStateLoss()
    }

    private fun navigateToView(bundle: Intent) {
        val uiCommand = bundle.extras?.getString(Constants.CURRENT_UI) ?: Constants.HOME_UI
        when (uiCommand) {
            Constants.HOME_UI -> {
                startFragment(HomeFragment())
            }
            Constants.PAINTER_UI -> {
                startFragment(ToolsFragment())
            }
            Constants.SETTING_UI -> {
                startFragment(GalleryFragment())
            }
            Constants.VIDEO_RESULT_UI -> {
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
}