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
package de.devmil.paperlaunch.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import de.devmil.paperlaunch.model.IEntry;
import de.devmil.paperlaunch.utils.ViewUtils;

public class LaunchEntryView extends LinearLayout {
    private LaunchEntryViewModel mViewModel;

    private LinearLayout mImgFrame;
    private ImageView mAppIcon;

    public LaunchEntryView(Context context) {
        super(context);
        construct();
    }

    public LaunchEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }

    public LaunchEntryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        construct();
    }

    public LaunchEntryView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        construct();
    }

    public void doInitialize(LaunchEntryViewModel viewModel)
    {
        mViewModel = viewModel;

        adaptModelState();
    }

    public IEntry getEntry() {
        return mViewModel.getEntry();
    }

    public void setState(LaunchEntryViewModel.State state)
    {
        setImageParameters(state, false, 0);
    }

    public void gotoState(LaunchEntryViewModel.State state)
    {
        gotoState(state, 0);
    }

    public void gotoState(LaunchEntryViewModel.State state, int delay)
    {
        if(mViewModel.getState() == state)
            return;
        if(mViewModel.getState().isAnimationStateFor(state))
            return;
        setImageParameters(state, true, delay);
    }

    private void construct()
    {
        ViewUtils.disableClipping(this);
    }

    private void adaptModelState()
    {
        applyParameters();

        setImageParameters(mViewModel.getState(), false, 0);
    }

    private void applyParameters()
    {
        removeAllViews();
        mImgFrame = new LinearLayout(getContext());
        mImgFrame.setBackgroundColor(mViewModel.getFrameDefaultColor());
        LayoutParams imgFrameParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        int marginsFramePx = (int)ViewUtils.getPxFromDip(getContext(), mViewModel.getEntriesMarginDip());
        imgFrameParams.setMargins(marginsFramePx, marginsFramePx, marginsFramePx, marginsFramePx);

        addView(mImgFrame, imgFrameParams);
        ViewUtils.disableClipping(mImgFrame);

        mImgFrame.removeAllViews();
        mAppIcon = new ImageView(getContext());
        mAppIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int imgWidth = (int)ViewUtils.getPxFromDip(getContext(), mViewModel.getImageWidthDip());
        int imgHeight = (int) ViewUtils.getPxFromDip(getContext(), mViewModel.getImageWidthDip());

        LayoutParams imgParams = new LayoutParams(imgWidth, imgHeight);
        int marginsImgPx = (int)ViewUtils.getPxFromDip(getContext(), mViewModel.getImageMarginDip());
        imgParams.setMargins(marginsImgPx, marginsImgPx, marginsImgPx, marginsImgPx);

        mImgFrame.addView(mAppIcon, imgParams);
        ViewUtils.disableClipping(mAppIcon);

        mAppIcon.setImageDrawable(mViewModel.getAppIcon());

        mImgFrame.setElevation(ViewUtils.getPxFromDip(getContext(), mViewModel.getImageElevationDip()));
    }

    private float getTranslateXToApply(LaunchEntryViewModel.State state)
    {
        float imgWidthPx = Math.max(mImgFrame.getWidth(), ViewUtils.getPxFromDip(getContext(), mViewModel.getImageWidthDip()));
        float offset = ViewUtils.getPxFromDip(getContext(), mViewModel.getImageOffsetDip());
        switch(state)
        {
            case Inactive:
                return imgWidthPx + 2*offset;
            case Active:
            case Activating:
                return imgWidthPx/2.0f + offset;
            case Focusing:
            case Focused:
            case Selected:
                return 0;
        }
        return 0.0f;
    }

    private float getAlphaToApply(LaunchEntryViewModel.State state)
    {
        switch(state)
        {
            case Inactive:
            case Active:
            case Activating:
            case Focusing:
            case Focused:
                return 1.0f;
            case Selected:
                return 0.0f;
        }
        return 1.0f;
    }

    private void setImageParameters(LaunchEntryViewModel.State state, boolean transit, int delay)
    {
        setImageParameters(getTranslateXToApply(state), getAlphaToApply(state), transit, state, delay);
    }

    private void setImageParameters(float translateX, final float alpha, boolean animate, final LaunchEntryViewModel.State targetState, int delay)
    {
        if(!animate) {
            mImgFrame.setTranslationX(translateX);
            mImgFrame.setAlpha(alpha);
            mViewModel.setState(targetState);
        }
        else {
            synchronized (this) {
                if(targetState.hasAnimationState()) {
                    mViewModel.setState(targetState.getAnimationState());
                }
                mImgFrame.animate()
                        .translationX(translateX)
                        .setDuration(mViewModel.getMoveDuration())
                        .setStartDelay(delay)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (this) {
                                    if (alpha != mImgFrame.getAlpha()) {
                                        mImgFrame.animate()
                                                .alpha(alpha)
                                                .setDuration(mViewModel.getAlphaDuration())
                                                .withEndAction(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        synchronized (LaunchEntryView.this) {
                                                            if (targetState != null)
                                                                mViewModel.setState(targetState);
                                                        }
                                                    }
                                                })
                                                .start();
                                    } else {
                                        mViewModel.setState(targetState);
                                    }

                                }
                            }
                        })
                        .start();
            }
        }
    }
}
