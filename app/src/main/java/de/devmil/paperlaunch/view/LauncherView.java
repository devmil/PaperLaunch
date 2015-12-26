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
import android.content.Intent;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;

import de.devmil.paperlaunch.R;
import de.devmil.paperlaunch.model.IEntry;
import de.devmil.paperlaunch.model.IFolder;
import de.devmil.paperlaunch.config.LaunchConfig;
import de.devmil.paperlaunch.model.Launch;
import de.devmil.paperlaunch.utils.PositionAndSizeEvaluator;
import de.devmil.paperlaunch.utils.ViewUtils;
import de.devmil.paperlaunch.view.utils.ColorUtils;
import de.devmil.paperlaunch.view.widgets.VerticalTextView;

public class LauncherView extends RelativeLayout {

    private static final String TAG = LauncherView.class.getSimpleName();

    private LauncherViewModel mViewModel;
    private List<LaunchLaneView> mLaneViews = new ArrayList<>();
    private RelativeLayout mBackground;
    private LinearLayout mNeutralZone;
    private LinearLayout mNeutralZoneBackground;
    private ImageView mNeutralZoneBackgroundImage;
    private VerticalTextView mNeutralZoneBackgroundAppNameText;
    private ILauncherViewListener mListener;
    private MotionEvent mAutoStartMotionEvent;

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
        transitToState(LauncherViewModel.State.Init);
        transitToState(LauncherViewModel.State.Initializing);
    }

    public void setListener(ILauncherViewListener listener) {
        mListener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(mAutoStartMotionEvent != null) {
            start();
            onTouchEvent(mAutoStartMotionEvent);
            mAutoStartMotionEvent = null;
        }
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

    public void doAutoStart(MotionEvent firstMotionEvent) {
        mAutoStartMotionEvent = firstMotionEvent;
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
        addBackground();
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
        mNeutralZone.bringToFront();
        mNeutralZoneBackground.bringToFront();
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
                    IFolder f = (IFolder) selectedItem;
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

    private void addBackground() {
        mBackground = new RelativeLayout(getContext());
        mBackground.setBackgroundColor(mViewModel.getBackgroundColor());
        mBackground.setAlpha(0f);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        addView(mBackground, params);
    }

    private void addNeutralZone()
    {
        mNeutralZone = new LinearLayout(getContext());
        mNeutralZone.setId(R.id.id_launchview_neutralzone);
        mNeutralZone.setMinimumWidth((int) ViewUtils.getPxFromDip(getContext(), mViewModel.getNeutralZoneWidthDip()));
        mNeutralZone.setClickable(false);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        if(mViewModel.isOnRightSide())
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        else
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        addView(mNeutralZone, params);

        mNeutralZoneBackground = new LinearLayout(getContext());
        mNeutralZoneBackground.setBackgroundColor(mViewModel.getDesignConfig().getFrameDefaultColor());
        mNeutralZoneBackground.setElevation(ViewUtils.getPxFromDip(getContext(), mViewModel.getHighElevationDip()));
        mNeutralZoneBackground.setClickable(false);
        mNeutralZoneBackground.setOrientation(LinearLayout.VERTICAL);
        mNeutralZoneBackground.setGravity(Gravity.CENTER_HORIZONTAL);

        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        mNeutralZone.addView(mNeutralZoneBackground, backParams);

        mNeutralZoneBackgroundImage = new ImageView(getContext());
        mNeutralZoneBackgroundImage.setImageResource(R.mipmap.ic_launcher);
        mNeutralZoneBackgroundImage.setClickable(false);
        mNeutralZoneBackgroundImage.setVisibility(View.GONE);

        LinearLayout.LayoutParams backImageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        backImageParams.setMargins(0, (int) ViewUtils.getPxFromDip(getContext(), mViewModel.getLaneConfig().getLaneIconTopMarginDip()), 0, 0);
        mNeutralZoneBackground.addView(mNeutralZoneBackgroundImage, backImageParams);

        mNeutralZoneBackground.setBackgroundColor(
                ColorUtils.getBackgroundColorFromImage(
                        getResources().getDrawable(
                                R.mipmap.ic_launcher,
                                getContext().getTheme()
                        ),
                        mViewModel.getFrameDefaultColor()));

        mNeutralZoneBackgroundAppNameText = new VerticalTextView(getContext());
        mNeutralZoneBackgroundAppNameText.setVisibility(View.GONE);
        mNeutralZoneBackgroundAppNameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, mViewModel.getItemNameTextSizeSP());
        //this is needed because the parts in the system run with another theme than the application parts
        mNeutralZoneBackgroundAppNameText.setTextColor(getResources().getColor(R.color.name_label));
        mNeutralZoneBackgroundAppNameText.setText(R.string.app_name);
        mNeutralZoneBackgroundAppNameText.setVisibility(View.GONE);

        LinearLayout.LayoutParams backTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        backTextParams.setMargins(0, (int) ViewUtils.getPxFromDip(getContext(), mViewModel.getLaneConfig().getLaneTextTopMarginDip()), 0, 0);

        mNeutralZoneBackground.addView(mNeutralZoneBackgroundAppNameText, backTextParams);
    }

    private boolean sendIfMatches(LaunchLaneView laneView, int action, float x, float y, int laneNumber)
    {
        laneView.doHandleTouch(action, (int) (x - laneView.getX()), (int) (y - laneView.getY()));
        if(action == MotionEvent.ACTION_UP) {
            launchAppIfSelected();
            if(mListener != null) {
                mListener.onFinished();
            }
        }
        return true;
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
        try {
            getContext().startActivity(intent);
        } catch(Exception e) {
            Log.e(TAG, "Error while launching app", e);
        }
    }

    private void transitToState(LauncherViewModel.State newState) {
        switch(newState) {
            case Init:
                hideBackground();
                hideNeutralZone();
                break;
            case Initializing:
                animateBackground();
                animateNeutralZone();
                break;
            case Ready:
                startLane();
                break;
        }
        mViewModel.setState(newState);
    }

    private void startLane() {
        mLaneViews.get(0).start();
    }

    private void hideBackground() {
        mBackground.setAlpha(0f);
    }

    private void hideNeutralZone() {
        mNeutralZoneBackground.setVisibility(View.INVISIBLE);
    }

    private void animateBackground() {
        if(mViewModel.showBackground()) {
            mBackground.setVisibility(View.VISIBLE);
            mBackground
                .animate()
                    .alpha(mViewModel.getBackgroundAlpha())
                    .setDuration(mViewModel.getBackgroundAnimationDurationMS())
                    .start();
        } else {
            mBackground.setVisibility(View.GONE);
        }
    }

    private void animateNeutralZone() {
        float size = mViewModel.getNeutralZoneWidthDip();

        int fromLeft = mViewModel.isOnRightSide() ? getWidth() - (int)size : 0;
        int fromTop = (getHeight() - (int)size) / 2;

        if(mAutoStartMotionEvent != null) {
            fromTop = Math.min(
                    (getHeight() - (int)size),
                    (int)mAutoStartMotionEvent.getY()
            );
        }

        int fromRight = fromLeft + (int)size;
        int fromBottom = fromTop + (int)size;

        Rect fromRect = new Rect(
                fromLeft,
                fromTop,
                fromRight,
                fromBottom
                );
        Rect toRect = new Rect(
                fromLeft,
                0,
                fromRight,
                getHeight()
        );

        ObjectAnimator anim = null;
        try {
            anim = ObjectAnimator.ofObject(
                    mNeutralZoneBackground,
                    "margins",
                    new PositionAndSizeEvaluator(mNeutralZoneBackground),
                    fromRect,
                    toRect);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mNeutralZoneBackground.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                            }
                            mNeutralZoneBackground.post(new Runnable() {
                                @Override
                                public void run() {
                                    transitToState(LauncherViewModel.State.Ready);
                                }
                            });
                        }
                    }).start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            anim.setDuration(mViewModel.getLauncherInitAnimationDurationMS());
            anim.start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(mViewModel.getLauncherInitAnimationDurationMS() / 2);
                        mNeutralZoneBackgroundImage.post(new Runnable() {
                            @Override
                            public void run() {
                                mNeutralZoneBackgroundImage.setVisibility(View.VISIBLE);
                                mNeutralZoneBackgroundAppNameText.setVisibility(View.VISIBLE);
                            }
                        });
                    } catch (InterruptedException e) {
                    }
                }
            }).start();

        } catch (InvalidClassException e) {
            e.printStackTrace();
        }
    }
}
