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
        val localEntriesContainer = LinearLayout(context)
        localEntriesContainer.orientation = LinearLayout.VERTICAL

        val localViewModel = viewModel!!

        when (localViewModel.launcherGravity) {
            LauncherGravity.Top -> localEntriesContainer.gravity = Gravity.TOP
            LauncherGravity.Center -> localEntriesContainer.gravity = Gravity.CENTER_VERTICAL
            LauncherGravity.Bottom -> localEntriesContainer.gravity = Gravity.BOTTOM
        }

        val entriesContainerParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)
        entriesContainerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        entriesContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        if (localViewModel.isOnRightSide) {
            entriesContainerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        } else {
            entriesContainerParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        }

        addView(localEntriesContainer, entriesContainerParams)
        ViewUtils.disableClipping(localEntriesContainer)


        val localSelectIndicatorContainer = LinearLayout(context)
        localSelectIndicatorContainer.orientation = LinearLayout.VERTICAL

        val indicatorContainerParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)
        indicatorContainerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        indicatorContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        indicatorContainerParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        indicatorContainerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

        addView(localSelectIndicatorContainer, indicatorContainerParams)
        ViewUtils.disableClipping(localSelectIndicatorContainer)


        val localSelectIndicator = LinearLayout(context)
        localSelectIndicator.setBackgroundColor(localViewModel.frameDefaultColor)
        localSelectIndicator.elevation = ViewUtils.getPxFromDip(context, localViewModel.selectedImageElevationDip)
        localSelectIndicator.visibility = View.INVISIBLE
        localSelectIndicator.gravity = Gravity.CENTER_HORIZONTAL
        localSelectIndicator.orientation = LinearLayout.VERTICAL

        val selectIndicatorParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)

        localSelectIndicatorContainer.addView(localSelectIndicator, selectIndicatorParams)
        ViewUtils.disableClipping(localSelectIndicator)

        val localSelectedIcon = ImageView(context)
        localSelectedIcon.scaleType = ImageView.ScaleType.CENTER_INSIDE
        localSelectedIcon.setImageResource(localViewModel.unknownAppImageId)

        val selectIconParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        selectIconParams.setMargins(0, ViewUtils.getPxFromDip(context, localViewModel.laneIconTopMarginDip).toInt(), 0, 0)

        localSelectIndicator.addView(localSelectedIcon, selectIconParams)

        val localSelectedItemTextView = VerticalTextView(context)
        localSelectedItemTextView.visibility = View.GONE
        localSelectedItemTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, localViewModel.itemNameTextSizeSP)
        //this is needed because the parts in the system run with another theme than the application parts
        localSelectedItemTextView.setTextColor(ContextCompat.getColor(context, R.color.name_label))

        val selectedItemTextViewParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        selectedItemTextViewParams.setMargins(0, ViewUtils.getPxFromDip(context, localViewModel.laneTextTopMarginDip).toInt(), 0, 0)
        localSelectIndicator.addView(localSelectedItemTextView, selectedItemTextViewParams)

        entriesContainer = localEntriesContainer
        selectIndicatorContainer = localSelectIndicatorContainer
        selectIndicator = localSelectIndicator
        selectedIcon = localSelectedIcon
        selectedItemTextView = localSelectedItemTextView
    }

    private fun createEntryViews() {
        val localEntriesContainer = entriesContainer!!
        val localViewModel = viewModel!!
        localEntriesContainer.removeAllViews()
        entryViews.clear()

        val entries = localViewModel.entries
        for (e in entries) {
            val ev = LaunchEntryView(context)
            entryViews.add(ev)
            val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            localEntriesContainer.addView(ev, params)

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
        laneListener?.onStateChanged(oldState, newState)
    }

    private fun fireSelectedEvent() {
        laneListener?.let { listener ->
            focusedEntryView?.let { entryView ->
                listener.onItemSelected(entryView.entry)
            }
        }
    }

    private fun fireSelectingEvent() {
        laneListener?.let { listener ->
            focusedEntryView?.let { entryView ->
                listener.onItemSelecting(entryView.entry)
            }
        }
    }

    private fun fireNotSelectedEvents() {
        laneListener?.let { listener ->
            listener.onItemSelected(null)
            listener.onItemSelecting(null)
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
        val localViewModel = viewModel!!
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
            delay += localViewModel.entryMoveDiffMS
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

            delay += localViewModel.entryMoveDiffMS
        }
    }

    private fun applySizeParameters() {
        val localViewModel = viewModel!!
        val localSelectedIcon = selectedIcon!!
        localSelectedIcon.maxHeight = ViewUtils.getPxFromDip(context, localViewModel.imageWidthDip).toInt()
        localSelectedIcon.maxWidth = ViewUtils.getPxFromDip(context, localViewModel.imageWidthDip).toInt()
    }

    private fun showSelectionIndicator() {
        if (focusedEntryView == null) {
            return
        }
        val localFocusedEntryView = focusedEntryView!!
        val localSelectIndicatorContainer = selectIndicatorContainer!!
        val localSelectedIcon = selectedIcon!!
        val localSelectedItemTextView = selectedItemTextView!!
        val localSelectIndicator = selectIndicator!!
        val localViewModel = viewModel!!

        val fromRect = Rect()
        localFocusedEntryView.getHitRect(fromRect)
        val toRect = Rect()
        localSelectIndicatorContainer.getHitRect(toRect)

        val drawable = localFocusedEntryView.entry.getIcon(context)
        localSelectedIcon.setImageDrawable(drawable)

        val useIconColor = localFocusedEntryView.entry.useIconColor()

        localSelectedItemTextView.text = localFocusedEntryView.entry.getName(context)

        val bmpResult = BitmapUtils.drawableToBitmap(drawable)
        if (useIconColor
                && bmpResult != null) {
            localSelectIndicator.setBackgroundColor(
                    ColorUtils.getBackgroundColorFromImage(
                            bmpResult.bitmap,
                            localViewModel.frameDefaultColor))
        } else {
            localSelectIndicator.setBackgroundColor(
                    localViewModel.frameDefaultColor)
        }

        if (bmpResult != null && bmpResult.isNew) {
            bmpResult.bitmap.recycle()
        }

        try {
            val anim = ObjectAnimator.ofObject(
                    localSelectIndicator,
                    "margins",
                    PositionAndSizeEvaluator(localSelectIndicator),
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
            anim.duration = localViewModel.selectingAnimationDurationMS.toLong()
            anim.start()

            Thread(Runnable {
                try {
                    Thread.sleep((localViewModel.selectingAnimationDurationMS / 2).toLong())
                    localSelectedItemTextView.post { localSelectedItemTextView.visibility = View.VISIBLE }
                } catch (e: InterruptedException) {
                }
            }).start()
        } catch (e: Exception) {
        }

        localSelectIndicator.visibility = View.VISIBLE
    }

    private fun hideSelectionIndicator() {
        selectIndicator?.visibility = View.INVISIBLE
        selectedItemTextView?.visibility = View.GONE
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
