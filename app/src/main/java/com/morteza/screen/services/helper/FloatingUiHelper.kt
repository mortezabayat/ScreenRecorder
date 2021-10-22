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
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.doOnEnd
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.isGone
import com.morteza.screen.BuildConfig
import com.morteza.screen.R
import com.morteza.screen.ScreenApp
import com.morteza.screen.common.Constants
import com.morteza.screen.common.DisplayInfo
import com.morteza.screen.common.UiNavigationManager
import com.morteza.screen.common.toast
import github.mortezabayat.circularfloatingmenu.FloatingActionButton
import github.mortezabayat.circularfloatingmenu.SubActionButton
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager
import kotlin.math.cos
import kotlin.math.sin
import android.R.id.button2

import github.mortezabayat.circularfloatingmenu.FloatingActionMenu
import android.widget.FrameLayout

import android.view.WindowManager
import github.mortezabayat.circularfloatingmenu.FloatingActionMenu.MenuStateChangeListener


/**
 * @author Morteza
 * @version 2019/12/3
 */
class FloatingUiHelper(
    private val mContext: Context,
    private val mScreenRecordControl: FloatingUiHelperInterface.FloatingUiControl
) : FloatingViewListener {
    private val TAG = "FloatingUiHelper"
    private val ACTION_STOP = BuildConfig.APPLICATION_ID + ".action.STOP"

    private val mWindowManger by lazy { mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val mRadius by lazy { mContext.resources.getDimensionPixelSize(R.dimen.radius) }
    private val mActionButtonSize by lazy { mContext.resources.getDimensionPixelSize(R.dimen.floating_icon_size) }
    private val mOverMargin by lazy { (15 * mDisplayInfo.metrics.density).toInt() }

    private val mCircularMenuParams: WindowManager.LayoutParams by lazy {
        WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE
            }, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSPARENT
        )
    }
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
            setFixedTrashIconImage(R.drawable.ic_clear)
            setActionTrashIconImage(R.drawable.circle_floating_button_bg)
            setSafeInsetRect(rect)

            val options = FloatingViewManager.Options().apply {
                moveDirection = FloatingViewManager.MOVE_DIRECTION_THROWN
                overMargin = this@FloatingUiHelper.mOverMargin
                floatingViewX =
                    mDisplayInfo.metrics.widthPixels - mActionButtonSize - mRadius + overMargin
                floatingViewY = (mDisplayInfo.metrics.heightPixels - mActionButtonSize) / 2
                isTrashViewEnabled = true
                setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS)
            }
            mActionButton?.let {
                addViewToWindow(it, options)
            }
        }
        initCircularMenu()
        mIsViewAddToWindowManager = true
    }

    private fun initNewCircularMenu() {
        // Set up the white button on the lower right corner
        // more or less with default parameter
        // Set up the white button on the lower right corner
        // more or less with default parameter
        val fabIconNew = ImageView(mContext)
        fabIconNew.setImageDrawable(mContext.getResources().getDrawable(R.drawable.button_action))
        val params = FloatingActionButton.Builder.getDefaultSystemWindowParams(mContext)

        val rightLowerButton = FloatingActionButton.Builder(mContext)
            .setContentView(fabIconNew)
            .setSystemOverlay(true)
            .setLayoutParams(params)
            .build()

        val rLSubBuilder = SubActionButton.Builder(mContext)
        val rlIcon1 = ImageView(mContext)
        val rlIcon2 = ImageView(mContext)
        val rlIcon3 = ImageView(mContext)
        val rlIcon4 = ImageView(mContext)

        rlIcon1.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_add))
        rlIcon2.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_build_circle))
        rlIcon3.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_circle_record))
        rlIcon4.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_clear))

        // Build the menu with default options: light theme, 90 degrees, 72dp radius.
        // Set 4 default SubActionButtons

        // Build the menu with default options: light theme, 90 degrees, 72dp radius.
        // Set 4 default SubActionButtons
        val rlSub1 = rLSubBuilder.setContentView(rlIcon1).build()
        val rlSub2 = rLSubBuilder.setContentView(rlIcon2).build()
        val rlSub3 = rLSubBuilder.setContentView(rlIcon3).build()
        val rlSub4 = rLSubBuilder.setContentView(rlIcon4).build()
        val rightLowerMenu = FloatingActionMenu.Builder(mContext, true)
            .addSubActionView(rlSub1, rlSub1.layoutParams.width, rlSub1.layoutParams.height)
            .addSubActionView(rlSub2, rlSub2.layoutParams.width, rlSub2.layoutParams.height)
            .addSubActionView(rlSub3, rlSub3.layoutParams.width, rlSub3.layoutParams.height)
            .addSubActionView(rlSub4, rlSub4.layoutParams.width, rlSub4.layoutParams.height)
            .setStartAngle(180)
            .setEndAngle(270)
            .attachTo(rightLowerButton)
            .build()

        ////////////////////////////////////////////////////////

        // Set up the large red button on the top center side
        // With custom button and content sizes and margins

        ////////////////////////////////////////////////////////

        // Set up the large red button on the top center side
        // With custom button and content sizes and margins
        val redActionButtonSize: Int =
            mContext.getResources().getDimensionPixelSize(R.dimen.red_action_button_size)
        val redActionButtonMargin: Int =
            mContext.getResources().getDimensionPixelOffset(R.dimen.action_button_margin)
        val redActionButtonContentSize: Int =
            mContext.getResources().getDimensionPixelSize(R.dimen.red_action_button_content_size)
        val redActionButtonContentMargin: Int =
            mContext.getResources().getDimensionPixelSize(R.dimen.red_action_button_content_margin)
        val redActionMenuRadius: Int =
            mContext.getResources().getDimensionPixelSize(R.dimen.red_action_menu_radius)
        val blueSubActionButtonSize: Int =
            mContext.getResources().getDimensionPixelSize(R.dimen.blue_sub_action_button_size)
        val blueSubActionButtonContentMargin: Int =
            mContext.getResources()
                .getDimensionPixelSize(R.dimen.blue_sub_action_button_content_margin)

        val fabIconStar = ImageView(mContext)
        fabIconStar.setImageDrawable(
            mContext.getResources().getDrawable(R.drawable.ic_circle_record)
        )

        val fabIconStarParams =
            FrameLayout.LayoutParams(redActionButtonContentSize, redActionButtonContentSize)
        fabIconStarParams.setMargins(
            redActionButtonContentMargin,
            redActionButtonContentMargin,
            redActionButtonContentMargin,
            redActionButtonContentMargin
        )

        val params2 = FloatingActionButton.Builder.getDefaultSystemWindowParams(mContext)
        params2.width = redActionButtonSize
        params2.height = redActionButtonSize

        val topCenterButton = FloatingActionButton.Builder(mContext)
            .setSystemOverlay(true)
            .setContentView(fabIconStar, fabIconStarParams)
            .setBackgroundDrawable(R.drawable.button_action)
            .setPosition(FloatingActionButton.POSITION_TOP_CENTER)
            .setLayoutParams(params2)
            .build()

        // Set up customized SubActionButtons for the right center menu

        // Set up customized SubActionButtons for the right center menu
        val tCSubBuilder = SubActionButton.Builder(mContext)
        tCSubBuilder.setBackgroundDrawable(
            mContext.getResources().getDrawable(R.drawable.button_action_dark_selector)
        )

        val tCRedBuilder = SubActionButton.Builder(mContext)
        tCRedBuilder.setBackgroundDrawable(
            mContext.getResources().getDrawable(R.drawable.button_action_dark_selector)
        )

        val blueContentParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        blueContentParams.setMargins(
            blueSubActionButtonContentMargin,
            blueSubActionButtonContentMargin,
            blueSubActionButtonContentMargin,
            blueSubActionButtonContentMargin
        )

        // Set custom layout params

        // Set custom layout params
        val blueParams = FrameLayout.LayoutParams(blueSubActionButtonSize, blueSubActionButtonSize)
        tCSubBuilder.setLayoutParams(blueParams)
        tCRedBuilder.setLayoutParams(blueParams)

        val tcIcon1 = ImageView(mContext)
        val tcIcon2 = ImageView(mContext)
        val tcIcon3 = ImageView(mContext)
        val tcIcon4 = ImageView(mContext)
        val tcIcon5 = ImageView(mContext)
        val tcIcon6 = ImageView(mContext)

        tcIcon1.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_clear))
        tcIcon2.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_clear))
        tcIcon3.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_clear))
        tcIcon4.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_clear))
        tcIcon5.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_clear))
        tcIcon6.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_clear))

        val tcSub1 = tCSubBuilder.setContentView(tcIcon1, blueContentParams).build()
        val tcSub2 = tCSubBuilder.setContentView(tcIcon2, blueContentParams).build()
        val tcSub3 = tCSubBuilder.setContentView(tcIcon3, blueContentParams).build()
        val tcSub4 = tCSubBuilder.setContentView(tcIcon4, blueContentParams).build()
        val tcSub5 = tCSubBuilder.setContentView(tcIcon5, blueContentParams).build()
        val tcSub6 = tCRedBuilder.setContentView(tcIcon6, blueContentParams).build()


        // Build another menu with custom options


        // Build another menu with custom options
        val topCenterMenu = FloatingActionMenu.Builder(mContext, true)
            .addSubActionView(tcSub1, tcSub1.layoutParams.width, tcSub1.layoutParams.height)
            .addSubActionView(tcSub2, tcSub2.layoutParams.width, tcSub2.layoutParams.height)
            .addSubActionView(tcSub3, tcSub3.layoutParams.width, tcSub3.layoutParams.height)
            .addSubActionView(tcSub4, tcSub4.layoutParams.width, tcSub4.layoutParams.height)
            .addSubActionView(tcSub5, tcSub5.layoutParams.width, tcSub5.layoutParams.height)
            .addSubActionView(tcSub6, tcSub6.layoutParams.width, tcSub6.layoutParams.height)
            .setRadius(redActionMenuRadius)
            .setStartAngle(0)
            .setEndAngle(180)
            .attachTo(topCenterButton)
            .build()

        topCenterMenu.setStateChangeListener(object : MenuStateChangeListener {
            override fun onMenuOpened(menu: FloatingActionMenu) {}
            override fun onMenuClosed(menu: FloatingActionMenu) {
//                if (serviceWillBeDismissed) {
//                    this@SystemOverlayMenuService.stopSelf()
//                    serviceWillBeDismissed = false
//                }
            }
        })

        // make the red button terminate the service

        // make the red button terminate the service
        tcSub6.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //serviceWillBeDismissed = true // the order is important
                topCenterMenu.close(true)
            }
        })
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    private fun initCircularMenu() {
        //Init Git Repo as Private Mode.
        mContext.registerReceiver(mStopActionReceiver, IntentFilter(ACTION_STOP))
        mCircularMenu =
            LayoutInflater.from(mContext)
                .inflate(R.layout.floating_action_circle_menu, null, false) as ConstraintLayout
        mConstraintSetLTR.apply {
            clone(mCircularMenu)
            connect(
                R.id.btnClose,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            connect(R.id.btnClose, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            connect(
                R.id.btnClose,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
            connect(R.id.btnClose, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
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
            connect(
                R.id.btnClose,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
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
            UiNavigationManager.getInstance().launchPainter()
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
            UiNavigationManager.getInstance().launchSetting()
            mCloseBtn.callOnClick()
        }
        mHomeBtn.setOnClickListener {
            UiNavigationManager.getInstance().launchHome()
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
        mCircularMenuParams.gravity = Gravity.CENTER
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
                }
                    .alpha(0f)
                    .setDuration(0L)
                    .setStartDelay(0L)
                    .start()
            }

            mIsHideCircularMenu = false
            if (isRTL()) {
                mConstraintSetRTL.clear(R.id.btnClose, ConstraintSet.BOTTOM)
                mConstraintSetRTL.clear(R.id.btnClose, ConstraintSet.START)
                mConstraintSetRTL.setMargin(R.id.btnClose, ConstraintSet.TOP, mYPosition)
                mConstraintSetRTL.applyTo(mCircularMenu)
            } else {
                mConstraintSetLTR.clear(R.id.btnClose, ConstraintSet.END)
                mConstraintSetLTR.clear(R.id.btnClose, ConstraintSet.BOTTOM)
                mConstraintSetLTR.setMargin(R.id.btnClose, ConstraintSet.TOP, mYPosition)
                mConstraintSetLTR.applyTo(mCircularMenu)
            }
            val isAttachedToWindow = ViewCompat.isAttachedToWindow(mCircularMenu)

            Log.e(
                TAG,
                "Is Attached To Window : $isAttachedToWindow X:${mCircularMenuParams.x} ,Y:${mCircularMenuParams.y}"
            )
            if (isAttachedToWindow) {
                mCircularMenu.isGone = false
                mWindowManger.updateViewLayout(mCircularMenu, mCircularMenuParams)
            } else {
                mWindowManger.addView(mCircularMenu, mCircularMenuParams)
            }
        } else {
            mIsHideCircularMenu = true
        }

        val angle = if (mIsHideCircularMenu) 0f else 45f
        mCloseBtn.animate().apply { cancel() }
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
                        it.animate().apply { cancel() }
                            .alpha(1f)
                            .setDuration(100L)
                            .setStartDelay(0L)
                            .withEndAction { it.animate().alpha(0.25f).setDuration(3000L).start() }
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

                //Log.e(TAG, "side: $x ,$y", )

            }
            doOnEnd {
                if (isGone) {
                    this@side.isGone = isGone
                    if (isLastItem) {
                        if (mIsHideCircularMenu) {
                            mActionButton?.let {
                                it.animate().apply { cancel() }.alpha(1f)
                                    .setStartDelay(0L)
                                    .setStartDelay(0L)
                                    .withEndAction {
                                        it.animate().alpha(0.45f).setDuration(3000L).start()
                                    }
                                    .start()
                            }

                        }
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