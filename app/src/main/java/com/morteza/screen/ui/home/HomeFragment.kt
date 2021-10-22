package com.morteza.screen.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.morteza.screen.R
import com.morteza.screen.common.base.BaseFragment

/**
 * @author Morteza
 * @version 2019/12/3
 */
@Deprecated("This Activity Will Be Removed ...")

class HomeFragment : BaseFragment() {

    private lateinit var homeViewModel: HomeViewModel
    override fun getFragmentName() = "HomeFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}