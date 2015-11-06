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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import de.devmil.paperlaunch.R;
import de.devmil.paperlaunch.model.LaunchConfig;
import de.devmil.paperlaunch.model.Launch;
import de.devmil.paperlaunch.utils.ViewUtils;

/**
 * Created by michaellamers on 29.05.15.
 */
public class LauncherView extends RelativeLayout {
    private LaunchConfig mConfig;

    private LauncherViewModel mViewModel;
    private List<LaunchLaneView> mLaneViews = new ArrayList<>();
    private LinearLayout mNeutralZone;

    public LauncherView(Context context) {
        super(context);
        construct();
    }

    public LauncherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }

    public LauncherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        construct();
    }

    public LauncherView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        construct();
    }

    public void doInitialize(LaunchConfig config)
    {
        mConfig = config;

        buildViewModel();

        buildViews();
    }

    public void start()
    {
        mLaneViews.get(0).start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);

        int laneNum = 1;

        //find the lane to dispatch to
        for(LaunchLaneView l : mLaneViews)
        {
            result = sendIfMatches(l, event, laneNum++) || result;
        }

        return result;

    }

    private void construct()
    {
        ViewUtils.disableClipping(this);
    }

    private void buildViewModel()
    {
        //TODO: split into auto folders if there are too many of them
        List<LaunchLaneViewModel> laneModels = new ArrayList<>();
        List<LaunchEntryViewModel> entryModels = new ArrayList<>();
        for(Launch e : mConfig.getEntries())
        {
            entryModels.add(LaunchEntryViewModel.createFrom(getContext(), e, mConfig));
        }
        laneModels.add(new LaunchLaneViewModel(entryModels, mConfig));

        mViewModel = new LauncherViewModel(laneModels);
    }

    private void buildViews()
    {
        removeAllViews();
        mLaneViews.clear();
        addNeutralZone();
        int currentAnchor = mNeutralZone.getId();
        int[] laneIds = getResources().getIntArray(R.array.laneIds);
        int currentIdx = 0;
        for(LaunchLaneViewModel lm : mViewModel.getLaneViewModels())
        {
            LaunchLaneView llv = new LaunchLaneView(getContext());
            llv.setId(laneIds[currentIdx++]);

            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            if(mConfig.isOnRightSide())
                params.addRule(RelativeLayout.LEFT_OF, currentAnchor);
            else
                params.addRule(RelativeLayout.RIGHT_OF, currentAnchor);

            addView(llv, params);
            mLaneViews.add(llv);
            llv.doInitializeData(lm);

            currentAnchor = llv.getId();
        }
    }

    private void addNeutralZone()
    {
        mNeutralZone = new LinearLayout(getContext());
        mNeutralZone.setId(R.id.id_launchview_neutralzone);
        mNeutralZone.setBackgroundColor(mConfig.getDesignConfig().getFrameDefaultColor());
        mNeutralZone.setElevation(ViewUtils.getPxFromDip(getContext(), mConfig.getHighElevationDip()));
        mNeutralZone.setMinimumWidth((int)ViewUtils.getPxFromDip(getContext(), mConfig.getNeutralZoneWidthDip()));
        mNeutralZone.setClickable(false);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        if(mConfig.isOnRightSide())
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        else
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        addView(mNeutralZone, params);
    }

    private boolean sendIfMatches(LaunchLaneView laneView, MotionEvent event, int laneNumber)
    {
        float x = event.getX();
        float y = event.getY();

//        boolean hits = viewScreenRect.contains((int)rawX, (int)rawY);
//
//        if(hits
//                || event.getAction() == MotionEvent.ACTION_MOVE
//                || event.getAction() == MotionEvent.ACTION_UP)
//        {
            laneView.doHandleTouch(event.getAction(), (int)(x - laneView.getX()), (int)(y - laneView.getY()));
            return true;
//        }
//
//        return false;
    }
}
