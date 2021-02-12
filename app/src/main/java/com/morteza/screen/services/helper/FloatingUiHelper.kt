package com.morteza.screen.services.helper

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.isGone
import com.morteza.screen.BuildConfig
import com.morteza.screen.R
import com.morteza.screen.ScreenApp
import com.morteza.screen.common.Constants
import com.morteza.screen.common.DisplayInfo
import com.morteza.screen.common.toast
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager
import kotlin.math.cos
import kotlin.math.sin

class FloatingUiHelper(
    private val mContext: Context,
    private val mScreenRecordControl: FloatingUiHelperInterface.FloatingUiControl
) : FloatingViewListener {
    private val TAG = "FloatingUiHelper"
    private val ACTION_STOP = BuildConfig.APPLICATION_ID + ".action.STOP"
    private val mWindowManger by lazy { mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val mRadius by lazy { mContext.resources.getDimensionPixelSize(R.dimen.radius) }
    private val mActionButtonSize by lazy { mContext.resources.getDimensionPixelSize(R.dimen.floating_icon_size) }
    private val mOverMargin by lazy { (2 * mDisplayInfo.metrics.density).toInt() }
    private var mCircularMenuParams: WindowManager.LayoutParams? = null
    private lateinit var mCircularMenu: ConstraintLayout

    private var mActionButton: AppCompatImageView? = null

    private lateinit var mCloseBtn: AppCompatImageView
    private lateinit var mToolsBtn: AppCompatImageView
    private lateinit var mStartRecordBtn: AppCompatImageView
    private lateinit var mHomeBtn: AppCompatImageView

    private lateinit var mPauseBtn: AppCompatImageView
    private lateinit var mStopBtn: AppCompatImageView
    private lateinit var mPaintBtn: AppCompatImageView
    private lateinit var mSettingsBtn: AppCompatImageView


    val mDisplayInfo by lazy { DisplayInfo(mWindowManger) }
    private var mFloatingViewManager: FloatingViewManager? = null
    private val mConstraintSetRTL = ConstraintSet()
    private val mConstraintSetLTR = ConstraintSet()
    private lateinit var mGestureDetector: GestureDetector
    private var mIsHideCircularMenu = true
    private var mXPosition: Int = 0
    private var mYPosition: Int = 0
    private val mIsMoveToEdge = true // For FloatingViewManager.MOVE_DIRECTION_THROWN
    var mIsViewAddToWindowManager = false
    var mHaveActiveRecording = false

    @SuppressLint("InflateParams")
    fun initFloatingView(intent: Intent) {
        mActionButton =
            LayoutInflater.from(mContext).inflate(
                R.layout.floating_action_button,
                null, false
            ) as AppCompatImageView
        mActionButton?.setOnClickListener {
            closeOpenCircleMenu()
        }
        val rect = intent.getParcelableExtra<Rect>(Constants.EXTRA_CUTOUT_SAFE_AREA)
        mFloatingViewManager = FloatingViewManager(mContext, this).apply {
            setFixedTrashIconImage(R.drawable.ic_trash_fixed)
            setActionTrashIconImage(R.drawable.ic_trash_action)
            setSafeInsetRect(rect)
            val options = FloatingViewManager.Options().apply {
                moveDirection = FloatingViewManager.MOVE_DIRECTION_THROWN
                overMargin = this@FloatingUiHelper.mOverMargin
                floatingViewX =
                    mDisplayInfo.metrics.widthPixels - mActionButtonSize - mRadius + overMargin
                floatingViewY = (mDisplayInfo.metrics.heightPixels - mActionButtonSize) / 2
                isTrashViewEnabled = false
                setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS)
            }
            mActionButton?.let {
                addViewToWindow(it, options)
            }
        }
        initCircularMenu()
        mIsViewAddToWindowManager = true
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    private fun initCircularMenu() {
        //Init Git Repo as Private Mode.
        mContext.registerReceiver(mStopActionReceiver, IntentFilter(ACTION_STOP))
        mCircularMenu =
            LayoutInflater.from(mContext).inflate(
                R.layout.floating_action_circle_menu,
                null, false
            ) as ConstraintLayout
        mConstraintSetLTR.apply {
            clone(mCircularMenu)
            connect(
                R.id.btnClose,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            connect(R.id.btnClose, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(
                R.id.btnClose,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
        }
        mConstraintSetRTL.apply {
            clone(mCircularMenu)
            connect(R.id.btnClose, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            connect(R.id.btnClose, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(
                R.id.btnClose,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
        }

        mCloseBtn = mCircularMenu.findViewById(R.id.btnClose)
        mToolsBtn = mCircularMenu.findViewById(R.id.tools_btn)
        mStartRecordBtn = mCircularMenu.findViewById(R.id.start_record_btn)
        mSettingsBtn = mCircularMenu.findViewById(R.id.settings_btn)
        mHomeBtn = mCircularMenu.findViewById(R.id.home_btn)

        mPauseBtn = mCircularMenu.findViewById(R.id.pause_btn)
        mPaintBtn = mCircularMenu.findViewById(R.id.paint_btn)
        mStopBtn = mCircularMenu.findViewById(R.id.stop_btn)

        mPauseBtn.setOnClickListener {
            mScreenRecordControl.pauseRecording()
            mCloseBtn.callOnClick()
        }
        mPaintBtn.setOnClickListener {
            mContext.toast("We Not Support This Option Now ... ")
            mCloseBtn.callOnClick()
        }
        mStopBtn.setOnClickListener {
            mContext.toast("stop recording and open the file ")
            mCloseBtn.callOnClick()
            mScreenRecordControl.stopRecording()
        }

        mCloseBtn.setOnClickListener {
            closeOpenCircleMenu()
        }
        mToolsBtn.setOnClickListener {
            mContext.toast("Tools Button Screen Will Destroy ;( ... ")
            mCloseBtn.callOnClick()
            onFinishFloatingView()
        }
        mStartRecordBtn.setOnClickListener {
            ScreenApp.getScreenshotPermission()
            mCloseBtn.callOnClick()
        }
        mSettingsBtn.setOnClickListener {
            mContext.toast("Setting Button ... ")
            mCloseBtn.callOnClick()
        }
        mHomeBtn.setOnClickListener {
            mContext.toast("Home Button ... ")
            mCloseBtn.callOnClick()
        }
        val gesture = object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                closeOpenCircleMenu()
                return super.onSingleTapConfirmed(e)
            }
        }
        mGestureDetector = GestureDetector(mContext, gesture)
        mCircularMenu.setOnTouchListener(FloatingButtonTouchListener())

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PRIORITY_PHONE
        }
        mCircularMenuParams = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    //or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSPARENT
        )
        mCircularMenuParams!!.gravity = Gravity.TOP or Gravity.START
    }

    private val mStopActionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_STOP == intent.action) {
                mScreenRecordControl.stopRecording()
            }
        }
    }

    fun closeOpenCircleMenu() {

        if (mIsHideCircularMenu) {

            mActionButton?.let {
                it.animate().apply {
                    cancel()
                }.alpha(0f)
                    .setDuration(0L)
                    .setStartDelay(0L)
                    .start()

                mCircularMenuParams!!.y =
                    mYPosition + (it.height - mCircularMenuParams!!.height) / 2
            }

            mIsHideCircularMenu = false

            mCircularMenuParams!!.x = if (!mIsMoveToEdge) {
                if (isRTL()) mXPosition - mCircularMenuParams!!.width + mActionButtonSize else mXPosition
            } else {
                if (isRTL()) getScreenWidth() - mCircularMenuParams!!.width + mOverMargin else -mOverMargin
            }

            if (isRTL()) {
                mConstraintSetRTL.clear(R.id.btnClose, ConstraintSet.START)
                mConstraintSetRTL.applyTo(mCircularMenu)
            } else {
                mConstraintSetLTR.clear(R.id.btnClose, ConstraintSet.END)
                mConstraintSetLTR.applyTo(mCircularMenu)
            }

            if (!ViewCompat.isAttachedToWindow(mCircularMenu)) {
                mWindowManger.addView(mCircularMenu, mCircularMenuParams)
            } else {
                mCircularMenu.isGone = false
                mWindowManger.updateViewLayout(mCircularMenu, mCircularMenuParams)
            }
        } else {
            mIsHideCircularMenu = true
        }

        val angle = if (mIsHideCircularMenu) 0f else 45f
        mCloseBtn.animate().apply {
            cancel()
        }
            .rotation(angle)
            .setDuration(100L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withStartAction {
                val count = mCircularMenu.childCount - 1
                mCircularMenu.forEachIndexed { index, view ->
                    if (view.id != R.id.btnClose) {
                        if (mHaveActiveRecording) {
                            if (view.id == R.id.pause_btn ||
                                view.id == R.id.paint_btn ||
                                view.id == R.id.stop_btn ||
                                view.id == R.id.settings_btn
                            ) {
                                if (!mIsHideCircularMenu) view.isGone = mIsHideCircularMenu
                            }

                        } else {
                            if (view.id == R.id.start_record_btn ||
                                view.id == R.id.home_btn ||
                                view.id == R.id.tools_btn ||
                                view.id == R.id.settings_btn
                            ) {
                                if (!mIsHideCircularMenu) view.isGone = mIsHideCircularMenu
                            }
                        }
                        view.side(mIsHideCircularMenu, mRadius, isRTL(), index == count - 1)
                    }
                }
            }
            .withEndAction {
                if (mIsHideCircularMenu) {
                    mActionButton?.let {
                        it.animate().apply {
                            cancel()
                        }.alpha(1f)
                            .setDuration(100L)
                            .setStartDelay(0L)
                            .withEndAction {
                                it.animate().alpha(0.25f).setDuration(3000L).start()
                            }
                            .start()

                    }
                }
            }
            .start()
    }

    private fun View.side(
        isGone: Boolean,
        radius: Int,
        isRTL: Boolean = false,
        isLastItem: Boolean = false
    ) {
        val layoutParams = this.layoutParams as ConstraintLayout.LayoutParams
        val angle = layoutParams.circleAngle.toDouble()
        var radian = Math.toRadians(angle)
        if (isRTL) radian = -radian
        var from = 0
        var to = radius
        if (isGone) {
            from = radius
            to = 0
        }
        ValueAnimator.ofInt(from, to).apply {
            duration = 200L
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val animatedValue = it.animatedValue as Int
                val x = animatedValue * sin(radian)
                val y = animatedValue * cos(radian)
                this@side.translationX = x.toFloat()
                this@side.translationY = y.toFloat()
            }
            doOnEnd {
                if (isGone) {
                    this@side.isGone = isGone
                    if (isLastItem) {
                        /*if (isHideCircularMenu) {
                            actionButton.animate().apply {
                                cancel()
                            }.alpha(1f)
                            .setStartDelay(0L)
                            .setStartDelay(0L)
                            .withEndAction {
                                actionButton.animate().alpha(0.45f).setDuration(3000L).start()
                            }
                            .start()
                        }*/
                        mCircularMenu.isGone = true
                    }
                }
            }
        }.start()
    }

    private fun isRTL() = mXPosition > mDisplayInfo.metrics.widthPixels / 2

    private fun getScreenWidth() =
        if (mContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            mDisplayInfo.metrics.heightPixels
        else
            mDisplayInfo.metrics.widthPixels

    private inner class FloatingButtonTouchListener : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            mGestureDetector.onTouchEvent(event)
            when (event.actionMasked) {
                MotionEvent.ACTION_OUTSIDE -> {
                    if (!mCircularMenu.isGone) {
                        mCloseBtn.callOnClick()
                    }
                }
            }
            return true
        }
    }

    fun unregisterReceiver() {
        try {
            mContext.unregisterReceiver(mStopActionReceiver)
        } catch (e: Exception) {
            //ignored
        }
    }

    fun destroy() {
        mFloatingViewManager?.removeAllViewToWindow()
        try {
            mWindowManger.removeView(mCircularMenu)
        } catch (e: IllegalArgumentException) {
        }
    }

    override fun onFinishFloatingView() {
        mScreenRecordControl.onFinishFloatingView()
    }

    /**
     * {@inheritDoc}
     */
    override fun onTouchFinished(isFinishing: Boolean, x: Int, y: Int) {
        if (isFinishing) {
            Log.d(TAG, mContext.getString(R.string.deleted_soon))
        } else {
            mXPosition = x
            mYPosition = y
            Log.d(TAG, mContext.getString(R.string.touch_finished_position, x, y))
        }
    }
}