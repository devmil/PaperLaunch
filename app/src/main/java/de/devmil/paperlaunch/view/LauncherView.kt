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
import android.annotation.SuppressLint
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

    private var viewModel: LauncherViewModel? = null
    private val laneViews = ArrayList<LaunchLaneView>()
    private var background: RelativeLayout? = null
    private var neutralZone: LinearLayout? = null
    private var neutralZoneBackground: LinearLayout? = null
    private var neutralZoneBackgroundImage: ImageView? = null
    private var neutralZoneBackgroundAppNameText: VerticalTextView? = null
    private var listener: ILauncherViewListener? = null
    private var autoStartMotionEvent: MotionEvent? = null

    private var currentlySelectedItem: IEntry? = null

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

    fun doInitialize(config: LaunchConfig) {
        buildViewModel(config)
    }

    private fun start() {
        buildViews()
        transitToState(LauncherViewModel.State.Init)
        transitToState(LauncherViewModel.State.Initializing)
    }

    fun setListener(listener: ILauncherViewListener) {
        this.listener = listener
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (autoStartMotionEvent != null) {
            start()
            onTouchEvent(autoStartMotionEvent!!)
            autoStartMotionEvent = null
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event) || handleTouchEvent(event.action, event.x, event.y)
    }

    fun handleTouchEvent(action: Int, x: Float, y: Float): Boolean {
        return laneViews.fold(false) { currentResult, laneView ->
            sendIfMatches(laneView, action, x, y) || currentResult
        }
    }

    fun doAutoStart(firstMotionEvent: MotionEvent) {
        autoStartMotionEvent = firstMotionEvent
    }

    private fun construct() {
        ViewUtils.disableClipping(this)
    }

    private fun buildViewModel(config: LaunchConfig) {
        viewModel = LauncherViewModel(config)
    }

    private val laneIds: IntArray
        get() = intArrayOf(
                R.id.id_launchview_lane1,
                R.id.id_launchview_lane2,
                R.id.id_launchview_lane3,
                R.id.id_launchview_lane4,
                R.id.id_launchview_lane5,
                R.id.id_launchview_lane6,
                R.id.id_launchview_lane7,
                R.id.id_launchview_lane8,
                R.id.id_launchview_lane9,
                R.id.id_launchview_lane10,
                R.id.id_launchview_lane11,
                R.id.id_launchview_lane12,
                R.id.id_launchview_lane13)

    private fun buildViews() {
        removeAllViews()
        laneViews.clear()
        addBackground()
        addNeutralZone()
        val laneIds = laneIds

        laneIds.indices.fold(neutralZone!!.id) { current, i ->
            addLaneView(i, current).id
        }

        setEntriesToLane(laneViews[0], viewModel!!.entries)

        laneViews.indices.reversed().forEach { i ->
            laneViews[i].bringToFront()
        }

        neutralZone!!.bringToFront()
        neutralZoneBackground!!.bringToFront()
    }

    private fun addLaneView(laneIndex: Int, anchorId: Int): LaunchLaneView {
        val laneIds = laneIds
        val id = laneIds[laneIndex]

        val llv = LaunchLaneView(context)
        llv.id = id

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        if (viewModel!!.isOnRightSide)
            params.addRule(RelativeLayout.LEFT_OF, anchorId)
        else
            params.addRule(RelativeLayout.RIGHT_OF, anchorId)

        addView(llv, params)
        laneViews.add(llv)

        llv.setLaneListener(object : LaunchLaneView.ILaneListener {
            override fun onItemSelected(selectedItem: IEntry?) {
                currentlySelectedItem = selectedItem
                if (selectedItem == null) {
                    return
                }
                if (selectedItem.isFolder) {
                    val f = selectedItem as IFolder?
                    if (laneViews.size <= laneIndex + 1) {
                        return
                    }
                    val nextLaneView = laneViews[laneIndex + 1]
                    setEntriesToLane(nextLaneView, f!!.subEntries.orEmpty())
                    nextLaneView.start()
                }
            }

            override fun onItemSelecting(selectedItem: IEntry?) {
                currentlySelectedItem = selectedItem
            }

            override fun onStateChanged(oldState: LaunchLaneViewModel.State, newState: LaunchLaneViewModel.State) {
                if (newState === LaunchLaneViewModel.State.Focusing) {
                    for (idx in laneIndex + 1 until laneViews.size) {
                        laneViews[idx].stop()
                    }
                }
            }
        })

        return llv
    }

    private fun setEntriesToLane(laneView: LaunchLaneView, entries: List<IEntry>) {
        val entryModels = entries.map { LaunchEntryViewModel.createFrom(context, it, viewModel!!.entryConfig) }

        val vm = LaunchLaneViewModel(entryModels, viewModel!!.laneConfig)
        laneView.doInitializeData(vm)
    }

    private fun addBackground() {
        background = RelativeLayout(context)
        background!!.setBackgroundColor(viewModel!!.backgroundColor)
        background!!.alpha = 0f

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

        addView(background, params)
    }

    private fun addNeutralZone() {
        neutralZone = LinearLayout(context)
        neutralZone!!.id = R.id.id_launchview_neutralzone
        neutralZone!!.minimumWidth = ViewUtils.getPxFromDip(context, viewModel!!.neutralZoneWidthDip).toInt()
        neutralZone!!.isClickable = false

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        if (viewModel!!.isOnRightSide) {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        }
        else {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        }

        addView(neutralZone, params)

        neutralZoneBackground = LinearLayout(context)
        neutralZoneBackground!!.setBackgroundColor(viewModel!!.designConfig.frameDefaultColor)
        neutralZoneBackground!!.elevation = ViewUtils.getPxFromDip(context, viewModel!!.highElevationDip)
        neutralZoneBackground!!.isClickable = false
        neutralZoneBackground!!.orientation = LinearLayout.VERTICAL
        neutralZoneBackground!!.gravity = Gravity.CENTER_HORIZONTAL

        val backParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )
        neutralZone!!.addView(neutralZoneBackground, backParams)

        neutralZoneBackgroundImage = ImageView(context)
        neutralZoneBackgroundImage!!.setImageResource(R.mipmap.ic_launcher)
        neutralZoneBackgroundImage!!.isClickable = false
        neutralZoneBackgroundImage!!.visibility = View.GONE

        val backImageParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        backImageParams.setMargins(0, ViewUtils.getPxFromDip(context, viewModel!!.laneConfig.laneIconTopMarginDip).toInt(), 0, 0)
        neutralZoneBackground!!.addView(neutralZoneBackgroundImage, backImageParams)

        neutralZoneBackground!!.setBackgroundColor(
                ColorUtils.getBackgroundColorFromImage(
                        resources.getDrawable(
                                R.mipmap.ic_launcher,
                                context.theme
                        ),
                        viewModel!!.frameDefaultColor))

        neutralZoneBackgroundAppNameText = VerticalTextView(context)
        neutralZoneBackgroundAppNameText!!.visibility = View.GONE
        neutralZoneBackgroundAppNameText!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, viewModel!!.itemNameTextSizeSP)
        //this is needed because the parts in the system run with another theme than the application parts
        neutralZoneBackgroundAppNameText!!.setTextColor(ContextCompat.getColor(context, R.color.name_label))
        neutralZoneBackgroundAppNameText!!.setText(R.string.app_name)
        neutralZoneBackgroundAppNameText!!.visibility = View.GONE

        val backTextParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        backTextParams.setMargins(0, ViewUtils.getPxFromDip(context, viewModel!!.laneConfig.laneTextTopMarginDip).toInt(), 0, 0)

        neutralZoneBackground!!.addView(neutralZoneBackgroundAppNameText, backTextParams)
    }

    private fun sendIfMatches(laneView: LaunchLaneView, action: Int, x: Float, y: Float): Boolean {
        val laneX = (x - laneView.x).toInt()
        val laneY = (y - laneView.y).toInt()
        laneView.doHandleTouch(action, laneX, laneY)
        if (action == MotionEvent.ACTION_UP) {
            launchAppIfSelected()
            listener?.onFinished()
        }
        return true
    }

    private fun launchAppIfSelected() {
        if (currentlySelectedItem?.isFolder != false) {
            return
        }
        val l = currentlySelectedItem as Launch?
        val intent = l?.launchIntent
        intent?.let {
            it.flags = it.flags or Intent.FLAG_ACTIVITY_NEW_TASK
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
        viewModel!!.state = newState
    }

    private fun startLane() {
        laneViews[0].start()
    }

    private fun hideBackground() {
        background?.alpha = 0f
    }

    private fun hideNeutralZone() {
        neutralZoneBackground?.visibility = View.INVISIBLE
    }

    private fun animateBackground() {
        if (viewModel?.showBackground == true) {
            background!!.visibility = View.VISIBLE
            background!!
                    .animate()
                    .alpha(viewModel!!.backgroundAlpha)
                    .setDuration(viewModel!!.backgroundAnimationDurationMS.toLong())
                    .start()
        } else {
            background?.visibility = View.GONE
        }
    }

    private fun animateNeutralZone() {
        val size = viewModel!!.neutralZoneWidthDip

        val fromLeft = if (viewModel!!.isOnRightSide) width - size.toInt() else 0
        var fromTop = (height - size.toInt()) / 2

        if (autoStartMotionEvent != null) {
            fromTop = Math.min(
                    height - size.toInt(),
                    autoStartMotionEvent!!.y.toInt()
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
                    neutralZoneBackground,
                    "margins",
                    PositionAndSizeEvaluator(neutralZoneBackground!!),
                    fromRect,
                    toRect)
            anim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    neutralZoneBackground!!.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    Thread(Runnable {
                        try {
                            Thread.sleep(100)
                        } catch (e: InterruptedException) {
                        }

                        neutralZoneBackground!!.post { transitToState(LauncherViewModel.State.Ready) }
                    }).start()
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })
            anim.duration = viewModel!!.launcherInitAnimationDurationMS.toLong()
            anim.start()

            Thread(Runnable {
                try {
                    Thread.sleep((viewModel!!.launcherInitAnimationDurationMS / 2).toLong())
                    neutralZoneBackgroundImage!!.post {
                        neutralZoneBackgroundImage!!.visibility = View.VISIBLE
                        neutralZoneBackgroundAppNameText!!.visibility = View.VISIBLE
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
