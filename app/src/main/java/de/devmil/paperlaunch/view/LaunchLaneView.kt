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
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout

import java.util.ArrayList

import de.devmil.paperlaunch.R
import de.devmil.paperlaunch.config.LauncherGravity
import de.devmil.paperlaunch.model.IEntry
import de.devmil.paperlaunch.utils.BitmapUtils
import de.devmil.paperlaunch.utils.PositionAndSizeEvaluator
import de.devmil.paperlaunch.view.utils.ViewUtils
import de.devmil.paperlaunch.view.utils.ColorUtils
import de.devmil.paperlaunch.view.widgets.VerticalTextView

class LaunchLaneView : RelativeLayout {
    interface ILaneListener {
        fun onItemSelected(selectedItem: IEntry?)
        fun onItemSelecting(selectedItem: IEntry?)
        fun onStateChanged(oldState: LaunchLaneViewModel.State, newState: LaunchLaneViewModel.State)
    }

    private var viewModel: LaunchLaneViewModel? = null
    private var laneListener: ILaneListener? = null

    //view components
    private var selectIndicatorContainer: LinearLayout? = null
    private var selectIndicator: LinearLayout? = null
    private var entriesContainer: LinearLayout? = null
    private var selectedIcon: ImageView? = null
    private var selectedItemTextView: VerticalTextView? = null
    private val entryViews = ArrayList<LaunchEntryView>()
    private var focusedEntryView: LaunchEntryView? = null

    constructor(context: Context) : super(context) {
        construct()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        construct()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        construct()
    }

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        construct()
    }

    fun doInitializeData(viewModel: LaunchLaneViewModel) {
        this.viewModel = viewModel

        createViews()
        createEntryViews()
        adaptModelState()
    }

    fun setLaneListener(listener: ILaneListener) {
        laneListener = listener
    }

    fun start() {
        gotoState(LaunchLaneViewModel.State.Focusing)
    }

    fun stop() {
        removeAllViews()
        viewModel?.let {
            it.state = LaunchLaneViewModel.State.Init
        }
    }

    private fun gotoState(state: LaunchLaneViewModel.State) {
        transitToState(state)
    }

    fun doHandleTouch(action: Int, x: Int, y: Int) {
        val focusSelectionBorder = width
        viewModel?.let {
            if (it.state === LaunchLaneViewModel.State.Focusing) {
                if (action == MotionEvent.ACTION_UP) {
                    sendAllEntriesToState(LaunchEntryViewModel.State.Active)
                    focusedEntryView = null
                } else {
                    ensureFocusedEntryAt(y)
                    if (focusedEntryView != null) {
                        if (it.isOnRightSide) {
                            if (x < focusSelectionBorder) {
                                transitToState(LaunchLaneViewModel.State.Selecting)
                            }
                        } else {
                            if (x > 0) {
                                transitToState(LaunchLaneViewModel.State.Selecting)
                            }
                        }
                    }
                }
            } else if (it.state === LaunchLaneViewModel.State.Selected) {
                if (it.isOnRightSide) {
                    if (x > focusSelectionBorder)
                        transitToState(LaunchLaneViewModel.State.Focusing)
                } else {
                    if (x < 0)
                        transitToState(LaunchLaneViewModel.State.Focusing)
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (entryViews.size > 0) {
            setMeasuredDimension(entryViews[0].measuredWidth, measuredHeight)
        }
    }

    private fun construct() {
        ViewUtils.disableClipping(this)
    }

    private fun createViews() {
        removeAllViews()
        entriesContainer = LinearLayout(context)
        entriesContainer!!.orientation = LinearLayout.VERTICAL

        val localViewModel = viewModel!!

        when (localViewModel.launcherGravity) {
            LauncherGravity.Top -> entriesContainer!!.gravity = Gravity.TOP
            LauncherGravity.Center -> entriesContainer!!.gravity = Gravity.CENTER_VERTICAL
            LauncherGravity.Bottom -> entriesContainer!!.gravity = Gravity.BOTTOM
        }

        val entriesContainerParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        entriesContainerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        entriesContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        if (localViewModel.isOnRightSide) {
            entriesContainerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        } else {
            entriesContainerParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        }

        addView(entriesContainer, entriesContainerParams)
        ViewUtils.disableClipping(entriesContainer!!)


        selectIndicatorContainer = LinearLayout(context)
        selectIndicatorContainer!!.orientation = LinearLayout.VERTICAL

        val indicatorContainerParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        indicatorContainerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        indicatorContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        indicatorContainerParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        indicatorContainerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

        addView(selectIndicatorContainer, indicatorContainerParams)
        ViewUtils.disableClipping(selectIndicatorContainer!!)


        selectIndicator = LinearLayout(context)
        selectIndicator!!.setBackgroundColor(localViewModel.frameDefaultColor)
        selectIndicator!!.elevation = ViewUtils.getPxFromDip(context, localViewModel.selectedImageElevationDip)
        selectIndicator!!.visibility = View.INVISIBLE
        selectIndicator!!.gravity = Gravity.CENTER_HORIZONTAL
        selectIndicator!!.orientation = LinearLayout.VERTICAL

        val selectIndicatorParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)

        selectIndicatorContainer!!.addView(selectIndicator, selectIndicatorParams)
        ViewUtils.disableClipping(selectIndicator!!)

        selectedIcon = ImageView(context)
        selectedIcon!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
        selectedIcon!!.setImageResource(localViewModel.unknownAppImageId)

        val selectIconParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        selectIconParams.setMargins(0, ViewUtils.getPxFromDip(context, localViewModel.laneIconTopMarginDip).toInt(), 0, 0)

        selectIndicator!!.addView(selectedIcon, selectIconParams)

        selectedItemTextView = VerticalTextView(context)
        selectedItemTextView!!.visibility = View.GONE
        selectedItemTextView!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, localViewModel.itemNameTextSizeSP)
        //this is needed because the parts in the system run with another theme than the application parts
        selectedItemTextView!!.setTextColor(ContextCompat.getColor(context, R.color.name_label))

        val selectedItemTextViewParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        selectedItemTextViewParams.setMargins(0, ViewUtils.getPxFromDip(context, localViewModel.laneTextTopMarginDip).toInt(), 0, 0)
        selectIndicator!!.addView(selectedItemTextView, selectedItemTextViewParams)
    }

    private fun createEntryViews() {
        entriesContainer!!.removeAllViews()
        entryViews.clear()

        val entries = viewModel!!.entries
        for (e in entries) {
            val ev = LaunchEntryView(context)
            entryViews.add(ev)
            val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            entriesContainer!!.addView(ev, params)

            ev.doInitialize(e)
        }
    }

    private fun transitToState(state: LaunchLaneViewModel.State) {
        when (state) {
            LaunchLaneViewModel.State.Init -> {
                fireNotSelectedEvents()
                hideSelectionIndicator()
                hideEntries()
                initEntryState(LaunchEntryViewModel.State.Inactive)
            }
            LaunchLaneViewModel.State.Focusing -> {
                fireNotSelectedEvents()
                hideSelectionIndicator()
                showEntries()
                sendAllEntriesToState(LaunchEntryViewModel.State.Active)
            }
            LaunchLaneViewModel.State.Selecting -> {
                showSelectionIndicator()
                sendAllEntriesToState(LaunchEntryViewModel.State.Inactive, focusedEntryView)
                fireSelectingEvent()
            }
            LaunchLaneViewModel.State.Selected -> fireSelectedEvent()
        }
        viewModel?.let {
            val oldState = it.state
            it.state = state
            fireStateChangedEvent(oldState, state)
        }
    }

    private fun fireStateChangedEvent(oldState: LaunchLaneViewModel.State, newState: LaunchLaneViewModel.State) {
        if (laneListener != null) {
            laneListener!!.onStateChanged(oldState, newState)
        }
    }

    private fun fireSelectedEvent() {
        if (laneListener != null) {
            laneListener!!.onItemSelected(focusedEntryView!!.entry)
        }
    }

    private fun fireSelectingEvent() {
        if (laneListener != null) {
            laneListener!!.onItemSelecting(focusedEntryView!!.entry)
        }
    }

    private fun fireNotSelectedEvents() {
        if (laneListener != null) {
            laneListener!!.onItemSelected(null)
            laneListener!!.onItemSelecting(null)
        }
    }

    private fun adaptModelState() {
        transitToState(viewModel!!.state)
        applySizeParameters()
    }

    private fun initEntryState(state: LaunchEntryViewModel.State) {
        for (ev in entryViews) {
            ev.setState(state)
        }
    }

    private fun sendAllEntriesToState(state: LaunchEntryViewModel.State, except: LaunchEntryView? = null) {
        var delay = 0

        val entryCount = entryViews.size
        val count = entryCount / 2
        var centerIndex = -1
        if (entryViews.size % 2 != 0) {
            centerIndex = count
        }

        if (centerIndex >= 0) {
            if (entryViews[centerIndex] != except) {
                entryViews[centerIndex].gotoState(state, delay)
            }
            delay += viewModel!!.entryMoveDiffMS
        }

        for (i in count - 1 downTo 0) {
            @Suppress("UnnecessaryVariable")
            val upperIdx = i
            val lowerIdx = entryCount - 1 - i
            if (entryViews[upperIdx] != except) {
                entryViews[upperIdx].gotoState(state, delay)
            }
            if (entryViews[lowerIdx] != except) {
                entryViews[lowerIdx].gotoState(state, delay)
            }

            delay += viewModel!!.entryMoveDiffMS
        }
    }

    private fun applySizeParameters() {
        selectedIcon!!.maxHeight = ViewUtils.getPxFromDip(context, viewModel!!.imageWidthDip).toInt()
        selectedIcon!!.maxWidth = ViewUtils.getPxFromDip(context, viewModel!!.imageWidthDip).toInt()
    }

    private fun showSelectionIndicator() {
        if (focusedEntryView == null) {
            return
        }
        val fromRect = Rect()
        focusedEntryView!!.getHitRect(fromRect)
        val toRect = Rect()
        selectIndicatorContainer!!.getHitRect(toRect)

        val drawable = focusedEntryView!!.entry.getIcon(context)
        selectedIcon!!.setImageDrawable(drawable)

        val useIconColor = focusedEntryView!!.entry.useIconColor()

        selectedItemTextView!!.text = focusedEntryView!!.entry.getName(context)

        val bmpResult = BitmapUtils.drawableToBitmap(drawable)
        if (useIconColor
                && bmpResult != null) {
            selectIndicator!!.setBackgroundColor(
                    ColorUtils.getBackgroundColorFromImage(
                            bmpResult.bitmap,
                            viewModel!!.frameDefaultColor))
        } else {
            selectIndicator!!.setBackgroundColor(
                    viewModel!!.frameDefaultColor)
        }

        if (bmpResult != null && bmpResult.isNew) {
            bmpResult.bitmap.recycle()
        }

        try {
            val anim = ObjectAnimator.ofObject(
                    selectIndicator,
                    "margins",
                    PositionAndSizeEvaluator(selectIndicator!!),
                    fromRect,
                    toRect)
            anim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    transitToState(LaunchLaneViewModel.State.Selected)
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })
            anim.duration = viewModel!!.selectingAnimationDurationMS.toLong()
            anim.start()

            Thread(Runnable {
                try {
                    Thread.sleep((viewModel!!.selectingAnimationDurationMS / 2).toLong())
                    selectedItemTextView!!.post { selectedItemTextView!!.visibility = View.VISIBLE }
                } catch (e: InterruptedException) {
                }
            }).start()
        } catch (e: Exception) {
        }

        selectIndicator!!.visibility = View.VISIBLE
    }

    private fun hideSelectionIndicator() {
        if (selectIndicator != null) {
            selectIndicator!!.visibility = View.INVISIBLE
        }
        if (selectedItemTextView != null) {
            selectedItemTextView!!.visibility = View.GONE
        }
    }

    private fun hideEntries() {
        for (ev in entryViews) {
            ev.visibility = View.INVISIBLE
        }
    }

    private fun showEntries() {
        for (ev in entryViews) {
            ev.visibility = View.VISIBLE
        }
    }

    private fun ensureFocusedEntryAt(y: Int) {
        focusedEntryView = null
        for (ev in entryViews) {
            val hit = isEntryAt(ev, y)
            var desiredState: LaunchEntryViewModel.State = LaunchEntryViewModel.State.Active
            if (hit) {
                desiredState = LaunchEntryViewModel.State.Focused
                focusedEntryView = ev
            }
            ev.gotoState(desiredState)
        }
    }

    private fun isEntryAt(entryView: LaunchEntryView, y: Int): Boolean {
        return entryView.y < y && y < entryView.y + entryView.height
    }
}
