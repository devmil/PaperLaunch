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

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import de.devmil.paperlaunch.model.IEntry;
import de.devmil.paperlaunch.utils.BitmapUtils;
import de.devmil.paperlaunch.utils.PositionAndSizeEvaluator;
import de.devmil.paperlaunch.utils.ViewUtils;
import de.devmil.paperlaunch.view.widgets.VerticalTextView;

public class LaunchLaneView extends RelativeLayout {
    interface ILaneListener {
        void onItemSelected(IEntry selectedItem);
        void onStateChanged(LaunchLaneViewModel.State oldState, LaunchLaneViewModel.State newState);
    }

    private LaunchLaneViewModel mViewModel;
    private ILaneListener mLaneListener;

    //view components
    private LinearLayout mSelectIndicatorContainer;
    private LinearLayout mSelectIndicator;
    private LinearLayout mEntriesContainer;
    private ImageView mSelectedIcon;
    private VerticalTextView mSelectedItemTextView;
    private List<LaunchEntryView> mEntryViews = new ArrayList<>();
    private LaunchEntryView mFocusedEntryView;

    public LaunchLaneView(Context context) {
        super(context);
        construct();
    }

    public LaunchLaneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }

    public LaunchLaneView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        construct();
    }

    public LaunchLaneView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        construct();
    }

    public void doInitializeData(LaunchLaneViewModel viewModel)
    {
        mViewModel = viewModel;

        createViews();
        createEntryViews();
        adaptModelState();
    }

    public void setLaneListener(ILaneListener listener) {
        mLaneListener = listener;
    }

    public void start()
    {
        gotoState(LaunchLaneViewModel.State.Focusing);
    }

    public void stop() {
        removeAllViews();
    }

    public void gotoState(LaunchLaneViewModel.State state)
    {
        transitToState(state);
    }

    public void doHandleTouch(int action, int x, int y)
    {
        int focusSelectionBorder = getWidth();
        if(mViewModel == null) {
            return;
        }
        if(mViewModel.getState() == LaunchLaneViewModel.State.Focusing)
        {
            if(action == MotionEvent.ACTION_UP) {
                sendAllEntriesToState(LaunchEntryViewModel.State.Active, true);
                mFocusedEntryView = null;
            }
            else {
                ensureFocusedEntryAt(y);
                if(mFocusedEntryView != null) {
                    if (mViewModel.isOnRightSide()) {
                        if (x < focusSelectionBorder) {
                            transitToState(LaunchLaneViewModel.State.Selecting);
                        }
                    } else {
                        if (x > focusSelectionBorder)
                            transitToState(LaunchLaneViewModel.State.Selecting);
                    }
                }
            }
        }
        else if(mViewModel.getState() == LaunchLaneViewModel.State.Selected)
        {
            if(mViewModel.isOnRightSide()) {
                if (x > focusSelectionBorder)
                    transitToState(LaunchLaneViewModel.State.Focusing);
            } else {
                if (x < focusSelectionBorder)
                    transitToState(LaunchLaneViewModel.State.Focusing);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(mEntryViews != null
                && mEntryViews.size() > 0)
        {
            setMeasuredDimension(mEntryViews.get(0).getMeasuredWidth(), getMeasuredHeight());
        }
    }

    private void construct()
    {
        ViewUtils.disableClipping(this);
    }

    private void createViews()
    {
        removeAllViews();
        mEntriesContainer = new LinearLayout(getContext());
        mEntriesContainer.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams entriesContainerParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        entriesContainerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        entriesContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        if(mViewModel.isOnRightSide())
        {
            entriesContainerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        else
        {
            entriesContainerParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }

        addView(mEntriesContainer, entriesContainerParams);
        ViewUtils.disableClipping(mEntriesContainer);


        mSelectIndicatorContainer = new LinearLayout(getContext());
        mSelectIndicatorContainer.setOrientation(LinearLayout.VERTICAL);

        RelativeLayout.LayoutParams indicatorContainerParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        indicatorContainerParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        indicatorContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        indicatorContainerParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        indicatorContainerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        addView(mSelectIndicatorContainer, indicatorContainerParams);
        ViewUtils.disableClipping(mSelectIndicatorContainer);


        mSelectIndicator = new LinearLayout(getContext());
        mSelectIndicator.setBackgroundColor(mViewModel.getFrameDefaultColor());
        mSelectIndicator.setElevation(ViewUtils.getPxFromDip(getContext(), mViewModel.getSelectedImageElevationDip()));
        mSelectIndicator.setVisibility(View.INVISIBLE);
        mSelectIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
        mSelectIndicator.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams selectIndicatorParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        mSelectIndicatorContainer.addView(mSelectIndicator, selectIndicatorParams);
        ViewUtils.disableClipping(mSelectIndicator);

        mSelectedIcon = new ImageView(getContext());
        mSelectedIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mSelectedIcon.setImageResource(mViewModel.getUnknownAppImageId());

        LinearLayout.LayoutParams selectIconParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        selectIconParams.setMargins(0, (int)ViewUtils.getPxFromDip(getContext(), 5), 0, 0);

        mSelectIndicator.addView(mSelectedIcon, selectIconParams);

        mSelectedItemTextView = new VerticalTextView(getContext());
        mSelectedItemTextView.setVisibility(View.GONE);
        mSelectedItemTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mViewModel.getItemNameTextSizeSP());

        LinearLayout.LayoutParams selectedItemTextViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        selectedItemTextViewParams.setMargins(0, (int)ViewUtils.getPxFromDip(getContext(), 20), 0, 0);
        mSelectIndicator.addView(mSelectedItemTextView, selectedItemTextViewParams);
    }

    private void createEntryViews() {
        mEntriesContainer.removeAllViews();
        mEntryViews.clear();

        for(LaunchEntryViewModel e : mViewModel.getEntries())
        {
            LaunchEntryView ev = new LaunchEntryView(getContext());
            mEntryViews.add(ev);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mEntriesContainer.addView(ev, params);

            ev.doInitialize(e);
        }
    }

    private void transitToState(LaunchLaneViewModel.State state)
    {
        switch(state)
        {
            case Init:
                hideSelectionIndicator();
                initEntryState(LaunchEntryViewModel.State.Inactive);
                break;
            case Focusing:
                hideSelectionIndicator();
                sendAllEntriesToState(LaunchEntryViewModel.State.Active, true);
                break;
            case Selecting:
                showSelectionIndicator();
                sendAllEntriesToState(LaunchEntryViewModel.State.Inactive, true, mFocusedEntryView);
                break;
            case Selected:
                fireSelectedEvent();
                break;
        }
        if(mViewModel != null) {
            LaunchLaneViewModel.State oldState = mViewModel.getState();
            LaunchLaneViewModel.State newState = state;
            mViewModel.setState(newState);
            fireStateChangedEvent(oldState, newState);
        }
    }

    private void fireStateChangedEvent(LaunchLaneViewModel.State oldState, LaunchLaneViewModel.State newState) {
        if(mLaneListener != null) {
            mLaneListener.onStateChanged(oldState, newState);
        }
    }

    private void fireSelectedEvent() {
        if(mLaneListener != null) {
            mLaneListener.onItemSelected(mFocusedEntryView.getEntry());
        }
    }

    private void adaptModelState() {
        transitToState(mViewModel.getState());
        applySizeParameters();
    }

    private void initEntryState(LaunchEntryViewModel.State state)
    {
        for(LaunchEntryView ev : mEntryViews)
        {
            ev.setState(state);
        }
    }

    private void sendAllEntriesToState(final LaunchEntryViewModel.State state, boolean topDown)
    {
        sendAllEntriesToState(state, topDown, null);
    }

    private void sendAllEntriesToState(final LaunchEntryViewModel.State state, boolean topDown, LaunchEntryView except)
    {
        int delay = 0;
        int start = 0;
        int end = mEntryViews.size();
        int diff = 1;
        if(!topDown)
        {
            start = mEntryViews.size() - 1;
            end = -1;
            diff = -1;
        }
        for(int i=start; i<end; i+=diff)
        {
            if(mEntryViews.get(i) != except) {
                mEntryViews.get(i).gotoState(state, delay += mViewModel.getEntryMoveDiffMS());
            }
        }
    }

    private void applySizeParameters()
    {
        mSelectedIcon.setMaxHeight((int) ViewUtils.getPxFromDip(getContext(), mViewModel.getImageWidthDip()));
        mSelectedIcon.setMaxWidth((int) ViewUtils.getPxFromDip(getContext(), mViewModel.getImageWidthDip()));
    }

    private void showSelectionIndicator()
    {
        if(mFocusedEntryView == null) {
            return;
        }
        Rect fromRect = new Rect();
        mFocusedEntryView.getHitRect(fromRect);
        Rect toRect = new Rect();
        mSelectIndicatorContainer.getHitRect(toRect);

        Drawable drawable = mFocusedEntryView.getEntry().getIcon(getContext());
        mSelectedIcon.setImageDrawable(drawable);

        mSelectedItemTextView.setText(mFocusedEntryView.getEntry().getName(getContext()));

        Palette p = Palette.from(BitmapUtils.drawableToBitmap(drawable)).generate();

        mSelectIndicator.setBackgroundColor(getColorFromPalette(p, mViewModel.getFrameDefaultColor()));

        try {
            ObjectAnimator anim = ObjectAnimator.ofObject(
                    mSelectIndicator,
                    "margins",
                    new PositionAndSizeEvaluator(mSelectIndicator),
                    fromRect,
                    toRect);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    transitToState(LaunchLaneViewModel.State.Selected);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            anim.setDuration(mViewModel.getSelectingAnimationDurationMS());
            anim.start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(mViewModel.getSelectingAnimationDurationMS() / 2);
                        mSelectedItemTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                mSelectedItemTextView.setVisibility(View.VISIBLE);
                            }
                        });
                    } catch (InterruptedException e) {
                    }
                }
            }).start();
        } catch(Exception e) {
        }
        mSelectIndicator.setVisibility(View.VISIBLE);
    }

    private int getColorFromPalette(Palette palette, int defaultColor) {
        int result = palette.getLightMutedColor(Color.BLACK);
        if(result == Color.BLACK) {
            result = palette.getLightVibrantColor(Color.BLACK);
        }
        if(result == Color.BLACK) {
            result = palette.getMutedColor(Color.BLACK);
        }
        if(result == Color.BLACK) {
            result = palette.getVibrantColor(Color.BLACK);
        }
        if(result == Color.BLACK) {
            result = defaultColor;
        }
        return result;
    }

    private void hideSelectionIndicator()
    {
        if(mSelectIndicator != null) {
            mSelectIndicator.setVisibility(View.INVISIBLE);
        }
        if(mSelectedItemTextView != null) {
            mSelectedItemTextView.setVisibility(View.GONE);
        }
    }

    private void ensureFocusedEntryAt(int y)
    {
        mFocusedEntryView = null;
        for(LaunchEntryView ev : mEntryViews)
        {
            boolean hit = isEntryAt(ev, y);
            LaunchEntryViewModel.State desiredState = LaunchEntryViewModel.State.Active;
            if(hit)
            {
                desiredState = LaunchEntryViewModel.State.Focused;
                mFocusedEntryView = ev;
            }
            ev.gotoState(desiredState);
        }
    }

    private boolean isEntryAt(LaunchEntryView entryView, int y)
    {
        return entryView.getY() < y && y < entryView.getY() + entryView.getHeight();
    }
}
