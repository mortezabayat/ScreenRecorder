package com.morteza.screen.common.base

import androidx.fragment.app.DialogFragment

abstract class BaseFragment : DialogFragment() {
    abstract fun getFragmentName(): String
}