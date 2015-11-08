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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import de.devmil.paperlaunch.utils.ViewUtils;

public class LaunchLaneView extends RelativeLayout {
    private LaunchLaneViewModel mViewModel;

    //view components
    private LinearLayout mSelectIndicatorContainer;
    private LinearLayout mSelectIndicator;
    private LinearLayout mEntriesContainer;
    private ImageView mSelectedIcon;
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

    public void start()
    {
        gotoState(LaunchLaneViewModel.State.Focusing);
    }

    public void gotoState(LaunchLaneViewModel.State state)
    {
        transitToState(state);
    }

    public void doHandleTouch(int action, int x, int y)
    {
        int focusSelectionBorder = getWidth();
        if(mViewModel.getState() == LaunchLaneViewModel.State.Focusing)
        {
            if(action == MotionEvent.ACTION_UP) {
                sendAllEntriesToState(LaunchEntryViewModel.State.Active, true);
                mFocusedEntryView = null;
            }
            else {
                ensureFocusedEntryAt(y);
                if (x < focusSelectionBorder)
                    transitToState(LaunchLaneViewModel.State.Selected);
            }
        }
        else if(mViewModel.getState() == LaunchLaneViewModel.State.Selected)
        {
            if(x > focusSelectionBorder)
                transitToState(LaunchLaneViewModel.State.Focusing);
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

        LinearLayout.LayoutParams selectIndicatorParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        mSelectIndicatorContainer.addView(mSelectIndicator, selectIndicatorParams);
        ViewUtils.disableClipping(mSelectIndicator);

        mSelectedIcon = new ImageView(getContext());
        mSelectedIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mSelectedIcon.setImageResource(mViewModel.getUnknownAppImageId());

        LinearLayout.LayoutParams selectIconParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        mSelectIndicator.addView(mSelectedIcon, selectIconParams);
    }

    private void createEntryViews() {
        mEntriesContainer.removeAllViews();
        mEntryViews.clear();

        for(LaunchEntryViewModel e : mViewModel.getEntries())
        {
            LaunchEntryView ev = new LaunchEntryView(getContext());
            mEntryViews.add(ev);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
            case Selected:
                showSelectionIndicator();
                //TODO: keep selected state and move all others to inactive
                break;
        }
        mViewModel.setState(state);
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
            mEntryViews.get(i).gotoState(state, delay += mViewModel.getEntryMoveDiffMS());
        }
    }

    private void applySizeParameters()
    {
        mSelectedIcon.setMaxHeight((int)ViewUtils.getPxFromDip(getContext(), mViewModel.getImageWidthDip()));
        mSelectedIcon.setMaxWidth((int) ViewUtils.getPxFromDip(getContext(), mViewModel.getImageWidthDip()));
    }

    private void showSelectionIndicator()
    {
        mSelectIndicator.setVisibility(View.VISIBLE);
    }

    private void hideSelectionIndicator()
    {
        mSelectIndicator.setVisibility(View.INVISIBLE);
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
