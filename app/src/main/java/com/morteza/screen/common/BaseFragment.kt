package com.morteza.screen.common

import androidx.fragment.app.DialogFragment

abstract class BaseFragment : DialogFragment() {
    abstract fun getFragmentName(): String
}