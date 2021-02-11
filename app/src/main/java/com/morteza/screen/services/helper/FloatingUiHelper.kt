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
import android.widget.Toast
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

    private val windowManger by lazy { mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val radius by lazy { mContext.resources.getDimensionPixelSize(R.dimen.radius) }
    private val actionButtonSize by lazy { mContext.resources.getDimensionPixelSize(R.dimen.floating_icon_size) }
    private val overMargin by lazy { (2 * mDisplayInfo.metrics.density).toInt() }

    private var circularMenuParams: WindowManager.LayoutParams? = null
    private var actionButton: AppCompatImageView? = null
    private lateinit var circularMenu: ConstraintLayout
    private lateinit var flatAppBtn: AppCompatImageView
    private lateinit var toolsBtn: AppCompatImageView
    private lateinit var startRecordBtn: AppCompatImageView
    private lateinit var settingsBtn: AppCompatImageView
    private lateinit var homeBtn: AppCompatImageView

    val mDisplayInfo by lazy { DisplayInfo(windowManger) }

    private var floatingViewManager: FloatingViewManager? = null
    private val constraintSetRTL = ConstraintSet()
    private val constraintSetLTR = ConstraintSet()

    private lateinit var gestureDetector: GestureDetector
    private var isHideCircularMenu = true
    private var xPosition: Int = 0
    private var yPosition: Int = 0
    private val isMoveToEdge = true // For FloatingViewManager.MOVE_DIRECTION_THROWN
    var isViewAddToWindowManager = false


    @SuppressLint("InflateParams")
    fun initFloatingView(intent: Intent) {

        val inflater = LayoutInflater.from(mContext)
        actionButton =
            inflater.inflate(R.layout.floating_action_button, null, false) as AppCompatImageView
        actionButton?.setOnClickListener {
            closeOpenCircleMenu()
        }


        val rect = intent.getParcelableExtra<Rect>(Constants.EXTRA_CUTOUT_SAFE_AREA)
        floatingViewManager = FloatingViewManager(mContext, this).apply {
            setFixedTrashIconImage(R.drawable.ic_trash_fixed)
            setActionTrashIconImage(R.drawable.ic_trash_action)
            setSafeInsetRect(rect)

            val options = FloatingViewManager.Options().apply {
                moveDirection = FloatingViewManager.MOVE_DIRECTION_THROWN
                overMargin = this@FloatingUiHelper.overMargin
                floatingViewX =
                    mDisplayInfo.metrics.widthPixels - actionButtonSize - radius + overMargin
                floatingViewY = (mDisplayInfo.metrics.heightPixels - actionButtonSize) / 2
                isTrashViewEnabled = false
                setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS)
            }
            actionButton?.let {
                addViewToWindow(it, options)
            }
        }

        initCircularMenu()

        isViewAddToWindowManager = true
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    private fun initCircularMenu() {

        //Init Git Repo as Private Mode.
        mContext.registerReceiver(mStopActionReceiver, IntentFilter(ACTION_STOP))

        val inflater = LayoutInflater.from(mContext)

        circularMenu =
            inflater.inflate(R.layout.floating_action_circle_menu, null, false) as ConstraintLayout

        constraintSetLTR.apply {
            clone(circularMenu)
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

        constraintSetRTL.apply {
            clone(circularMenu)
            connect(R.id.btnClose, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            connect(R.id.btnClose, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(
                R.id.btnClose,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
        }
        flatAppBtn = circularMenu.findViewById(R.id.btnClose)
        toolsBtn = circularMenu.findViewById(R.id.tools_btn)
        startRecordBtn = circularMenu.findViewById(R.id.start_record_btn)
        settingsBtn = circularMenu.findViewById(R.id.settings_btn)
        homeBtn = circularMenu.findViewById(R.id.home_btn)

        flatAppBtn.setOnClickListener {
            closeOpenCircleMenu()
        }

        toolsBtn.setOnClickListener {
            Toast.makeText(it.context, "Exit the app", Toast.LENGTH_LONG).show()
            flatAppBtn.callOnClick()
            onFinishFloatingView()
        }

        startRecordBtn.setOnClickListener {
            ScreenApp.getScreenshotPermission();
            flatAppBtn.callOnClick()
        }

        settingsBtn.setOnClickListener {
            Toast.makeText(it.context, "button4", Toast.LENGTH_LONG).show()

            //stopRecorder()

            flatAppBtn.callOnClick()
        }

        homeBtn.setOnClickListener {
            Toast.makeText(it.context, "stop recording and open the file", Toast.LENGTH_LONG).show()
            flatAppBtn.callOnClick()

            mScreenRecordControl.stopRecording()
        }

        val gesture = object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                closeOpenCircleMenu()
                return super.onSingleTapConfirmed(e)
            }
        }

        gestureDetector = GestureDetector(mContext, gesture)

        circularMenu.setOnTouchListener(FloatingButtonTouchListener())

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PRIORITY_PHONE
        }

        circularMenuParams = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    //or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSPARENT
        )

        circularMenuParams!!.gravity = Gravity.TOP or Gravity.START
    }

    private val mStopActionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_STOP == intent.action) {
                mScreenRecordControl.stopRecording()
            }
        }
    }

    fun closeOpenCircleMenu() {

        if (isHideCircularMenu) {

            actionButton?.let {
                it.animate().apply {
                    cancel()
                }.alpha(0f)
                    .setDuration(0L)
                    .setStartDelay(0L)
                    .start()

                circularMenuParams!!.y =
                    yPosition + (it.height - circularMenuParams!!.height) / 2
            }

            isHideCircularMenu = false

            circularMenuParams!!.x = if (!isMoveToEdge) {
                if (isRTL()) xPosition - circularMenuParams!!.width + actionButtonSize else xPosition
            } else {
                if (isRTL()) getScreenWidth() - circularMenuParams!!.width + overMargin else -overMargin
            }



            if (isRTL()) {
                constraintSetRTL.clear(R.id.btnClose, ConstraintSet.START)
                constraintSetRTL.applyTo(circularMenu)
            } else {
                constraintSetLTR.clear(R.id.btnClose, ConstraintSet.END)
                constraintSetLTR.applyTo(circularMenu)
            }

            if (!ViewCompat.isAttachedToWindow(circularMenu)) {
                windowManger.addView(circularMenu, circularMenuParams)
            } else {
                circularMenu.isGone = false
                windowManger.updateViewLayout(circularMenu, circularMenuParams)
            }
        } else {
            isHideCircularMenu = true
        }

        val angle = if (isHideCircularMenu) 0f else 45f
        flatAppBtn.animate().apply {
            cancel()
        }
            .rotation(angle)
            .setDuration(100L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withStartAction {
                val count = circularMenu.childCount - 1
                circularMenu.forEachIndexed { index, view ->
                    if (view.id != R.id.btnClose) {
                        if (!isHideCircularMenu) view.isGone = isHideCircularMenu
                        view.side(isHideCircularMenu, radius, isRTL(), index == count - 1)
                    }
                }
            }
            .withEndAction {
                if (isHideCircularMenu) {
                    actionButton?.let {
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
                        circularMenu.isGone = true
                    }
                }
            }
        }.start()
    }

    private fun isRTL() = xPosition > mDisplayInfo.metrics.widthPixels / 2

    private fun getScreenWidth() =
        if (mContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            mDisplayInfo.metrics.heightPixels
        else
            mDisplayInfo.metrics.widthPixels

    private inner class FloatingButtonTouchListener : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            gestureDetector.onTouchEvent(event)
            when (event.actionMasked) {
                MotionEvent.ACTION_OUTSIDE -> {
                    if (!circularMenu.isGone) {
                        flatAppBtn.callOnClick()
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
        floatingViewManager?.removeAllViewToWindow()
        try {
            windowManger.removeView(circularMenu)
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
            xPosition = x
            yPosition = y
            Log.d(TAG, mContext.getString(R.string.touch_finished_position, x, y))
        }
    }
}