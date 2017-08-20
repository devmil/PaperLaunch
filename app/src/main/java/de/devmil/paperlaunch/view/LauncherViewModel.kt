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
package de.devmil.paperlaunch.view

import de.devmil.paperlaunch.config.IDesignConfig
import de.devmil.paperlaunch.model.IEntry
import de.devmil.paperlaunch.config.ILaunchEntryConfig
import de.devmil.paperlaunch.config.ILaunchLaneConfig
import de.devmil.paperlaunch.config.LaunchConfig

class LauncherViewModel(private val mConfig: LaunchConfig) {
    var state = State.Init

    enum class State {
        Init,
        Initializing,
        Ready
    }

    val entries: List<IEntry>
        get() = mConfig.entries

    val isOnRightSide: Boolean
        get() = mConfig.isOnRightSide

    val entryConfig: ILaunchEntryConfig
        get() = mConfig

    val laneConfig: ILaunchLaneConfig
        get() = mConfig

    val designConfig: IDesignConfig
        get() = mConfig.designConfig

    val highElevationDip: Float
        get() = mConfig.highElevationDip

    val neutralZoneWidthDip: Float
        get() = mConfig.neutralZoneWidthDip

    val launcherInitAnimationDurationMS: Int
        get() = mConfig.launcherInitAnimationDurationMS

    val frameDefaultColor: Int
        get() = mConfig.designConfig.frameDefaultColor

    val itemNameTextSizeSP: Float
        get() = mConfig.itemNameTextSizeSP

    val showBackground: Boolean
        get() = mConfig.showLauncherBackground

    val backgroundAlpha: Float
        get() = mConfig.launcherBackgroundAlpha

    val backgroundColor: Int
        get() = mConfig.launcherBackgroundColor

    val backgroundAnimationDurationMS: Int
        get() = mConfig.launcherBackgroundAnimationDurationMS
}
