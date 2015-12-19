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
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import de.devmil.paperlaunch.R;
import de.devmil.paperlaunch.model.Folder;
import de.devmil.paperlaunch.model.IEntry;
import de.devmil.paperlaunch.model.LaunchConfig;
import de.devmil.paperlaunch.model.Launch;
import de.devmil.paperlaunch.utils.ViewUtils;

public class LauncherView extends RelativeLayout {
    private LauncherViewModel mViewModel;
    private List<LaunchLaneView> mLaneViews = new ArrayList<>();
    private LinearLayout mNeutralZone;
    private ILauncherViewListener mListener;

    private IEntry mCurrentlySelectedItem;

    public interface ILauncherViewListener {
        void onFinished();
    }

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
        buildViewModel(config);
    }

    public void start()
    {
        buildViews();
        mLaneViews.get(0).start();
    }

    public void setListener(ILauncherViewListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);

        result = handleTouchEvent(event.getAction(), event.getX(), event.getY()) || result;

        return result;
    }

    public boolean handleTouchEvent(int action, float x, float y) {
        boolean result = false;
        int laneNum = 1;

        //find the lane to dispatch to
        for(LaunchLaneView l : mLaneViews)
        {
            result = sendIfMatches(l, action, x, y, laneNum++) || result;
        }

        return result;
    }

    private void construct()
    {
        ViewUtils.disableClipping(this);
    }

    private void buildViewModel(LaunchConfig config)
    {
        mViewModel = new LauncherViewModel(config);
    }

    private int[] getLaneIds() {
        return new int[] {
                R.id.id_launchview_lane1,
                R.id.id_launchview_lane2,
                R.id.id_launchview_lane3,
                R.id.id_launchview_lane4,
                R.id.id_launchview_lane5,
                R.id.id_launchview_lane6,
                R.id.id_launchview_lane7,
                R.id.id_launchview_lane8
        };
    }

    private void buildViews()
    {
        removeAllViews();
        mLaneViews.clear();
        addNeutralZone();
        int currentAnchor = mNeutralZone.getId();
        int[] laneIds = getLaneIds();

        for(int i=0; i<laneIds.length; i++)
        {
            currentAnchor = addLaneView(i, currentAnchor).getId();
        }

        setEntriesToLane(mLaneViews.get(0), mViewModel.getEntries());

        for(int i=mLaneViews.size() - 1; i>=0; i--) {
            mLaneViews.get(i).bringToFront();
        }
    }

    private LaunchLaneView addLaneView(final int laneIndex, int anchorId) {
        final int[] laneIds = getLaneIds();
        int id = laneIds[laneIndex];

        LaunchLaneView llv = new LaunchLaneView(getContext());
        llv.setId(id);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        if(mViewModel.isOnRightSide())
            params.addRule(RelativeLayout.LEFT_OF, anchorId);
        else
            params.addRule(RelativeLayout.RIGHT_OF, anchorId);

        addView(llv, params);
        mLaneViews.add(llv);

        llv.setLaneListener(new LaunchLaneView.ILaneListener() {
            @Override
            public void onItemSelected(IEntry selectedItem) {
                mCurrentlySelectedItem = selectedItem;
                if (selectedItem == null) {
                    return;
                }
                if (selectedItem.isFolder()) {
                    Folder f = (Folder) selectedItem;
                    LaunchLaneView nextLaneView = mLaneViews.get(laneIndex + 1);
                    setEntriesToLane(nextLaneView, f.getSubEntries());
                    nextLaneView.start();
                }
            }

            @Override
            public void onItemSelecting(IEntry selectedItem) {
                mCurrentlySelectedItem = selectedItem;
            }

            @Override
            public void onStateChanged(LaunchLaneViewModel.State oldState, LaunchLaneViewModel.State newState) {
                if (newState == LaunchLaneViewModel.State.Focusing) {
                    for (int idx = laneIndex + 1; idx < mLaneViews.size(); idx++) {
                        mLaneViews.get(idx).stop();
                    }
                }
            }
        });

        return llv;
    }

    private void setEntriesToLane(LaunchLaneView laneView, List<IEntry> entries) {
        //TODO: split into auto folders if there are too many of them
        List<LaunchEntryViewModel> entryModels = new ArrayList<>();
        for(IEntry e : entries)
        {
            entryModels.add(LaunchEntryViewModel.createFrom(getContext(), e, mViewModel.getEntryConfig()));
        }

        LaunchLaneViewModel vm = new LaunchLaneViewModel(entryModels, mViewModel.getLaneConfig());
        laneView.doInitializeData(vm);
    }

    private void addNeutralZone()
    {
        mNeutralZone = new LinearLayout(getContext());
        mNeutralZone.setId(R.id.id_launchview_neutralzone);
        mNeutralZone.setBackgroundColor(mViewModel.getDesignConfig().getFrameDefaultColor());
        mNeutralZone.setElevation(ViewUtils.getPxFromDip(getContext(), mViewModel.getHighElevationDip()));
        mNeutralZone.setMinimumWidth((int)ViewUtils.getPxFromDip(getContext(), mViewModel.getNeutralZoneWidthDip()));
        mNeutralZone.setClickable(false);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        if(mViewModel.isOnRightSide())
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        else
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        addView(mNeutralZone, params);
    }

    private boolean sendIfMatches(LaunchLaneView laneView, int action, float x, float y, int laneNumber)
    {
//        boolean hits = viewScreenRect.contains((int)rawX, (int)rawY);
//
//        if(hits
//                || event.getAction() == MotionEvent.ACTION_MOVE
//                || event.getAction() == MotionEvent.ACTION_UP)
//        {
            laneView.doHandleTouch(action, (int) (x - laneView.getX()), (int) (y - laneView.getY()));
            if(action == MotionEvent.ACTION_UP) {
                launchAppIfSelected();
                if(mListener != null) {
                    mListener.onFinished();
                }
            }
            return true;
//        }
//
//        return false;
    }

    private void launchAppIfSelected() {
        if(mCurrentlySelectedItem == null) {
            return;
        }
        if(mCurrentlySelectedItem.isFolder()) {
            return;
        }
        Launch l = (Launch)mCurrentlySelectedItem;
        Intent intent = l.getLaunchIntent();
        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }
}
