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
import android.graphics.drawable.Drawable
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

    private var mViewModel: LaunchLaneViewModel? = null
    private var mLaneListener: ILaneListener? = null

    //view components
    private var mSelectIndicatorContainer: LinearLayout? = null
    private var mSelectIndicator: LinearLayout? = null
    private var mEntriesContainer: LinearLayout? = null
    private var mSelectedIcon: ImageView? = null
    private var mSelectedItemTextView: VerticalTextView? = null
    private val mEntryViews = ArrayList<LaunchEntryView>()
    private var mFocusedEntryView: LaunchEntryView? = null

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

    fun doInitializeData(viewModel: LaunchLaneViewModel) {
        mViewModel = viewModel

        createViews()
        createEntryViews()
        adaptModelState()
    }

    fun setLaneListener(listener: ILaneListener) {
        mLaneListener = listener
    }

    fun start() {
        gotoState(LaunchLaneViewModel.State.Focusing)
    }

    fun stop() {
        removeAllViews()
    }

    fun gotoState(state: LaunchLaneViewModel.State) {
        transitToState(state)
    }

    fun doHandleTouch(action: Int, x: Int, y: Int) {
        val focusSelectionBorder = width
        if (mViewModel == null) {
            return
        }
        if (mViewModel!!.state === LaunchLaneViewModel.State.Focusing) {
            if (action == MotionEvent.ACTION_UP) {
                sendAllEntriesToState(LaunchEntryViewModel.State.Active)
                mFocusedEntryView = null
            } else {
                ensureFocusedEntryAt(y)
                if (mFocusedEntryView != null) {
                    if (mViewModel!!.isOnRightSide) {
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
        } else if (mViewModel!!.state === LaunchLaneViewModel.State.Selected) {
            if (mViewModel!!.isOnRightSide) {
                if (x > focusSelectionBorder)
                    transitToState(LaunchLaneViewModel.State.Focusing)
            } else {
                if (x < 0)
                    transitToState(LaunchLaneViewModel.State.Focusing)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (mEntryViews != null && mEntryViews.size > 0) {
            setMeasuredDimension(mEntryViews[0].measuredWidth, measuredHeight)
        }
    }

    private fun construct() {
        ViewUtils.disableClipping(this)
    }

    private fun createViews() {
        removeAllViews()
        mEntriesContainer = LinearLayout(context)
        mEntriesContainer!!.orientation = LinearLayout.VERTICAL

        when (mViewModel!!.launcherGravity) {
            LauncherGravity.Top -> mEntriesContainer!!.setGravity(Gravity.TOP)
            LauncherGravity.Center -> mEntriesContainer!!.setGravity(Gravity.CENTER_VERTICAL)
            LauncherGravity.Bottom -> mEntriesContainer!!.setGravity(Gravity.BOTTOM)
        }

        val entriesContainerParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        entriesContainerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        entriesContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        if (mViewModel!!.isOnRightSide) {
            entriesContainerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        } else {
            entriesContainerParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        }

        addView(mEntriesContainer, entriesContainerParams)
        ViewUtils.disableClipping(mEntriesContainer!!)


        mSelectIndicatorContainer = LinearLayout(context)
        mSelectIndicatorContainer!!.orientation = LinearLayout.VERTICAL

        val indicatorContainerParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        indicatorContainerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        indicatorContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        indicatorContainerParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        indicatorContainerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

        addView(mSelectIndicatorContainer, indicatorContainerParams)
        ViewUtils.disableClipping(mSelectIndicatorContainer!!)


        mSelectIndicator = LinearLayout(context)
        mSelectIndicator!!.setBackgroundColor(mViewModel!!.frameDefaultColor)
        mSelectIndicator!!.elevation = ViewUtils.getPxFromDip(context, mViewModel!!.selectedImageElevationDip)
        mSelectIndicator!!.visibility = View.INVISIBLE
        mSelectIndicator!!.setGravity(Gravity.CENTER_HORIZONTAL)
        mSelectIndicator!!.orientation = LinearLayout.VERTICAL

        val selectIndicatorParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)

        mSelectIndicatorContainer!!.addView(mSelectIndicator, selectIndicatorParams)
        ViewUtils.disableClipping(mSelectIndicator!!)

        mSelectedIcon = ImageView(context)
        mSelectedIcon!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
        mSelectedIcon!!.setImageResource(mViewModel!!.unknownAppImageId)

        val selectIconParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        selectIconParams.setMargins(0, ViewUtils.getPxFromDip(context, mViewModel!!.laneIconTopMarginDip).toInt(), 0, 0)

        mSelectIndicator!!.addView(mSelectedIcon, selectIconParams)

        mSelectedItemTextView = VerticalTextView(context)
        mSelectedItemTextView!!.visibility = View.GONE
        mSelectedItemTextView!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, mViewModel!!.itemNameTextSizeSP)
        //this is needed because the parts in the system run with another theme than the application parts
        mSelectedItemTextView!!.setTextColor(resources.getColor(R.color.name_label))

        val selectedItemTextViewParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        selectedItemTextViewParams.setMargins(0, ViewUtils.getPxFromDip(context, mViewModel!!.laneTextTopMarginDip).toInt(), 0, 0)
        mSelectIndicator!!.addView(mSelectedItemTextView, selectedItemTextViewParams)
    }

    private fun createEntryViews() {
        mEntriesContainer!!.removeAllViews()
        mEntryViews.clear()

        for (e in mViewModel!!.entries) {
            val ev = LaunchEntryView(context)
            mEntryViews.add(ev)
            val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            mEntriesContainer!!.addView(ev, params)

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
                sendAllEntriesToState(LaunchEntryViewModel.State.Inactive, mFocusedEntryView)
                fireSelectingEvent()
            }
            LaunchLaneViewModel.State.Selected -> fireSelectedEvent()
        }
        if (mViewModel != null) {
            val oldState = mViewModel!!.state
            val newState = state
            mViewModel!!.state = newState
            fireStateChangedEvent(oldState, newState)
        }
    }

    private fun fireStateChangedEvent(oldState: LaunchLaneViewModel.State, newState: LaunchLaneViewModel.State) {
        if (mLaneListener != null) {
            mLaneListener!!.onStateChanged(oldState, newState)
        }
    }

    private fun fireSelectedEvent() {
        if (mLaneListener != null) {
            mLaneListener!!.onItemSelected(mFocusedEntryView!!.entry)
        }
    }

    private fun fireSelectingEvent() {
        if (mLaneListener != null) {
            mLaneListener!!.onItemSelecting(mFocusedEntryView!!.entry)
        }
    }

    private fun fireNotSelectedEvents() {
        if (mLaneListener != null) {
            mLaneListener!!.onItemSelected(null)
            mLaneListener!!.onItemSelecting(null)
        }
    }

    private fun adaptModelState() {
        transitToState(mViewModel!!.state)
        applySizeParameters()
    }

    private fun initEntryState(state: LaunchEntryViewModel.State) {
        for (ev in mEntryViews) {
            ev.setState(state)
        }
    }

    private fun sendAllEntriesToState(state: LaunchEntryViewModel.State, except: LaunchEntryView? = null) {
        var delay = 0

        val entryCount = mEntryViews.size
        val count = entryCount / 2
        var centerIndex = -1
        if (mEntryViews.size % 2 != 0) {
            centerIndex = count
        }

        if (centerIndex >= 0) {
            if (mEntryViews[centerIndex] != except) {
                mEntryViews[centerIndex].gotoState(state, delay)
            }
            delay += mViewModel!!.entryMoveDiffMS
        }

        for (i in count - 1 downTo 0) {
            val upperIdx = i
            val lowerIdx = entryCount - 1 - i
            if (mEntryViews[upperIdx] != except) {
                mEntryViews[upperIdx].gotoState(state, delay)
            }
            if (mEntryViews[lowerIdx] != except) {
                mEntryViews[lowerIdx].gotoState(state, delay)
            }

            delay += mViewModel!!.entryMoveDiffMS
        }
    }

    private fun applySizeParameters() {
        mSelectedIcon!!.maxHeight = ViewUtils.getPxFromDip(context, mViewModel!!.imageWidthDip).toInt()
        mSelectedIcon!!.maxWidth = ViewUtils.getPxFromDip(context, mViewModel!!.imageWidthDip).toInt()
    }

    private fun showSelectionIndicator() {
        if (mFocusedEntryView == null) {
            return
        }
        val fromRect = Rect()
        mFocusedEntryView!!.getHitRect(fromRect)
        val toRect = Rect()
        mSelectIndicatorContainer!!.getHitRect(toRect)

        val drawable = mFocusedEntryView!!.entry.getIcon(context)
        mSelectedIcon!!.setImageDrawable(drawable)

        val useIconColor = mFocusedEntryView!!.entry.useIconColor()

        mSelectedItemTextView!!.text = mFocusedEntryView!!.entry.getName(context)

        val bmpResult = BitmapUtils.drawableToBitmap(drawable)
        if (useIconColor
                && bmpResult != null
                && bmpResult.bitmap != null) {
            mSelectIndicator!!.setBackgroundColor(
                    ColorUtils.getBackgroundColorFromImage(
                            bmpResult.bitmap,
                            mViewModel!!.frameDefaultColor))
        } else {
            mSelectIndicator!!.setBackgroundColor(
                    mViewModel!!.frameDefaultColor)
        }

        if (bmpResult != null && bmpResult.isNew) {
            bmpResult.bitmap.recycle()
        }

        try {
            val anim = ObjectAnimator.ofObject(
                    mSelectIndicator,
                    "margins",
                    PositionAndSizeEvaluator(mSelectIndicator!!),
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
            anim.duration = mViewModel!!.selectingAnimationDurationMS.toLong()
            anim.start()

            Thread(Runnable {
                try {
                    Thread.sleep((mViewModel!!.selectingAnimationDurationMS / 2).toLong())
                    mSelectedItemTextView!!.post { mSelectedItemTextView!!.visibility = View.VISIBLE }
                } catch (e: InterruptedException) {
                }
            }).start()
        } catch (e: Exception) {
        }

        mSelectIndicator!!.visibility = View.VISIBLE
    }

    private fun hideSelectionIndicator() {
        if (mSelectIndicator != null) {
            mSelectIndicator!!.visibility = View.INVISIBLE
        }
        if (mSelectedItemTextView != null) {
            mSelectedItemTextView!!.visibility = View.GONE
        }
    }

    private fun hideEntries() {
        for (ev in mEntryViews) {
            ev.visibility = View.INVISIBLE
        }
    }

    private fun showEntries() {
        for (ev in mEntryViews) {
            ev.visibility = View.VISIBLE
        }
    }

    private fun ensureFocusedEntryAt(y: Int) {
        mFocusedEntryView = null
        for (ev in mEntryViews) {
            val hit = isEntryAt(ev, y)
            var desiredState: LaunchEntryViewModel.State = LaunchEntryViewModel.State.Active
            if (hit) {
                desiredState = LaunchEntryViewModel.State.Focused
                mFocusedEntryView = ev
            }
            ev.gotoState(desiredState)
        }
    }

    private fun isEntryAt(entryView: LaunchEntryView, y: Int): Boolean {
        return entryView.y < y && y < entryView.y + entryView.height
    }
}
