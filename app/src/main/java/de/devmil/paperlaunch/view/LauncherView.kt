/*
 * Copyright 2015 Devmil Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.devmil.paperlaunch.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout

import java.io.InvalidClassException
import java.util.ArrayList

import de.devmil.paperlaunch.R
import de.devmil.paperlaunch.model.IEntry
import de.devmil.paperlaunch.model.IFolder
import de.devmil.paperlaunch.config.LaunchConfig
import de.devmil.paperlaunch.model.Launch
import de.devmil.paperlaunch.utils.PositionAndSizeEvaluator
import de.devmil.paperlaunch.view.utils.ViewUtils
import de.devmil.paperlaunch.view.utils.ColorUtils
import de.devmil.paperlaunch.view.widgets.VerticalTextView

class LauncherView : RelativeLayout {

    private var mViewModel: LauncherViewModel? = null
    private val mLaneViews = ArrayList<LaunchLaneView>()
    private var mBackground: RelativeLayout? = null
    private var mNeutralZone: LinearLayout? = null
    private var mNeutralZoneBackground: LinearLayout? = null
    private var mNeutralZoneBackgroundImage: ImageView? = null
    private var mNeutralZoneBackgroundAppNameText: VerticalTextView? = null
    private var mListener: ILauncherViewListener? = null
    private var mAutoStartMotionEvent: MotionEvent? = null

    private var mCurrentlySelectedItem: IEntry? = null

    interface ILauncherViewListener {
        fun onFinished()
    }

    constructor(context: Context) : super(context) {
        construct()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        construct()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        construct()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        construct()
    }

    fun doInitialize(config: LaunchConfig) {
        buildViewModel(config)
    }

    fun start() {
        buildViews()
        transitToState(LauncherViewModel.State.Init)
        transitToState(LauncherViewModel.State.Initializing)
    }

    fun setListener(listener: ILauncherViewListener) {
        mListener = listener
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (mAutoStartMotionEvent != null) {
            start()
            onTouchEvent(mAutoStartMotionEvent!!)
            mAutoStartMotionEvent = null
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var result = super.onTouchEvent(event)

        result = handleTouchEvent(event.action, event.x, event.y) || result

        return result
    }

    fun handleTouchEvent(action: Int, x: Float, y: Float): Boolean {
        var result = false
        var laneNum = 1

        //find the lane to dispatch to
        for (l in mLaneViews) {
            result = sendIfMatches(l, action, x, y, laneNum++) || result
        }

        return result
    }

    fun doAutoStart(firstMotionEvent: MotionEvent) {
        mAutoStartMotionEvent = firstMotionEvent
    }

    private fun construct() {
        ViewUtils.disableClipping(this)
    }

    private fun buildViewModel(config: LaunchConfig) {
        mViewModel = LauncherViewModel(config)
    }

    private val laneIds: IntArray
        get() = intArrayOf(R.id.id_launchview_lane1, R.id.id_launchview_lane2, R.id.id_launchview_lane3, R.id.id_launchview_lane4, R.id.id_launchview_lane5, R.id.id_launchview_lane6, R.id.id_launchview_lane7, R.id.id_launchview_lane8, R.id.id_launchview_lane9, R.id.id_launchview_lane10, R.id.id_launchview_lane11, R.id.id_launchview_lane12, R.id.id_launchview_lane13)

    private fun buildViews() {
        removeAllViews()
        mLaneViews.clear()
        addBackground()
        addNeutralZone()
        var currentAnchor = mNeutralZone!!.id
        val laneIds = laneIds

        for (i in laneIds.indices) {
            currentAnchor = addLaneView(i, currentAnchor).id
        }

        setEntriesToLane(mLaneViews[0], mViewModel!!.entries)

        for (i in mLaneViews.indices.reversed()) {
            mLaneViews[i].bringToFront()
        }
        mNeutralZone!!.bringToFront()
        mNeutralZoneBackground!!.bringToFront()
    }

    private fun addLaneView(laneIndex: Int, anchorId: Int): LaunchLaneView {
        val laneIds = laneIds
        val id = laneIds[laneIndex]

        val llv = LaunchLaneView(context)
        llv.id = id

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        if (mViewModel!!.isOnRightSide)
            params.addRule(RelativeLayout.LEFT_OF, anchorId)
        else
            params.addRule(RelativeLayout.RIGHT_OF, anchorId)

        addView(llv, params)
        mLaneViews.add(llv)

        llv.setLaneListener(object : LaunchLaneView.ILaneListener {
            override fun onItemSelected(selectedItem: IEntry?) {
                mCurrentlySelectedItem = selectedItem
                if (selectedItem == null) {
                    return
                }
                if (selectedItem.isFolder) {
                    val f = selectedItem as IFolder?
                    if (mLaneViews.size <= laneIndex + 1) {
                        return
                    }
                    val nextLaneView = mLaneViews[laneIndex + 1]
                    setEntriesToLane(nextLaneView, f!!.subEntries.orEmpty())
                    nextLaneView.start()
                }
            }

            override fun onItemSelecting(selectedItem: IEntry?) {
                mCurrentlySelectedItem = selectedItem
            }

            override fun onStateChanged(oldState: LaunchLaneViewModel.State, newState: LaunchLaneViewModel.State) {
                if (newState === LaunchLaneViewModel.State.Focusing) {
                    for (idx in laneIndex + 1..mLaneViews.size - 1) {
                        mLaneViews[idx].stop()
                    }
                }
            }
        })

        return llv
    }

    private fun setEntriesToLane(laneView: LaunchLaneView, entries: List<IEntry>) {
        val entryModels = entries.map { LaunchEntryViewModel.createFrom(context, it, mViewModel!!.entryConfig) }

        val vm = LaunchLaneViewModel(entryModels, mViewModel!!.laneConfig)
        laneView.doInitializeData(vm)
    }

    private fun addBackground() {
        mBackground = RelativeLayout(context)
        mBackground!!.setBackgroundColor(mViewModel!!.backgroundColor)
        mBackground!!.alpha = 0f

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

        addView(mBackground, params)
    }

    private fun addNeutralZone() {
        mNeutralZone = LinearLayout(context)
        mNeutralZone!!.id = R.id.id_launchview_neutralzone
        mNeutralZone!!.minimumWidth = ViewUtils.getPxFromDip(context, mViewModel!!.neutralZoneWidthDip).toInt()
        mNeutralZone!!.isClickable = false

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        if (mViewModel!!.isOnRightSide)
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        else
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)

        addView(mNeutralZone, params)

        mNeutralZoneBackground = LinearLayout(context)
        mNeutralZoneBackground!!.setBackgroundColor(mViewModel!!.designConfig.frameDefaultColor)
        mNeutralZoneBackground!!.elevation = ViewUtils.getPxFromDip(context, mViewModel!!.highElevationDip)
        mNeutralZoneBackground!!.isClickable = false
        mNeutralZoneBackground!!.orientation = LinearLayout.VERTICAL
        mNeutralZoneBackground!!.setGravity(Gravity.CENTER_HORIZONTAL)

        val backParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )
        mNeutralZone!!.addView(mNeutralZoneBackground, backParams)

        mNeutralZoneBackgroundImage = ImageView(context)
        mNeutralZoneBackgroundImage!!.setImageResource(R.mipmap.ic_launcher)
        mNeutralZoneBackgroundImage!!.isClickable = false
        mNeutralZoneBackgroundImage!!.visibility = View.GONE

        val backImageParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        backImageParams.setMargins(0, ViewUtils.getPxFromDip(context, mViewModel!!.laneConfig.laneIconTopMarginDip).toInt(), 0, 0)
        mNeutralZoneBackground!!.addView(mNeutralZoneBackgroundImage, backImageParams)

        mNeutralZoneBackground!!.setBackgroundColor(
                ColorUtils.getBackgroundColorFromImage(
                        resources.getDrawable(
                                R.mipmap.ic_launcher,
                                context.theme
                        ),
                        mViewModel!!.frameDefaultColor))

        mNeutralZoneBackgroundAppNameText = VerticalTextView(context)
        mNeutralZoneBackgroundAppNameText!!.visibility = View.GONE
        mNeutralZoneBackgroundAppNameText!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, mViewModel!!.itemNameTextSizeSP)
        //this is needed because the parts in the system run with another theme than the application parts
        mNeutralZoneBackgroundAppNameText!!.setTextColor(ContextCompat.getColor(context, R.color.name_label))
        mNeutralZoneBackgroundAppNameText!!.setText(R.string.app_name)
        mNeutralZoneBackgroundAppNameText!!.visibility = View.GONE

        val backTextParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        backTextParams.setMargins(0, ViewUtils.getPxFromDip(context, mViewModel!!.laneConfig.laneTextTopMarginDip).toInt(), 0, 0)

        mNeutralZoneBackground!!.addView(mNeutralZoneBackgroundAppNameText, backTextParams)
    }

    private fun sendIfMatches(laneView: LaunchLaneView, action: Int, x: Float, y: Float, laneNumber: Int): Boolean {
        val laneX = (x - laneView.x).toInt()
        val laneY = (y - laneView.y).toInt()
        laneView.doHandleTouch(action, laneX, laneY)
        if (action == MotionEvent.ACTION_UP) {
            launchAppIfSelected()
            if (mListener != null) {
                mListener!!.onFinished()
            }
        }
        return true
    }

    private fun launchAppIfSelected() {
        if (mCurrentlySelectedItem == null) {
            return
        }
        if (mCurrentlySelectedItem!!.isFolder) {
            return
        }
        val l = mCurrentlySelectedItem as Launch?
        val intent = l!!.launchIntent
        if (intent != null) {
            intent.flags = intent.flags or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error while launching app", e)
        }

    }

    private fun transitToState(newState: LauncherViewModel.State) {
        when (newState) {
            LauncherViewModel.State.Init -> {
                hideBackground()
                hideNeutralZone()
            }
            LauncherViewModel.State.Initializing -> {
                animateBackground()
                animateNeutralZone()
            }
            LauncherViewModel.State.Ready -> startLane()
        }
        mViewModel!!.state = newState
    }

    private fun startLane() {
        mLaneViews[0].start()
    }

    private fun hideBackground() {
        mBackground!!.alpha = 0f
    }

    private fun hideNeutralZone() {
        mNeutralZoneBackground!!.visibility = View.INVISIBLE
    }

    private fun animateBackground() {
        if (mViewModel!!.showBackground()) {
            mBackground!!.visibility = View.VISIBLE
            mBackground!!
                    .animate()
                    .alpha(mViewModel!!.backgroundAlpha)
                    .setDuration(mViewModel!!.backgroundAnimationDurationMS.toLong())
                    .start()
        } else {
            mBackground!!.visibility = View.GONE
        }
    }

    private fun animateNeutralZone() {
        val size = mViewModel!!.neutralZoneWidthDip

        val fromLeft = if (mViewModel!!.isOnRightSide) width - size.toInt() else 0
        var fromTop = (height - size.toInt()) / 2

        if (mAutoStartMotionEvent != null) {
            fromTop = Math.min(
                    height - size.toInt(),
                    mAutoStartMotionEvent!!.y.toInt()
            )
        }

        val fromRight = fromLeft + size.toInt()
        val fromBottom = fromTop + size.toInt()

        val fromRect = Rect(
                fromLeft,
                fromTop,
                fromRight,
                fromBottom
        )
        val toRect = Rect(
                fromLeft,
                0,
                fromRight,
                height
        )

        val anim: ObjectAnimator?
        try {
            anim = ObjectAnimator.ofObject(
                    mNeutralZoneBackground,
                    "margins",
                    PositionAndSizeEvaluator(mNeutralZoneBackground!!),
                    fromRect,
                    toRect)
            anim!!.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    mNeutralZoneBackground!!.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    Thread(Runnable {
                        try {
                            Thread.sleep(100)
                        } catch (e: InterruptedException) {
                        }

                        mNeutralZoneBackground!!.post { transitToState(LauncherViewModel.State.Ready) }
                    }).start()
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })
            anim.duration = mViewModel!!.launcherInitAnimationDurationMS.toLong()
            anim.start()

            Thread(Runnable {
                try {
                    Thread.sleep((mViewModel!!.launcherInitAnimationDurationMS / 2).toLong())
                    mNeutralZoneBackgroundImage!!.post {
                        mNeutralZoneBackgroundImage!!.visibility = View.VISIBLE
                        mNeutralZoneBackgroundAppNameText!!.visibility = View.VISIBLE
                    }
                } catch (e: InterruptedException) {
                }
            }).start()

        } catch (e: InvalidClassException) {
            e.printStackTrace()
        }

    }

    companion object {

        private val TAG = LauncherView::class.java.simpleName
    }
}
