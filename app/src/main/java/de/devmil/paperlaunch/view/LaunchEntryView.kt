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

import android.content.Context
import android.os.AsyncTask
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import de.devmil.paperlaunch.model.IEntry
import de.devmil.paperlaunch.view.utils.ViewUtils

internal class LoadParams(var target: ImageView, var entry: LaunchEntryViewModel, var context: Context)

internal class LoadIconTask : AsyncTask<LoadParams, Void, Void>() {

    override fun doInBackground(vararg params: LoadParams): Void? {
        for (p in params) {
            val icon = p.entry.appIcon
            p.target.post { p.target.setImageDrawable(icon) }
        }
        return null
    }
}

class LaunchEntryView : LinearLayout {
    private var viewModel: LaunchEntryViewModel? = null

    private var imgFrame: LinearLayout? = null
    private var appIcon: ImageView? = null
    private var loadTask: LoadIconTask? = null

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

    fun doInitialize(viewModel: LaunchEntryViewModel) {
        this.viewModel = viewModel

        adaptModelState()
    }

    val entry: IEntry
        get() = viewModel!!.entry

    fun setState(state: LaunchEntryViewModel.State) {
        setImageParameters(state, false, 0)
    }

    @JvmOverloads fun gotoState(state: LaunchEntryViewModel.State, delay: Int = 0) {
        if (viewModel!!.state === state)
            return
        if (viewModel!!.state.isAnimationStateFor(state))
            return
        setImageParameters(state, true, delay)
    }

    private fun construct() {
        ViewUtils.disableClipping(this)
    }

    private fun adaptModelState() {
        applyParameters()

        setImageParameters(viewModel!!.state, false, 0)
    }

    private fun applyParameters() {
        removeAllViews()
        imgFrame = LinearLayout(context)
        imgFrame!!.setBackgroundColor(viewModel!!.frameDefaultColor)
        val imgFrameParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val marginsFramePx = ViewUtils.getPxFromDip(context, viewModel!!.entriesMarginDip).toInt()
        imgFrameParams.setMargins(marginsFramePx, marginsFramePx, marginsFramePx, marginsFramePx)

        addView(imgFrame, imgFrameParams)
        ViewUtils.disableClipping(imgFrame!!)

        imgFrame!!.removeAllViews()
        appIcon = ImageView(context)
        appIcon!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
        val imgWidth = ViewUtils.getPxFromDip(context, viewModel!!.imageWidthDip).toInt()
        val imgHeight = ViewUtils.getPxFromDip(context, viewModel!!.imageWidthDip).toInt()

        val imgParams = LinearLayout.LayoutParams(imgWidth, imgHeight)
        val marginsImgPx = ViewUtils.getPxFromDip(context, viewModel!!.imageMarginDip).toInt()
        imgParams.setMargins(marginsImgPx, marginsImgPx, marginsImgPx, marginsImgPx)

        imgFrame!!.addView(appIcon, imgParams)
        ViewUtils.disableClipping(appIcon!!)

        if (loadTask != null) {
            loadTask!!.cancel(true)
        }
        loadTask = LoadIconTask()
        loadTask!!.execute(LoadParams(appIcon!!, viewModel!!, context))

        imgFrame!!.elevation = ViewUtils.getPxFromDip(context, viewModel!!.imageElevationDip)
    }

    private fun getTranslateXToApply(state: LaunchEntryViewModel.State): Float {
        val imgWidthPx = Math.max(imgFrame!!.width.toFloat(), ViewUtils.getPxFromDip(context, viewModel!!.imageWidthDip))
        val offset = ViewUtils.getPxFromDip(context, viewModel!!.imageOffsetDip)
        when (state) {
            LaunchEntryViewModel.State.Inactive -> {
                if (viewModel!!.isOnRightSide) {
                    return imgWidthPx + 2 * offset
                } else {
                    return -(imgWidthPx + 2 * offset)
                }
            }
            LaunchEntryViewModel.State.Active, LaunchEntryViewModel.State.Activating -> {
                if (viewModel!!.isOnRightSide) {
                    return imgWidthPx / 2.0f + offset
                } else {
                    return -(imgWidthPx / 2.0f + offset)
                }
            }
            LaunchEntryViewModel.State.Focusing, LaunchEntryViewModel.State.Focused, LaunchEntryViewModel.State.Selected -> {
                return 0f
            }
        }
    }

    private fun getAlphaToApply(state: LaunchEntryViewModel.State): Float {
        when (state) {
            LaunchEntryViewModel.State.Inactive,
            LaunchEntryViewModel.State.Active,
            LaunchEntryViewModel.State.Activating,
            LaunchEntryViewModel.State.Focusing,
            LaunchEntryViewModel.State.Focused -> {
                return 1.0f
            }
            LaunchEntryViewModel.State.Selected -> {
                return 0.0f
            }
        }
    }

    private fun setImageParameters(state: LaunchEntryViewModel.State, transit: Boolean, delay: Int) {
        setImageParameters(getTranslateXToApply(state), getAlphaToApply(state), transit, state, delay)
    }

    private fun setImageParameters(translateX: Float, alpha: Float, animate: Boolean, targetState: LaunchEntryViewModel.State, delay: Int) {
        if (!animate) {
            imgFrame!!.translationX = translateX
            imgFrame!!.alpha = alpha
            viewModel!!.state = targetState
        } else {
            synchronized(this) {
                if (targetState.hasAnimationState()) {
                    viewModel!!.state = targetState.animationState
                }
                imgFrame!!.animate()
                        .translationX(translateX)
                        .setDuration(viewModel!!.moveDuration.toLong())
                        .setStartDelay(delay.toLong())
                        .withEndAction(object : Runnable {
                            override fun run() {
                                synchronized(this) {
                                    if (alpha != imgFrame!!.alpha) {
                                        imgFrame!!.animate()
                                                .alpha(alpha)
                                                .setDuration(viewModel!!.alphaDuration.toLong())
                                                .withEndAction {
                                                    synchronized(this@LaunchEntryView) {
                                                        viewModel!!.state = targetState
                                                    }
                                                }
                                                .start()
                                    } else {
                                        viewModel!!.state = targetState
                                    }

                                }
                            }
                        })
                        .start()
            }
        }
    }
}
