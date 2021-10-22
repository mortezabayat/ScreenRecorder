package com.morteza.screen.ui.previewsvideo

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util
import com.morteza.screen.R
import com.morteza.screen.ScreenApp
import com.morteza.screen.common.base.BaseFragment
import com.morteza.screen.tools.player.BitmapOverlayVideoProcessor
import com.morteza.screen.tools.player.VideoProcessingGLSurfaceView
import java.io.File


class PreviewsVideo : BaseFragment(), Player.EventListener {
    override fun getFragmentName() = "PreviewsVideo"

    private var playerView: PlayerView? = null
    private var videoProcessingGLSurfaceView: VideoProcessingGLSurfaceView? = null
    private var player: SimpleExoPlayer? = null
    private val mContext: Context = ScreenApp.getInstance().applicationContext


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_video, container, false)
//        val imageView: ImageView = root.findViewById(R.id.imageView)
        (root.findViewById(R.id.close) as ImageButton)
            .setOnClickListener {
                activity?.finish()
            }

        playerView = root.findViewById(R.id.player_view)
        val videoProcessingGLSurfaceView = VideoProcessingGLSurfaceView(
            context, false, BitmapOverlayVideoProcessor(context)
        )
        val contentFrame: FrameLayout = root.findViewById(R.id.exo_content_frame)
        contentFrame.addView(videoProcessingGLSurfaceView)
        this.videoProcessingGLSurfaceView = videoProcessingGLSurfaceView


//        (root.findViewById(R.id.play) as ImageButton)
//            .setOnClickListener {
        //Todo Play the video
//                val destFile = File(
//                    Environment.getExternalStorageDirectory(),
//                    File.separator + Config.VIDEO_COMPRESSOR_APPLICATION_DIR_NAME
//                            + Config.VIDEO_COMPRESSOR_COMPRESSED_VIDEOS_DIR + File.separator
//                            + random.nextInt(10000) + ".mp4")
//                val videoInfo = VideoInfo(tempFile.path, destFile.path, selectedCompression, mute)
//                MediaController.getInstance().convertVideo(videoInfo)
//            }
//        videoPath?.let {
//            Glide.with(imageView.context).asBitmap()
//                .load(it)
//                .apply(
//                    RequestOptions()
//                        .frame(1000)
//                        .centerCrop()
//                )
//                .into(imageView)
//        }
        return root
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
            playerView?.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
            playerView?.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            playerView?.onPause()
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            if (playerView != null) {
                playerView!!.onPause()
            }
            releasePlayer()
        }
    }

    private fun initializePlayer() {
        videoPath?.let {
            // Create a data source factory.
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(mContext)
            val myFile = File(it)
            val videoUrl = Uri.fromFile(myFile).toString()
            val uri = Uri.parse(videoUrl)

            // Create a progressive media source pointing to a stream uri.
            val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))
            val player =
                SimpleExoPlayer.Builder(mContext).build()
            player.repeatMode = Player.REPEAT_MODE_OFF
            player.setMediaSource(mediaSource)
            player.prepare()
            player.play()
            val videoProcessingGLSurfaceView = videoProcessingGLSurfaceView
            videoProcessingGLSurfaceView?.setVideoComponent(player.videoComponent)
            playerView?.player = player
            player.addAnalyticsListener(EventLogger( /* trackSelector= */null))
            this.player = player
        }
    }

    private fun releasePlayer() {
        player?.let {
            it.release()
            videoProcessingGLSurfaceView?.setVideoComponent(null)
            player = null
        }
    }

    companion object {
        private var videoPath: String? = null

        fun newInstance(path: String?): PreviewsVideo {
            videoPath = path
            return PreviewsVideo()
        }
    }
}