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

    @Deprecated("Deprecated in Java")
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
        if (viewModel?.state === state)
            return
        if (viewModel?.state?.isAnimationStateFor(state) != false)
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

        val localViewModel = viewModel!!

        val localImgFrame = LinearLayout(context)
        localImgFrame.setBackgroundColor(localViewModel.frameDefaultColor)
        val imgFrameParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val marginsFramePx = ViewUtils.getPxFromDip(context, localViewModel.entriesMarginDip).toInt()
        imgFrameParams.setMargins(marginsFramePx, marginsFramePx, marginsFramePx, marginsFramePx)

        addView(localImgFrame, imgFrameParams)
        ViewUtils.disableClipping(localImgFrame)

        localImgFrame.removeAllViews()
        val localAppIcon = ImageView(context)
        localAppIcon.scaleType = ImageView.ScaleType.CENTER_INSIDE
        val imgWidth = ViewUtils.getPxFromDip(context, localViewModel.imageWidthDip).toInt()
        val imgHeight = ViewUtils.getPxFromDip(context, localViewModel.imageWidthDip).toInt()

        val imgParams = LinearLayout.LayoutParams(imgWidth, imgHeight)
        val marginsImgPx = ViewUtils.getPxFromDip(context, localViewModel.imageMarginDip).toInt()
        imgParams.setMargins(marginsImgPx, marginsImgPx, marginsImgPx, marginsImgPx)

        localImgFrame.addView(localAppIcon, imgParams)
        ViewUtils.disableClipping(localAppIcon)

        loadTask?.cancel(true)
        val localLoadTask = LoadIconTask()
        localLoadTask.execute(LoadParams(localAppIcon, localViewModel, context))

        localImgFrame.elevation = ViewUtils.getPxFromDip(context, localViewModel.imageElevationDip)

        imgFrame = localImgFrame
        appIcon = localAppIcon
        loadTask = localLoadTask
    }

    private fun getTranslateXToApply(state: LaunchEntryViewModel.State): Float {
        val localImgFrame = imgFrame!!
        val localViewModel = viewModel!!

        val imgWidthPx = Math.max(localImgFrame.width.toFloat(), ViewUtils.getPxFromDip(context, localViewModel.imageWidthDip))
        val offset = ViewUtils.getPxFromDip(context, localViewModel.imageOffsetDip)
        when (state) {
            LaunchEntryViewModel.State.Inactive -> {
                return if (localViewModel.isOnRightSide) {
                    imgWidthPx + 2 * offset
                } else {
                    -(imgWidthPx + 2 * offset)
                }
            }
            LaunchEntryViewModel.State.Active, LaunchEntryViewModel.State.Activating -> {
                return if (localViewModel.isOnRightSide) {
                    imgWidthPx / 2.0f + offset
                } else {
                    -(imgWidthPx / 2.0f + offset)
                }
            }
            LaunchEntryViewModel.State.Focusing, LaunchEntryViewModel.State.Focused, LaunchEntryViewModel.State.Selected -> {
                return 0f
            }
        }
    }

    private fun getAlphaToApply(state: LaunchEntryViewModel.State): Float {
        return when (state) {
            LaunchEntryViewModel.State.Inactive,
            LaunchEntryViewModel.State.Active,
            LaunchEntryViewModel.State.Activating,
            LaunchEntryViewModel.State.Focusing,
            LaunchEntryViewModel.State.Focused -> {
                1.0f
            }
            LaunchEntryViewModel.State.Selected -> {
                0.0f
            }
        }
    }

    private fun setImageParameters(state: LaunchEntryViewModel.State, transit: Boolean, delay: Int) {
        setImageParameters(getTranslateXToApply(state), getAlphaToApply(state), transit, state, delay)
    }

    private fun setImageParameters(translateX: Float, alpha: Float, animate: Boolean, targetState: LaunchEntryViewModel.State, delay: Int) {
        val localImgFrame = imgFrame!!
        val localViewModel = viewModel!!

        if (!animate) {
            localImgFrame.translationX = translateX
            localImgFrame.alpha = alpha
            localViewModel.state = targetState
        } else {
            synchronized(this) {
                if (targetState.hasAnimationState()) {
                    localViewModel.state = targetState.animationState
                }
                localImgFrame.animate()
                        .translationX(translateX)
                        .setDuration(localViewModel.moveDuration.toLong())
                        .setStartDelay(delay.toLong())
                        .withEndAction(object : Runnable {
                            override fun run() {
                                synchronized(this) {
                                    if (alpha != localImgFrame.alpha) {
                                        localImgFrame.animate()
                                                .alpha(alpha)
                                                .setDuration(localViewModel.alphaDuration.toLong())
                                                .withEndAction {
                                                    synchronized(this@LaunchEntryView) {
                                                        localViewModel.state = targetState
                                                    }
                                                }
                                                .start()
                                    } else {
                                        localViewModel.state = targetState
                                    }

                                }
                            }
                        })
                        .start()
            }
        }
    }
}
