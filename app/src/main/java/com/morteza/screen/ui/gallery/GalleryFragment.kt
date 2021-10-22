package com.morteza.screen.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.morteza.screen.R
import com.morteza.screen.common.base.BaseFragment

/**
 * @author Morteza
 * @version 2019/12/3
 */
@Deprecated("This Activity Will Be Removed ...")
class GalleryFragment : BaseFragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    override fun getFragmentName() = "GalleryFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        galleryViewModel =
            ViewModelProviders.of(this).get(GalleryViewModel::class.java)
        val root = inflater.inflate(R.layout.activity_dialog, container, false)
//        val textView: TextView = root.findViewById(R.id.text_gallery)
//        galleryViewModel.text.observe(this, Observer {
//            textView.text = it
//        })
        return root
    }
}