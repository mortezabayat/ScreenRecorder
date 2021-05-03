package com.morteza.screen.ui.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.morteza.screen.R
import com.morteza.screen.common.BaseFragment

/**
 * @author Morteza
 * @version 2019/12/3
 */
@Deprecated("This Activity Will Be Removed ...")
class ToolsFragment : BaseFragment() {

    private lateinit var toolsViewModel: ToolsViewModel
    override fun getFragmentName() = "ToolsFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        toolsViewModel =
            ViewModelProviders.of(this).get(ToolsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_tools, container, false)
        val textView: TextView = root.findViewById(R.id.text_tools)
        toolsViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}