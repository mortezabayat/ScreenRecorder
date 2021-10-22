package com.morteza.screen.common.base;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author Morteza
 * @version 2019/12/3
 */
abstract public class BaseActivity extends AppCompatActivity {

    public abstract String TAG();

    public boolean VERBOSE = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
