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
package de.devmil.paperlaunch.model;

import java.util.ArrayList;
import java.util.List;

public class LaunchConfig implements ILaunchEntryConfig, ILaunchLaneConfig {
    private List<IEntry> mEntries;
    private float mImageWidthDip = 40;
    private float mLowerElevationDip = 2;
    private float mHighElevationDip = 6;
    private int mEntryMoveAnimationDuration = 100;
    private int mEntryMoveDiffMS = 50;
    private int mEntryAlphaAnimationDuration = 100;
    private boolean mIsOnRightSide = true;
    private IDesignConfig mDesignConfig = new DesignConfig();
    private float mNeutralZoneWidthDip = 40;
    private float mImageOffsetDip = 5;

    public List<IEntry> getEntries()
    {
        return new ArrayList<>(mEntries);
    }

    public LaunchConfig()
    {
    }

    public void setEntries(List<IEntry> entries)
    {
        mEntries = new ArrayList<>(entries);
    }

    public float getImageWidthDip()
    {
        return mImageWidthDip;
    }

    public float getLowElevationDip()
    {
        return mLowerElevationDip;
    }

    public float getHighElevationDip()
    {
        return mHighElevationDip;
    }

    public boolean isOnRightSide()
    {
        return mIsOnRightSide;
    }

    public int getEntryMoveAnimationDuration()
    {
        return mEntryMoveAnimationDuration;
    }

    public int getEntryAlphaAnimationDuration()
    {
        return mEntryAlphaAnimationDuration;
    }

    public int getEntryMoveDiffMS()
    {
        return mEntryMoveDiffMS;
    }

    public IDesignConfig getDesignConfig()
    {
        return mDesignConfig;
    }

    public float getNeutralZoneWidthDip() {
        return mNeutralZoneWidthDip;
    }

    public float getImageOffsetDip()
    {
        return mImageOffsetDip;
    }
}
