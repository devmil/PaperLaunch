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

import android.graphics.drawable.Drawable;

import de.devmil.paperlaunch.model.ILaunchEntryConfig;
import de.devmil.paperlaunch.model.LaunchEntry;

/**
 * Created by michaellamers on 29.05.15.
 */
public class LaunchEntryViewModel {
    private String mAppName;
    private Drawable mAppIcon;
    private State mState = State.Inactive;
    private ILaunchEntryConfig mConfig;

    public LaunchEntryViewModel(String appName, Drawable appIcon, ILaunchEntryConfig config)
    {
        mAppName = appName;
        mAppIcon = appIcon;
        mConfig = config;
    }

    public String getAppName()
    {
        return mAppName;
    }

    public Drawable getAppIcon()
    {
        return mAppIcon;
    }

    public float getImageWidthDip()
    {
        return mConfig.getImageWidthDip();
    }

    public float getImageElevationDip()
    {
        return mConfig.getLowElevationDip();
    }

    public State getState()
    {
        return mState;
    }

    public void setState(State state)
    {
        mState = state;
    }

    public int getFrameDefaultColor() {
        return mConfig.getDesignConfig().getFrameDefaultColor();
    }

    public float getImageOffsetDip() {
        return mConfig.getImageOffsetDip();
    }

    public enum State
    {
        Inactive(0, -1, -1),
        Activating(1, 2, -1),
        Active(2, -1, 1),
        Focusing(3, 4, -1),
        Focused(4, -1, 3),
        Selected(5, -1, -1);

        private int mId;
        private int mAnimationRefId;
        private int mAnimationStateId;

        State(int id, int animationRefId, int animationStateId)
        {
            mId = id;
            mAnimationRefId = animationRefId;
            mAnimationStateId = animationStateId;
        }

        public boolean isAnimationStateFor(State other)
        {
            return mAnimationRefId == other.mId;
        }

        public boolean hasAnimationState() {
            return mAnimationStateId > -1;
        }

        public State getAnimationState()
        {
            return getStateForId(mAnimationStateId);
        }

        public static State getStateForId(int id)
        {
            if(Inactive.mId == id)
                return Inactive;
            if(Active.mId == id)
                return Active;
            if(Activating.mId == id)
                return Activating;
            if(Focusing.mId == id)
                return Focusing;
            if(Focused.mId == id)
                return Focused;
            if(Selected.mId == id)
                return Selected;
            return Inactive;
        }
    }

    public int getMoveDuration()
    {
        return mConfig.getEntryMoveAnimationDuration();
    }

    public int getAlphaDuration()
    {
        return mConfig.getEntryAlphaAnimationDuration();
    }

    public static LaunchEntryViewModel createFrom(LaunchEntry entry, ILaunchEntryConfig config) {
        return new LaunchEntryViewModel(entry.getAppName(), entry.getAppIcon(), config);
    }
}
