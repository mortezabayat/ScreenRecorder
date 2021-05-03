package com.morteza.screen.ui.previewsvideo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.morteza.screen.R
import com.morteza.screen.common.BaseFragment

class PreviewsVideo : BaseFragment() {
    override fun getFragmentName() = "PreviewsVideo"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_video, container, false)
        val imageView: ImageView = root.findViewById(R.id.imageView)
        videoPath?.let {
            Glide.with(imageView.context).asBitmap()
                .load(it)
                .apply(
                    RequestOptions()
                        .frame(10000)
                        .centerCrop()
                )
                .into(imageView)
        }
        return root
    }

    companion object {
        private var videoPath: String? = null

        fun newInstance(path: String?): PreviewsVideo {
            videoPath = path
            return PreviewsVideo()
        }
    }
}