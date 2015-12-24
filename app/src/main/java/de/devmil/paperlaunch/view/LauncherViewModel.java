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

import de.devmil.paperlaunch.model.IDesignConfig;
import de.devmil.paperlaunch.model.IEntry;
import de.devmil.paperlaunch.model.ILaunchEntryConfig;
import de.devmil.paperlaunch.model.ILaunchLaneConfig;
import de.devmil.paperlaunch.model.LaunchConfig;

public class LauncherViewModel {
    private LaunchConfig mConfig;
    private State mState = State.Init;

    enum State {
        Init,
        Initializing,
        Ready
    }

    public LauncherViewModel(LaunchConfig config)
    {
        mConfig = config;
    }

    public List<IEntry> getEntries() {
        return mConfig.getEntries();
    }

    public boolean isOnRightSide() {
        return mConfig.isOnRightSide();
    }

    public ILaunchEntryConfig getEntryConfig() {
        return mConfig;
    }

    public ILaunchLaneConfig getLaneConfig() {
        return mConfig;
    }

    public IDesignConfig getDesignConfig() {
        return mConfig.getDesignConfig();
    }

    public float getHighElevationDip() {
        return mConfig.getHighElevationDip();
    }

    public float getNeutralZoneWidthDip() {
        return mConfig.getNeutralZoneWidthDip();
    }

    public int getLauncherInitAnimationDurationMS() {
        return mConfig.getLauncherInitAnimationDurationMS();
    }

    public int getFrameDefaultColor() {
        return mConfig.getDesignConfig().getFrameDefaultColor();
    }

    public float getItemNameTextSizeSP() {
        return mConfig.getItemNameTextSizeSP();
    }

    public State getState() {
        return mState;
    }

    public void setState(State mState) {
        this.mState = mState;
    }

    public float getBackgroundAlpha() {
        return mConfig.getLauncherBackgroundAlpha();
    }

    public int getBackgroundColor() {
        return mConfig.getLauncherBackgroundColor();
    }

    public int getBackgroundAnimationDurationMS() {
        return mConfig.getLauncherBackgroundAnimationDurationMS();
    }
}
