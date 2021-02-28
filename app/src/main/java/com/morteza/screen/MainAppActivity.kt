package com.morteza.screen

import android.os.Bundle
import android.view.Menu
import com.morteza.screen.common.BaseActivity

/**
 * @author Morteza
 * @version 2019/12/3
 */
class MainAppActivity : BaseActivity() {


    override fun TAG(): String = "MainAppActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Todo Add Home Fragment .

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

}


