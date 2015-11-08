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

import java.util.ArrayList;
import java.util.List;

import de.devmil.paperlaunch.model.ILaunchLaneConfig;

public class LaunchLaneViewModel {
    private List<LaunchEntryViewModel> mEntryModels;
    private ILaunchLaneConfig mConfig;
    private State mState = State.Init;

    public LaunchLaneViewModel(List<LaunchEntryViewModel> entryModels, ILaunchLaneConfig config)
    {
        mEntryModels = new ArrayList<>(entryModels);
        mConfig = config;
    }

    public List<LaunchEntryViewModel> getEntries()
    {
        return mEntryModels;
    }

    public State getState()
    {
        return mState;
    }

    public float getImageWidthDip()
    {
        return mConfig.getImageWidthDip();
    }

    public boolean isOnRightSide()
    {
        return mConfig.isOnRightSide();
    }

    public int getEntryMoveDiffMS()
    {
        return mConfig.getEntryMoveDiffMS();
    }

    public int getFrameDefaultColor() {
        return mConfig.getDesignConfig().getFrameDefaultColor();
    }

    public void setState(State state) {
        this.mState = state;
    }

    public int getUnknownAppImageId() {
        return mConfig.getDesignConfig().getUnknownAppImageId();
    }

    public float getSelectedImageElevationDip() {
        return mConfig.getHighElevationDip();
    }

    public enum State
    {
        Init,
        Focusing,
        Selected
    }
}
