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
    private var mViewModel: LaunchEntryViewModel? = null

    private var mImgFrame: LinearLayout? = null
    private var mAppIcon: ImageView? = null
    private var mLoadTask: LoadIconTask? = null

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
        mViewModel = viewModel

        adaptModelState()
    }

    val entry: IEntry
        get() = mViewModel!!.entry

    fun setState(state: LaunchEntryViewModel.State) {
        setImageParameters(state, false, 0)
    }

    @JvmOverloads fun gotoState(state: LaunchEntryViewModel.State, delay: Int = 0) {
        if (mViewModel!!.state === state)
            return
        if (mViewModel!!.state.isAnimationStateFor(state))
            return
        setImageParameters(state, true, delay)
    }

    private fun construct() {
        ViewUtils.disableClipping(this)
    }

    private fun adaptModelState() {
        applyParameters()

        setImageParameters(mViewModel!!.state, false, 0)
    }

    private fun applyParameters() {
        removeAllViews()
        mImgFrame = LinearLayout(context)
        mImgFrame!!.setBackgroundColor(mViewModel!!.frameDefaultColor)
        val imgFrameParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val marginsFramePx = ViewUtils.getPxFromDip(context, mViewModel!!.entriesMarginDip).toInt()
        imgFrameParams.setMargins(marginsFramePx, marginsFramePx, marginsFramePx, marginsFramePx)

        addView(mImgFrame, imgFrameParams)
        ViewUtils.disableClipping(mImgFrame!!)

        mImgFrame!!.removeAllViews()
        mAppIcon = ImageView(context)
        mAppIcon!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
        val imgWidth = ViewUtils.getPxFromDip(context, mViewModel!!.imageWidthDip).toInt()
        val imgHeight = ViewUtils.getPxFromDip(context, mViewModel!!.imageWidthDip).toInt()

        val imgParams = LinearLayout.LayoutParams(imgWidth, imgHeight)
        val marginsImgPx = ViewUtils.getPxFromDip(context, mViewModel!!.imageMarginDip).toInt()
        imgParams.setMargins(marginsImgPx, marginsImgPx, marginsImgPx, marginsImgPx)

        mImgFrame!!.addView(mAppIcon, imgParams)
        ViewUtils.disableClipping(mAppIcon!!)

        if (mLoadTask != null) {
            mLoadTask!!.cancel(true)
        }
        mLoadTask = LoadIconTask()
        mLoadTask!!.execute(LoadParams(mAppIcon!!, mViewModel!!, context))

        mImgFrame!!.elevation = ViewUtils.getPxFromDip(context, mViewModel!!.imageElevationDip)
    }

    private fun getTranslateXToApply(state: LaunchEntryViewModel.State): Float {
        val imgWidthPx = Math.max(mImgFrame!!.width.toFloat(), ViewUtils.getPxFromDip(context, mViewModel!!.imageWidthDip))
        val offset = ViewUtils.getPxFromDip(context, mViewModel!!.imageOffsetDip)
        when (state) {
            LaunchEntryViewModel.State.Inactive -> {
                if (mViewModel!!.isOnRightSide) {
                    return imgWidthPx + 2 * offset
                } else {
                    return -(imgWidthPx + 2 * offset)
                }
            }
            LaunchEntryViewModel.State.Active, LaunchEntryViewModel.State.Activating -> {
                if (mViewModel!!.isOnRightSide) {
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

    private fun setImageParameters(translateX: Float, alpha: Float, animate: Boolean, targetState: LaunchEntryViewModel.State?, delay: Int) {
        if (!animate) {
            mImgFrame!!.translationX = translateX
            mImgFrame!!.alpha = alpha
            mViewModel!!.state = targetState!!
        } else {
            synchronized(this) {
                if (targetState!!.hasAnimationState()) {
                    mViewModel!!.state = targetState.animationState
                }
                mImgFrame!!.animate()
                        .translationX(translateX)
                        .setDuration(mViewModel!!.moveDuration.toLong())
                        .setStartDelay(delay.toLong())
                        .withEndAction(object : Runnable {
                            override fun run() {
                                synchronized(this) {
                                    if (alpha != mImgFrame!!.alpha) {
                                        mImgFrame!!.animate()
                                                .alpha(alpha)
                                                .setDuration(mViewModel!!.alphaDuration.toLong())
                                                .withEndAction {
                                                    synchronized(this@LaunchEntryView) {
                                                        if (targetState != null)
                                                            mViewModel!!.state = targetState
                                                    }
                                                }
                                                .start()
                                    } else {
                                        mViewModel!!.state = targetState
                                    }

                                }
                            }
                        })
                        .start()
            }
        }
    }
}
